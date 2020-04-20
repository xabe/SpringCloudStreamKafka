package com.xabe.spring.cloud.stream.producer.infrastructure.integration.config;

import com.xabe.avro.v1.MessageEnvelope;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CarProcessor {

  private final BlockingQueue<Message<MessageEnvelope>> messagesKafka = new ArrayBlockingQueue<>(100);

  private final Logger logger;

  @StreamListener(IntegrationStreams.CONSUMER_CAR_IN)
  public void process(final Message<MessageEnvelope> message) throws InterruptedException {
    this.processMessage(message);
  }

  private void processMessage(final Message<MessageEnvelope> message) throws InterruptedException {
    this.logger.info("Car Command received {}", message.getPayload().getPayload().getClass().getName());
    this.logger.info("Car Command received partition id {}", message.getHeaders().get(KafkaHeaders.RECEIVED_PARTITION_ID));
    if (!this.messagesKafka.offer(message, 1, TimeUnit.SECONDS)) {
      this.logger.warn("Adding {} to messages kafka timed out", message.getPayload().getPayload().getClass().getName());
    }
  }

  public void before() {
    this.messagesKafka.clear();
  }

  public <T> Message<MessageEnvelope> expectMessagePipe(final Class<T> payloadClass, final long milliseconds) throws InterruptedException {
    final Message<MessageEnvelope> message = this.messagesKafka.poll(milliseconds, TimeUnit.MILLISECONDS);
    if (message == null) {
      throw new RuntimeException("An exception happened while polling the queue for " + payloadClass.getName());
    }
    if (!message.getPayload().getPayload().getClass().equals(payloadClass)) {
      throw new AssertionError("payload cant be casted");
    }
    return message;
  }

  public <T> List<Message<MessageEnvelope>> expectMultipleMessagesPipe(final Class<T> payloadClass, final long milliseconds,
      final int size) {
    return IntStream.range(0, size).mapToObj(item -> {
      try {
        return this.expectMessagePipe(payloadClass, milliseconds);
      } catch (InterruptedException e) {
        throw new RuntimeException(e.getCause());
      }
    }).collect(Collectors.toList());
  }

}
