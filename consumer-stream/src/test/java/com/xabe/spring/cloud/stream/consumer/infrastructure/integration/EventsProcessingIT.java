package com.xabe.spring.cloud.stream.consumer.infrastructure.integration;

import com.xabe.avro.v1.Car;
import com.xabe.avro.v1.CarCreated;
import com.xabe.avro.v1.CarDeleted;
import com.xabe.avro.v1.CarUpdated;
import com.xabe.avro.v1.MessageEnvelope;
import com.xabe.avro.v1.Metadata;
import com.xabe.spring.cloud.stream.consumer.domain.repository.ConsumerRepository;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;

public class EventsProcessingIT extends BaseIT {

  private static final String PARTITION_KEY = "partitionKey";

  private static final int TIMEOUT = 2;

  private static final int DELAY = 500;

  private static final int POLL_INTERVAL = 500;

  @Autowired
  private IntegrationStreams integrationStreams;

  @Autowired
  private ConsumerRepository consumerRepository;

  @BeforeEach
  public void init() {
    this.consumerRepository.clean();
  }

  @Test
  public void shouldConsumerCarCreated() throws Exception {
    //Given
    final Car car = Car.newBuilder().setId("id1").setName("mazda").build();
    final CarCreated carCreated = CarCreated.newBuilder().setSentAt(Instant.now().toEpochMilli()).setCar(car).build();
    final MessageEnvelope messageEnvelope = MessageEnvelope.newBuilder().setMetadata(this.createMetaData()).setPayload(carCreated).build();

    //When
    this.integrationStreams.carOutputChannel()
        .send(MessageBuilder.withPayload(messageEnvelope).setHeader(PARTITION_KEY, car.getId()).build());

    //Then
    Awaitility.await().pollDelay(DELAY, TimeUnit.MILLISECONDS).pollInterval(POLL_INTERVAL, TimeUnit.MILLISECONDS)
        .atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> this.consumerRepository.getCarDOS().size() == 1);
  }

  @Test
  public void shouldConsumerCarUpdate() throws Exception {
    //Given
    final Car carOld = Car.newBuilder().setId("id2").setName("mazda").build();
    this.integrationStreams.carOutputChannel().send(MessageBuilder.withPayload(
        MessageEnvelope.newBuilder().setMetadata(this.createMetaData())
            .setPayload(CarCreated.newBuilder().setSentAt(Instant.now().toEpochMilli()).setCar(carOld).build()).build())
        .setHeader(PARTITION_KEY, carOld.getId()).build());

    final Car car = Car.newBuilder().setId("id2").setName("mazda3").build();
    final CarUpdated carUpdated = CarUpdated.newBuilder().setSentAt(Instant.now().toEpochMilli()).setCarBeforeUpdate(carOld).setCar(car)
        .build();
    final MessageEnvelope messageEnvelope = MessageEnvelope.newBuilder().setMetadata(this.createMetaData()).setPayload(carUpdated).build();

    //When
    this.integrationStreams.carOutputChannel()
        .send(MessageBuilder.withPayload(messageEnvelope).setHeader(PARTITION_KEY, car.getId()).build());

    //Then
    Awaitility.await().pollDelay(DELAY, TimeUnit.MILLISECONDS).pollInterval(POLL_INTERVAL, TimeUnit.MILLISECONDS)
        .atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> this.consumerRepository.getCarDOS().size() == 1);
  }

  @Test
  public void shouldConsumerCarDelete() throws Exception {
    //Given
    final Car car = Car.newBuilder().setId("delete").setName("delete").build();
    this.integrationStreams.carOutputChannel().send(MessageBuilder.withPayload(
        MessageEnvelope.newBuilder().setMetadata(this.createMetaData())
            .setPayload(CarCreated.newBuilder().setSentAt(Instant.now().toEpochMilli()).setCar(car).build()).build())
        .setHeader(PARTITION_KEY, car.getId()).build());

    final CarDeleted carDeleted = CarDeleted.newBuilder().setSentAt(Instant.now().toEpochMilli()).setCar(car).build();
    final MessageEnvelope messageEnvelope = MessageEnvelope.newBuilder().setMetadata(this.createMetaData()).setPayload(carDeleted).build();

    //When
    this.integrationStreams.carOutputChannel()
        .send(MessageBuilder.withPayload(messageEnvelope).setHeader(PARTITION_KEY, car.getId()).build());

    //Then
    Awaitility.await().pollDelay(DELAY, TimeUnit.MILLISECONDS).pollInterval(POLL_INTERVAL, TimeUnit.MILLISECONDS)
        .atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> this.consumerRepository.getCarDOS().size() == 0);
  }

  private Metadata createMetaData() {
    return Metadata.newBuilder().setDomain("car").setName("car").setAction("update").setVersion("vTest")
        .setTimestamp(DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now())).build();
  }

}
