package com.xabe.spring.cloud.stream.consumer.infrastructure.integration;

import com.xabe.avro.v1.Car;
import com.xabe.avro.v1.CarCreated;
import com.xabe.avro.v1.CarDeleted;
import com.xabe.avro.v1.CarUpdated;
import com.xabe.avro.v1.MessageEnvelope;
import com.xabe.avro.v1.Metadata;
import com.xabe.spring.cloud.stream.consumer.App;
import com.xabe.spring.cloud.stream.consumer.domain.repository.ConsumerRepository;
import com.xabe.spring.cloud.stream.consumer.infrastructure.integration.config.IntegrationStreams;
import com.xabe.spring.cloud.stream.consumer.infrastructure.integration.config.UrlUtil;
import com.xabe.spring.cloud.stream.consumer.infrastructure.presentation.payload.CarPayload;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.apache.commons.io.IOUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(classes = App.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class EventsProcessingIT {

  private static final String PARTITION_KEY = "partitionKey";

  public static final int TIMEOUT_MS = 5000;

  public static final int DELAY_MS = 1500;

  public static final int POLL_INTERVAL_MS = 500;

  @Autowired
  private IntegrationStreams integrationStreams;

  @Autowired
  private ConsumerRepository consumerRepository;

  @LocalServerPort
  private int serverPort;

  @BeforeAll
  public static void createMapping() throws IOException {
    final InputStream car = EventsProcessingIT.class.getClassLoader().getResourceAsStream("avro-car.json");
    Unirest.post(UrlUtil.getInstance().getSchemaRegistryCar()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(IOUtils.toString(car, StandardCharsets.UTF_8)).asJson();

  }

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

    Awaitility.await().pollDelay(DELAY_MS, TimeUnit.MILLISECONDS).pollInterval(POLL_INTERVAL_MS, TimeUnit.MILLISECONDS)
        .atMost(TIMEOUT_MS, TimeUnit.MILLISECONDS).until(() -> {

      final HttpResponse<CarPayload[]> response = Unirest.get(String.format("http://localhost:%d/consumer", this.serverPort))
          .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).asObject(CarPayload[].class);

      return response != null && (response.getStatus() >= 200 || response.getStatus() < 300) && response.getBody().length >= 1;
    });
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
    Awaitility.await().pollDelay(DELAY_MS, TimeUnit.MILLISECONDS).pollInterval(POLL_INTERVAL_MS, TimeUnit.MILLISECONDS)
        .atMost(TIMEOUT_MS, TimeUnit.MILLISECONDS).until(() -> {

      final HttpResponse<CarPayload[]> response = Unirest.get(String.format("http://localhost:%d/consumer", this.serverPort))
          .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).asObject(CarPayload[].class);

      return response != null && (response.getStatus() >= 200 || response.getStatus() < 300) && response.getBody().length >= 1;
    });
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
    Awaitility.await().pollDelay(DELAY_MS, TimeUnit.MILLISECONDS).pollInterval(POLL_INTERVAL_MS, TimeUnit.MILLISECONDS)
        .atMost(TIMEOUT_MS, TimeUnit.MILLISECONDS).until(() -> {

      final HttpResponse<CarPayload[]> response = Unirest.get(String.format("http://localhost:%d/consumer", this.serverPort))
          .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).asObject(CarPayload[].class);

      return response != null && (response.getStatus() >= 200 || response.getStatus() < 300) && response.getBody().length == 0;
    });
  }

  private Metadata createMetaData() {
    return Metadata.newBuilder().setDomain("car").setName("car").setAction("update").setVersion("vTest")
        .setTimestamp(DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now())).build();
  }

}
