package com.xabe.spring.cloud.stream.consumer.infrastructure.messaging;

import java.util.Map;
import java.util.Objects;

import com.xabe.avro.v1.MessageEnvelope;
import com.xabe.spring.cloud.stream.consumer.domain.exception.BusinessException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreaker.State;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerEvent;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerEvent.Type;
import org.apache.avro.specific.SpecificRecord;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class CarProcessor {

  public static final String X_RETRIES_HEADER = "x-retries";

  public static final String X_LAST_RETRY_TIMESTAMP_HEADER = "x-last-retry-timestamp";

  public static final int DEFAULT_RETRIES = 0;

  private final Logger logger;

  private final Map<Class, EventHandler> handlers;

  private final long dlqDelayIntervalMs;

  private final int dlqMaxRetries;

  private final CircuitBreaker circuitBreaker;

  @Autowired
  public CarProcessor(final Logger logger, @Qualifier("carHandlers") final Map<Class, EventHandler> handlers,
      @Value("${dlq-delay-interval-ms:5000}") final long dlqDelayIntervalMs, @Value("${dlq-max-retries:3}") final int dlqMaxRetries,
      final CircuitBreakerRegistry circuitBreakerRegistry) {
    this.logger = logger;
    this.handlers = handlers;
    this.dlqDelayIntervalMs = dlqDelayIntervalMs;
    this.dlqMaxRetries = dlqMaxRetries;
    this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("circuitBreak");
    this.circuitBreaker.getEventPublisher().onEvent(this::circuitBreakEventPublisher);
  }

  private void circuitBreakEventPublisher(final CircuitBreakerEvent circuitBreakerEvent) {
    if (Type.SUCCESS != circuitBreakerEvent.getEventType()) {
      this.logger.warn("New message processing CircuitBreaker event: {}", circuitBreakerEvent);
    }
  }

  @StreamListener(PipeStreams.CONSUMER_CAR_IN)
  @SendTo(PipeStreams.PRODUCER_CAR_DLQ_OUT)
  public Message<MessageEnvelope> processCarEvent(final Message<MessageEnvelope> message,
      @Header(KafkaHeaders.CONSUMER) final Consumer consumer) {
    this.logger.info("Consumer message {}", message.getPayload().getPayload().getClass().getSimpleName());
    if (CarProcessor.isCircuitBreakerOpen(this.circuitBreaker.getState())) {

      consumer.pause(consumer.assignment());
      this.logger.warn("CircuitBreaker is open and won't process message and paused consumer {} topic {}", !consumer.paused().isEmpty(),
          consumer.assignment());
      return CarProcessor.buildDlqMessage(message, false);
    }
    this.circuitBreaker.executeRunnable(() -> this.processMessage(message.getPayload()));
    return null;
  }

  public static boolean isCircuitBreakerOpen(final State state) {
    return state == State.FORCED_OPEN || state == State.OPEN;
  }

  @StreamListener(PipeStreams.CONSUMER_CAR_DLQ_IN)
  @SendTo(PipeStreams.PRODUCER_CAR_DLQ_OUT)
  public Message<MessageEnvelope> processCarDlqEvent(final Message<MessageEnvelope> message,
      @Header(KafkaHeaders.CONSUMER) final Consumer consumer) {
    final String event = message.getPayload().getPayload().getClass().getSimpleName();
    this.logger.info("Consumer dlq message {}", event);
    final Long lastRetryTimestamp = CarProcessor.getRetriesTimeStampFromMessage(message);
    final long timeElapsed = System.currentTimeMillis() - this.dlqDelayIntervalMs;
    if (lastRetryTimestamp > timeElapsed || CarProcessor.isCircuitBreakerOpen(this.circuitBreaker.getState())) {
      this.logger
          .warn("Received message from {}, time elapsed {} and circuit break {} pausing DLQ", event, lastRetryTimestamp - timeElapsed,
              this.circuitBreaker.getState());
      consumer.pause(consumer.assignment());
      return CarProcessor.buildDlqMessage(message, false);
    }

    this.circuitBreaker.executeRunnable(() -> this.processMessage(message.getPayload()));
    return null;
  }

  private void processMessage(final MessageEnvelope messageEnvelope) {
    final Class<?> msgClass = messageEnvelope.getPayload().getClass();
    final SpecificRecord payload = SpecificRecord.class.cast(messageEnvelope.getPayload());
    final EventHandler handler = this.handlers.get(msgClass);
    if (handler == null) {
      this.logger.warn("Received a non supported message. Type: {}, toString: {}", msgClass.getName(), payload.toString());
    } else {
      handler.handle(payload);
    }
  }

  public boolean filterMessageForDlq(final MessagingException message) {
    final boolean isBusinessException = ExceptionUtils.indexOfType(message.getCause(), BusinessException.class) >= 0;
    this.logger.error("Error stream binder exception: {} is BusinessException {} ", message.getMessage(), isBusinessException);

    final Message<MessageEnvelope> originalMessage = (Message<MessageEnvelope>) message.getFailedMessage();
    if (Objects.nonNull(originalMessage)) {
      final int retries = CarProcessor.getRetriesFromMessage(originalMessage);
      this.logger.warn("Message number of retries {} {}", retries, originalMessage.getPayload().getPayload().getClass().getSimpleName());
      if (retries > this.dlqMaxRetries) {
        this.logger.warn("Message {} discarded because it exceeded the number of retries {}", originalMessage, retries);
        return false;
      }
    }

    return !isBusinessException;
  }

  public Message<MessageEnvelope> transformMessageForDlq(final MessagingException exception) {
    final Message<MessageEnvelope> originalMessage = (Message<MessageEnvelope>) exception.getFailedMessage();

    if (Objects.nonNull(originalMessage)) {
      this.logger.warn("Routing failed message to the DLQ: {}", originalMessage);
      return CarProcessor.buildDlqMessage(originalMessage, true);
    }

    this.logger.warn("Routing failed message null to the DLQ");
    throw new UnsupportedOperationException();
  }

  private static Message<MessageEnvelope> buildDlqMessage(final Message<MessageEnvelope> message, final boolean incrementRetries) {
    final int retries = CarProcessor.getRetriesFromMessage(message);

    //if the retry is a new one, the time window should be honoured, therefore the timestamp should be created again instead of reusing it
    final long lastRetryTimestamp = incrementRetries ? System.currentTimeMillis() : CarProcessor.getRetriesTimeStampFromMessage(message);

    return MessageBuilder.fromMessage(message).setHeader(X_RETRIES_HEADER, Integer.toString(incrementRetries ? retries + 1 : retries))
        .setHeader(X_LAST_RETRY_TIMESTAMP_HEADER, Long.toString(lastRetryTimestamp)).build();
  }

  private static int getRetriesFromMessage(final Message<MessageEnvelope> message) {
    final String retries = message.getHeaders().get(X_RETRIES_HEADER, String.class);
    return Objects.isNull(retries) ? DEFAULT_RETRIES : Integer.parseInt(retries);
  }

  private static long getRetriesTimeStampFromMessage(final Message<MessageEnvelope> message) {
    final String retriesTimeStamp = message.getHeaders().get(X_LAST_RETRY_TIMESTAMP_HEADER, String.class);
    return Objects.isNull(retriesTimeStamp) ? System.currentTimeMillis() : Long.parseLong(retriesTimeStamp);
  }

}
