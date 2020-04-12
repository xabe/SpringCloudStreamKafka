package com.xabe.spring.cloud.stream.consumer.infrastructure.messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.xabe.avro.v1.Car;
import com.xabe.avro.v1.CarCreated;
import com.xabe.avro.v1.CarDeleted;
import com.xabe.avro.v1.MessageEnvelope;
import com.xabe.avro.v1.Metadata;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

class CarProcessorTest {

  private Logger logger;

  private EventHandler eventHandler;

  private CarProcessor carProcessor;

  @BeforeEach
  public void setUp() throws Exception {
    this.logger = mock(Logger.class);
    this.eventHandler = mock(EventHandler.class);
    final Map<Class, EventHandler> handlerMap = new HashMap<>();
    handlerMap.put(CarCreated.class, this.eventHandler);
    this.carProcessor = new CarProcessor(this.logger, handlerMap);
  }

  @Test
  public void notShouldProcessCarEvent() throws Exception {
    final Car car = Car.newBuilder().setId("id").setName("name").build();
    final CarDeleted carDeleted = CarDeleted.newBuilder().setCar(car).setSentAt(1L).build();
    final MessageEnvelope messageEnvelope = MessageEnvelope.newBuilder().setMetadata(this.createMetaData()).setPayload(carDeleted).build();

    this.carProcessor.processCarEvent(messageEnvelope);

    verify(this.eventHandler, never()).handle(any());
  }

  @Test
  public void shouldProcessCarEvent() throws Exception {
    final Car car = Car.newBuilder().setId("id").setName("name").build();
    final CarCreated carCreated = CarCreated.newBuilder().setCar(car).setSentAt(1L).build();
    final MessageEnvelope messageEnvelope = MessageEnvelope.newBuilder().setMetadata(this.createMetaData()).setPayload(carCreated).build();

    this.carProcessor.processCarEvent(messageEnvelope);

    verify(this.eventHandler).handle(eq(carCreated));
  }

  private Metadata createMetaData() {
    return Metadata.newBuilder().setDomain("car").setName("car").setAction("update").setVersion("vTest")
        .setTimestamp(DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now())).build();
  }

}