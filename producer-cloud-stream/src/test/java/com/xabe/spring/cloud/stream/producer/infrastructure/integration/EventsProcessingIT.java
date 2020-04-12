package com.xabe.spring.cloud.stream.producer.infrastructure.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsNot.not;

import com.xabe.avro.v1.CarCreated;
import com.xabe.avro.v1.CarDeleted;
import com.xabe.avro.v1.CarUpdated;
import com.xabe.avro.v1.MessageEnvelope;
import com.xabe.spring.cloud.stream.producer.infrastructure.persentation.payload.CarPayload;
import java.util.List;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;

public class EventsProcessingIT extends BaseIT {

  private static final long DEFAULT_TIMEOUT = 3000;

  @Autowired
  public CarProcessor carProcessor;

  @BeforeEach
  public void init() {
    this.carProcessor.before();
  }

  @Test
  public void shouldCreatedCar() throws Exception {
    final CarPayload carPayload = CarPayload.builder().id("id").name("mazda 3").build();

    final HttpResponse<JsonNode> response = Unirest.post(String.format("http://localhost:%d/producer/car", this.serverPort))
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).body(carPayload).asJson();

    assertThat(response, is(notNullValue()));
    assertThat(response.getStatus(), is(200));

    final Message<MessageEnvelope> result = this.carProcessor.expectMessagePipe(CarCreated.class, DEFAULT_TIMEOUT);
    assertThat(result, is(notNullValue()));
  }

  @Test
  public void shouldUpdateCar() throws Exception {
    final CarPayload carPayload = CarPayload.builder().id("id").name("mazda 5").build();

    final HttpResponse<JsonNode> response = Unirest.put(String.format("http://localhost:%d/producer/car", this.serverPort))
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).body(carPayload).asJson();

    assertThat(response, is(notNullValue()));
    assertThat(response.getStatus(), is(200));

    final Message<MessageEnvelope> result = this.carProcessor.expectMessagePipe(CarUpdated.class, DEFAULT_TIMEOUT);
    assertThat(result, is(notNullValue()));
  }

  @Test
  public void shouldDeleteCar() throws Exception {

    final HttpResponse<JsonNode> response = Unirest.delete(String.format("http://localhost:%d/producer/car/1", this.serverPort))
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).asJson();

    assertThat(response, is(notNullValue()));
    assertThat(response.getStatus(), is(200));

    final Message<MessageEnvelope> result = this.carProcessor.expectMessagePipe(CarDeleted.class, DEFAULT_TIMEOUT);
    assertThat(result, is(notNullValue()));
  }

  @Test
  public void givenASameVersionWhenInvokeUpdateVersionReturnVersionCommand() throws Exception {
    final CarPayload carPayload = CarPayload.builder().id("id").name("test").build();

    final HttpResponse<JsonNode> response = Unirest.post(String.format("http://localhost:%d/producer/car", this.serverPort))
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).body(carPayload).asJson();

    final HttpResponse<JsonNode> response1 = Unirest.post(String.format("http://localhost:%d/producer/car", this.serverPort))
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).body(carPayload).asJson();

    assertThat(response, is(notNullValue()));
    assertThat(response.getStatus(), is(200));

    assertThat(response1, is(notNullValue()));
    assertThat(response1.getStatus(), is(200));

    final List<Message<MessageEnvelope>> result = this.carProcessor.expectMultipleMessagesPipe(CarCreated.class, DEFAULT_TIMEOUT, 2);
    assertThat(result, is(notNullValue()));
    assertThat(result, is(hasSize(2)));
    final Integer idPartition1 = (Integer) result.get(0).getHeaders().get(KafkaHeaders.RECEIVED_PARTITION_ID);
    final Integer idPartition2 = (Integer) result.get(1).getHeaders().get(KafkaHeaders.RECEIVED_PARTITION_ID);
    assertThat(idPartition1, is(equalTo(idPartition2)));
  }

  @Test
  public void givenADistinctVersionWhenInvokeUpdateVersionReturnVersionCommand() throws Exception {
    final CarPayload carPayload = CarPayload.builder().id("id").name("test").build();

    final HttpResponse<JsonNode> response = Unirest.post(String.format("http://localhost:%d/producer/car", this.serverPort))
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).body(carPayload).asJson();

    final HttpResponse<JsonNode> response1 = Unirest.post(String.format("http://localhost:%d/producer/car", this.serverPort))
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).body(carPayload.toBuilder().id("test").build()).asJson();

    assertThat(response, is(notNullValue()));
    assertThat(response.getStatus(), is(200));

    assertThat(response1, is(notNullValue()));
    assertThat(response1.getStatus(), is(200));

    final List<Message<MessageEnvelope>> result = this.carProcessor.expectMultipleMessagesPipe(CarCreated.class, DEFAULT_TIMEOUT, 2);
    assertThat(result, is(notNullValue()));
    assertThat(result, is(hasSize(2)));
    final Integer idPartition1 = (Integer) result.get(0).getHeaders().get(KafkaHeaders.RECEIVED_PARTITION_ID);
    final Integer idPartition2 = (Integer) result.get(1).getHeaders().get(KafkaHeaders.RECEIVED_PARTITION_ID);
    assertThat(idPartition1, is(not((equalTo(idPartition2)))));
  }
}
