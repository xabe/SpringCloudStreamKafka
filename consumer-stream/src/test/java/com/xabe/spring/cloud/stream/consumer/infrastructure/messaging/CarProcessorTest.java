package com.xabe.spring.cloud.stream.consumer.infrastructure.messaging;

import static com.xabe.spring.cloud.stream.consumer.infrastructure.messaging.CarProcessor.X_LAST_RETRY_TIMESTAMP_HEADER;
import static com.xabe.spring.cloud.stream.consumer.infrastructure.messaging.CarProcessor.X_RETRIES_HEADER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
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

  @Test
  public void notShouldProcessCarEventWhenCircuitBreakOpen() throws Exception {
    final Car car = Car.newBuilder().setId("id").setName("name").build();
    final CarCreated carCreated = CarCreated.newBuilder().setCar(car).setSentAt(1L).build();
    final MessageEnvelope messageEnvelope = MessageEnvelope.newBuilder().setMetadata(this.createMetaData()).setPayload(carCreated).build();
    final Consumer consumer = spy(new MockConsumer(OffsetResetStrategy.EARLIEST));
    this.circuitBreaker.transitionToOpenState();

    final Message<MessageEnvelope> result = this.carProcessor
        .processCarEvent(MessageBuilder.withPayload(messageEnvelope).build(), consumer);

    verify(this.eventHandler, never()).handle(eq(carCreated));
    verify(consumer).pause(any());
    assertThat(result, is(notNullValue()));
    assertThat(result.getHeaders().get(X_RETRIES_HEADER), is("0"));
    assertThat(result.getHeaders().get(X_LAST_RETRY_TIMESTAMP_HEADER), is(notNullValue()));
  }

  @Test
  public void shouldProcessCarEventDlqWhenTimeElapsedAndCircuitBreakClose() throws Exception {
    final Car car = Car.newBuilder().setId("id").setName("name").build();
    final CarCreated carCreated = CarCreated.newBuilder().setCar(car).setSentAt(1L).build();
    final MessageEnvelope messageEnvelope = MessageEnvelope.newBuilder().setMetadata(this.createMetaData()).setPayload(carCreated).build();
    final Consumer consumer = spy(new MockConsumer(OffsetResetStrategy.EARLIEST));
    final Map<String, Object> headers = new HashMap<>();
    headers.put(X_RETRIES_HEADER, "0");
    headers.put(X_LAST_RETRY_TIMESTAMP_HEADER, Long.toString(System.currentTimeMillis() - 60000 - 100));

    this.carProcessor.processCarDlqEvent(MessageBuilder.withPayload(messageEnvelope).copyHeaders(headers).build(), consumer);

    verify(this.eventHandler).handle(eq(carCreated));
  }

  @Test
  public void notShouldProcessCarEventDlqWhenTimeElapsedMinorAndCircuitBreakClose() throws Exception {
    final Car car = Car.newBuilder().setId("id").setName("name").build();
    final CarCreated carCreated = CarCreated.newBuilder().setCar(car).setSentAt(1L).build();
    final MessageEnvelope messageEnvelope = MessageEnvelope.newBuilder().setMetadata(this.createMetaData()).setPayload(carCreated).build();
    final Consumer consumer = spy(new MockConsumer(OffsetResetStrategy.EARLIEST));
    final Map<String, Object> headers = new HashMap<>();
    headers.put(X_RETRIES_HEADER, "0");
    final long millis = System.currentTimeMillis();
    headers.put(X_LAST_RETRY_TIMESTAMP_HEADER, Long.toString(millis));

    final Message<MessageEnvelope> result = this.carProcessor
        .processCarDlqEvent(MessageBuilder.withPayload(messageEnvelope).copyHeaders(headers).build(), consumer);

    verify(this.eventHandler, never()).handle(eq(carCreated));
    verify(consumer).pause(any());
    assertThat(result, is(notNullValue()));
    assertThat(result.getHeaders().get(X_RETRIES_HEADER), is("0"));
    assertThat(Long.valueOf(result.getHeaders().get(X_LAST_RETRY_TIMESTAMP_HEADER).toString()), is(greaterThanOrEqualTo(millis)));
  }

  @Test
  public void notShouldProcessCarEventDlqWhenTimeElapsedAndCircuitBreakOpen() throws Exception {
    final Car car = Car.newBuilder().setId("id").setName("name").build();
    final CarCreated carCreated = CarCreated.newBuilder().setCar(car).setSentAt(1L).build();
    final MessageEnvelope messageEnvelope = MessageEnvelope.newBuilder().setMetadata(this.createMetaData()).setPayload(carCreated).build();
    final Consumer consumer = spy(new MockConsumer(OffsetResetStrategy.EARLIEST));
    final Map<String, Object> headers = new HashMap<>();
    headers.put(X_RETRIES_HEADER, "0");
    final long millis = System.currentTimeMillis() - 60000 - 100;
    headers.put(X_LAST_RETRY_TIMESTAMP_HEADER, Long.toString(millis));
    this.circuitBreaker.transitionToForcedOpenState();

    final Message<MessageEnvelope> result = this.carProcessor
        .processCarDlqEvent(MessageBuilder.withPayload(messageEnvelope).copyHeaders(headers).build(), consumer);

    verify(this.eventHandler, never()).handle(eq(carCreated));
    verify(consumer).pause(any());
    assertThat(result, is(notNullValue()));
    assertThat(result.getHeaders().get(X_RETRIES_HEADER), is("0"));
    assertThat(Long.valueOf(result.getHeaders().get(X_LAST_RETRY_TIMESTAMP_HEADER).toString()), is(greaterThanOrEqualTo(millis)));
  }

  @Test
  public void givenAMessagingExceptionWithBusinessExceptionWhenInvokeFilterMessageForDlqThenReturnFalse() {
    final Car car = Car.newBuilder().setId("id").setName("name").build();
    final CarCreated carCreated = CarCreated.newBuilder().setCar(car).setSentAt(1L).build();
    final MessageEnvelope messageEnvelope = MessageEnvelope.newBuilder().setMetadata(this.createMetaData()).setPayload(carCreated).build();
    final Map<String, Object> headers = new HashMap<>();
    headers.put(X_RETRIES_HEADER, "0");
    headers.put(X_LAST_RETRY_TIMESTAMP_HEADER, Long.toString(System.currentTimeMillis()));
    final Message<MessageEnvelope> message = MessageBuilder.withPayload(messageEnvelope).copyHeaders(headers).build();
    final MessagingException messagingException = new MessagingException(message, "business fail",
        new BusinessException("business exception"));

    final boolean result = this.carProcessor.filterMessageForDlq(messagingException);

    assertThat(result, is(false));
  }

  @Test
  public void givenAMessagingExceptionWithoutBusinessExceptionWhenInvokeFilterMessageForDlqThenReturnFalse() {
    final Car car = Car.newBuilder().setId("id").setName("name").build();
    final CarCreated carCreated = CarCreated.newBuilder().setCar(car).setSentAt(1L).build();
    final MessageEnvelope messageEnvelope = MessageEnvelope.newBuilder().setMetadata(this.createMetaData()).setPayload(carCreated).build();
    final Map<String, Object> headers = new HashMap<>();
    headers.put(X_RETRIES_HEADER, "0");
    headers.put(X_LAST_RETRY_TIMESTAMP_HEADER, Long.toString(System.currentTimeMillis()));
    final Message<MessageEnvelope> message = MessageBuilder.withPayload(messageEnvelope).copyHeaders(headers).build();
    final MessagingException messagingException = new MessagingException(message, "business fail", new RuntimeException("exception"));

    final boolean result = this.carProcessor.filterMessageForDlq(messagingException);

    assertThat(result, is(true));
  }

  @Test
  public void givenAMessagingExceptionWithoutBusinessExceptionAndRetriesWhenInvokeFilterMessageForDlqThenReturnFalse() {
    final Car car = Car.newBuilder().setId("id").setName("name").build();
    final CarCreated carCreated = CarCreated.newBuilder().setCar(car).setSentAt(1L).build();
    final MessageEnvelope messageEnvelope = MessageEnvelope.newBuilder().setMetadata(this.createMetaData()).setPayload(carCreated).build();
    final Map<String, Object> headers = new HashMap<>();
    headers.put(X_RETRIES_HEADER, "4");
    headers.put(X_LAST_RETRY_TIMESTAMP_HEADER, Long.toString(System.currentTimeMillis()));
    final Message<MessageEnvelope> message = MessageBuilder.withPayload(messageEnvelope).copyHeaders(headers).build();
    final MessagingException messagingException = new MessagingException(message, "business fail", new RuntimeException("exception"));

    final boolean result = this.carProcessor.filterMessageForDlq(messagingException);

    assertThat(result, is(false));
  }

  @Test
  public void givenAMessagingExceptionWhenInvokeTransformMessageForDlqThenReturnMessage() {
    final Car car = Car.newBuilder().setId("id").setName("name").build();
    final CarCreated carCreated = CarCreated.newBuilder().setCar(car).setSentAt(1L).build();
    final MessageEnvelope messageEnvelope = MessageEnvelope.newBuilder().setMetadata(this.createMetaData()).setPayload(carCreated).build();
    final Message<MessageEnvelope> message = MessageBuilder.withPayload(messageEnvelope).build();
    final MessagingException messagingException = new MessagingException(message);

    final Message result = this.carProcessor.transformMessageForDlq(messagingException);

    assertThat(result, is(notNullValue()));
    final MessageHeaders headers = result.getHeaders();
    assertThat(headers, is(notNullValue()));
    assertThat(headers.get(X_RETRIES_HEADER, String.class), is("1"));
    assertThat(headers.get(X_LAST_RETRY_TIMESTAMP_HEADER, String.class), is(notNullValue()));
  }

  @Test
  public void givenAMessagingExceptionWhenInvokeTransformMessageForDlqThenThrowException() {
    final Car car = Car.newBuilder().setId("id").setName("name").build();
    final CarCreated carCreated = CarCreated.newBuilder().setCar(car).setSentAt(1L).build();
    final MessageEnvelope messageEnvelope = MessageEnvelope.newBuilder().setMetadata(this.createMetaData()).setPayload(carCreated).build();
    final Message<MessageEnvelope> message = MessageBuilder.withPayload(messageEnvelope).build();
    final MessagingException messagingException = spy(new MessagingException(message));

    when(messagingException.getFailedMessage()).thenReturn(null);
    Assertions.assertThrows(UnsupportedOperationException.class, () -> this.carProcessor.transformMessageForDlq(messagingException));
  }

  private Metadata createMetaData() {
    return Metadata.newBuilder().setDomain("car").setName("car").setAction("update").setVersion("vTest")
        .setTimestamp(DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now())).build();
  }

}