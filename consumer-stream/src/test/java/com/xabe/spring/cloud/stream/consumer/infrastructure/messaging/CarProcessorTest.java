package com.xabe.spring.cloud.stream.consumer.infrastructure.messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.xabe.avro.v1.Car;
import com.xabe.avro.v1.CarCreated;
import com.xabe.avro.v1.CarDeleted;
import com.xabe.avro.v1.MessageEnvelope;
import com.xabe.avro.v1.Metadata;
import com.xabe.spring.cloud.stream.consumer.domain.exception.BusinessException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.messaging.support.MessageBuilder;

class CarProcessorTest {

  private Logger logger;

  private EventHandler eventHandler;

  private CircuitBreaker circuitBreaker;

  private CarProcessor carProcessor;

  @BeforeEach
  public void setUp() throws Exception {
    this.logger = mock(Logger.class);
    this.eventHandler = mock(EventHandler.class);
    final Map<Class, EventHandler> handlerMap = new HashMap<>();
    handlerMap.put(CarCreated.class, this.eventHandler);
    final CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom().failureRateThreshold(1L)
        .enableAutomaticTransitionFromOpenToHalfOpen().ignoreExceptions(BusinessException.class)
        .slidingWindowType(SlidingWindowType.COUNT_BASED).slidingWindowSize(1).minimumNumberOfCalls(1)
        .waitDurationInOpenState(Duration.ofSeconds(2)).build();
    final CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig);
    this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("circuitBreak");
    this.carProcessor = new CarProcessor(this.logger, handlerMap, 60000, 3, circuitBreakerRegistry);
  }

  @Test
  public void notShouldProcessCarEvent() throws Exception {
    final Car car = Car.newBuilder().setId("id").setName("name").build();
    final CarDeleted carDeleted = CarDeleted.newBuilder().setCar(car).setSentAt(1L).build();
    final MessageEnvelope messageEnvelope = MessageEnvelope.newBuilder().setMetadata(this.createMetaData()).setPayload(carDeleted).build();
    final Consumer consumer = spy(new MockConsumer(OffsetResetStrategy.EARLIEST));

    this.carProcessor.processCarEvent(MessageBuilder.withPayload(messageEnvelope).build(), consumer);

    verify(this.eventHandler, never()).handle(any());
  }

  @Test
  public void shouldProcessCarEvent() throws Exception {
    final Car car = Car.newBuilder().setId("id").setName("name").build();
    final CarCreated carCreated = CarCreated.newBuilder().setCar(car).setSentAt(1L).build();
    final MessageEnvelope messageEnvelope = MessageEnvelope.newBuilder().setMetadata(this.createMetaData()).setPayload(carCreated).build();
    final Consumer consumer = spy(new MockConsumer(OffsetResetStrategy.EARLIEST));

    this.carProcessor.processCarEvent(MessageBuilder.withPayload(messageEnvelope).build(), consumer);

    verify(this.eventHandler).handle(eq(carCreated));
  }

  private Metadata createMetaData() {
    return Metadata.newBuilder().setDomain("car").setName("car").setAction("update").setVersion("vTest")
        .setTimestamp(DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now())).build();
  }

}