package com.xabe.spring.cloud.stream.consumer.infrastructure.messaging;

import com.xabe.avro.v1.MessageEnvelope;
import com.xabe.spring.cloud.stream.consumer.domain.exception.BusinessException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreaker.State;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import org.apache.avro.specific.SpecificRecord;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.binder.BinderHeaders;
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
      @Value("${dql-delay-interval-ms:60000}") final long dlqDelayIntervalMs, @Value("${dql-max-retries:3}") final int dlqMaxRetries,
      final CircuitBreakerRegistry circuitBreakerRegistry) {
    this.logger = logger;
    this.handlers = handlers;
    this.dlqDelayIntervalMs = dlqDelayIntervalMs;
    this.dlqMaxRetries = dlqMaxRetries;
    this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("circuitBreak");
  }

  @StreamListener(PipeStreams.CONSUMER_CAR_IN)
  @SendTo(PipeStreams.PRODUCER_CAR_DLQ_OUT)
  public Message<MessageEnvelope> processCarEvent(final Message<MessageEnvelope> message,
      @Header(KafkaHeaders.CONSUMER) final Consumer consumer) {
    this.logger.info("Consumer message {}", message.getPayload().getPayload().getClass().getSimpleName());
    if (this.isCircuitBreakerOpen(this.circuitBreaker.getState())) {
      this.logger.warn("CircuitBreaker is open and won't process message");
      consumer.pause(consumer.assignment());

      return this.buildDlqMessage(message, false);
    }
    this.circuitBreaker.executeRunnable(() -> this.processMessage(message.getPayload()));
    return null;
  }

  public boolean isCircuitBreakerOpen(final State state) {
    return state == State.FORCED_OPEN || state == State.OPEN;
  }

  @StreamListener(PipeStreams.CONSUMER_CAR_DLQ_IN)
  @SendTo(PipeStreams.PRODUCER_CAR_DLQ_OUT)
  public Message<MessageEnvelope> processCarDlqEvent(final Message<MessageEnvelope> message,
      @Header(KafkaHeaders.CONSUMER) final Consumer consumer) {
    this.logger.info("Consumer dlq message {}", message.getPayload().getPayload().getClass().getSimpleName());
    final Long lastRetryTimestamp = this.getRetriesTimeStampFromMessage(message);

    this.logger.debug("Processing DLQ'ed message {} for {} try queued in {}", message.getPayload().getPayload(),
        Instant.ofEpochMilli((lastRetryTimestamp)), consumer.assignment());

    if (lastRetryTimestamp > (System.currentTimeMillis() - this.dlqDelayIntervalMs)) {
      this.logger.warn("Received message from {}, pausing DLQ in {}, consumers... {}", message.getPayload().getPayload(),
          Instant.ofEpochMilli(lastRetryTimestamp), consumer.assignment());

      consumer.pause(consumer.assignment());
      return this.buildDlqMessage(message, false);
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
      final int retries = this.getRetriesFromMessage(originalMessage);
      this.logger.warn("Message number of retries {} {}", retries, originalMessage);
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
      return this.buildDlqMessage(originalMessage, true);
    }

    this.logger.warn("Routing failed message null to the DLQ");
    throw new UnsupportedOperationException();
  }

  private Message<MessageEnvelope> buildDlqMessage(final Message<MessageEnvelope> message, final boolean incrementRetries) {
    final int retries = this.getRetriesFromMessage(message);

    return MessageBuilder.fromMessage(message).setHeader(X_RETRIES_HEADER, Integer.toString(incrementRetries ? retries + 1 : retries))
        .setHeader(BinderHeaders.PARTITION_OVERRIDE, message.getHeaders().get(KafkaHeaders.RECEIVED_PARTITION_ID))
        .setHeader(X_LAST_RETRY_TIMESTAMP_HEADER, Long.toString(System.currentTimeMillis())).build();
  }

  private int getRetriesFromMessage(final Message<MessageEnvelope> message) {
    final String retries = message.getHeaders().get(X_RETRIES_HEADER, String.class);
    return Objects.isNull(retries) ? DEFAULT_RETRIES : Integer.parseInt(retries);
  }

  private long getRetriesTimeStampFromMessage(final Message<MessageEnvelope> message) {
    final String retriesTimeStamp = message.getHeaders().get(X_LAST_RETRY_TIMESTAMP_HEADER, String.class);
    return Objects.isNull(retriesTimeStamp) ? System.currentTimeMillis() : Long.parseLong(retriesTimeStamp);
  }

}
