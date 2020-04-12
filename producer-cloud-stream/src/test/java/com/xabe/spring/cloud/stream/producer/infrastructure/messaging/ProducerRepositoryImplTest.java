package com.xabe.spring.cloud.stream.producer.infrastructure.messaging;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.xabe.avro.v1.CarCreated;
import com.xabe.avro.v1.CarUpdated;
import com.xabe.avro.v1.MessageEnvelope;
import com.xabe.avro.v1.Metadata;
import com.xabe.spring.cloud.stream.producer.domain.entity.CarDO;
import com.xabe.spring.cloud.stream.producer.domain.repository.ProducerRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

class ProducerRepositoryImplTest {

  private Logger logger;

  private MessageChannel messageChannel;

  private Clock clock;

  private ProducerRepository producerRepository;

  @BeforeEach
  public void setUp() throws Exception {
    this.logger = mock(Logger.class);
    this.messageChannel = mock(MessageChannel.class);
    this.clock = mock(Clock.class);
    this.producerRepository = new ProducerRepositoryImpl(this.logger, this.messageChannel, this.clock);
  }

  @Test
  public void shouldSendCarCreated() throws Exception {
    final CarDO carDO = CarDO.builder().id("id").name("name").sentAt(5L).build();
    final ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);

    when(this.clock.getZone()).thenReturn(ZoneId.systemDefault());
    when(this.clock.instant()).thenReturn(Instant.parse("2018-11-30T18:35:24.00Z"));
    when(this.messageChannel.send(messageArgumentCaptor.capture())).thenReturn(true);

    this.producerRepository.saveCar(carDO);

    final Message result = messageArgumentCaptor.getValue();
    assertThat(result, is(notNullValue()));
    assertThat(result.getHeaders().get(ProducerRepositoryImpl.PARTITION_KEY), is("id"));
    assertThat(result.getPayload(), is(notNullValue()));
    final MessageEnvelope messageEnvelope = MessageEnvelope.class.cast(result.getPayload());
    final Metadata metadata = Metadata.class.cast(messageEnvelope.getMetadata());
    assertThat(metadata, is(notNullValue()));
    assertThat(metadata.getDomain(), is((ProducerRepositoryImpl.CAR)));
    assertThat(metadata.getName(), is((ProducerRepositoryImpl.CAR)));
    assertThat(metadata.getAction(), is((ProducerRepositoryImpl.CREATE)));
    assertThat(metadata.getVersion(), is((ProducerRepositoryImpl.V_TEST)));
    assertThat(metadata.getTimestamp(), is(("2018-11-30T19:35:24+01:00")));
    final CarCreated carCreated = CarCreated.class.cast(messageEnvelope.getPayload());
    assertThat(carCreated, is(notNullValue()));
    assertThat(carCreated.getSentAt(), is(5L));
    assertThat(carCreated.getCar().getId(), is("id"));
    assertThat(carCreated.getCar().getName(), is("name"));
  }

  @Test
  public void shouldSendCarUpdate() throws Exception {
    final CarDO carDO = CarDO.builder().id("id").name("name").sentAt(5L).build();
    final ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);

    when(this.clock.getZone()).thenReturn(ZoneId.systemDefault());
    when(this.clock.instant()).thenReturn(Instant.parse("2018-11-30T18:35:24.00Z"));
    when(this.messageChannel.send(messageArgumentCaptor.capture())).thenReturn(true);

    this.producerRepository.updateCar(carDO);

    final Message result = messageArgumentCaptor.getValue();
    assertThat(result, is(notNullValue()));
    assertThat(result.getHeaders().get(ProducerRepositoryImpl.PARTITION_KEY), is("id"));
    assertThat(result.getPayload(), is(notNullValue()));
    final MessageEnvelope messageEnvelope = MessageEnvelope.class.cast(result.getPayload());
    final Metadata metadata = Metadata.class.cast(messageEnvelope.getMetadata());
    assertThat(metadata, is(notNullValue()));
    assertThat(metadata.getDomain(), is((ProducerRepositoryImpl.CAR)));
    assertThat(metadata.getName(), is((ProducerRepositoryImpl.CAR)));
    assertThat(metadata.getAction(), is((ProducerRepositoryImpl.CREATE)));
    assertThat(metadata.getVersion(), is((ProducerRepositoryImpl.V_TEST)));
    assertThat(metadata.getTimestamp(), is(("2018-11-30T19:35:24+01:00")));
    final CarUpdated carUpdated = CarUpdated.class.cast(messageEnvelope.getPayload());
    assertThat(carUpdated, is(notNullValue()));
    assertThat(carUpdated.getSentAt(), is(5L));
    assertThat(carUpdated.getCar().getId(), is("id"));
    assertThat(carUpdated.getCar().getName(), is("name"));
  }

  @Test
  public void shouldSendCarDelete() throws Exception {
    final CarDO carDO = CarDO.builder().id("id").name("name").sentAt(5L).build();
    final ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);

    when(this.clock.getZone()).thenReturn(ZoneId.systemDefault());
    when(this.clock.instant()).thenReturn(Instant.parse("2018-11-30T18:35:24.00Z"));
    when(this.messageChannel.send(messageArgumentCaptor.capture())).thenReturn(true);

    this.producerRepository.updateCar(carDO);

    final Message result = messageArgumentCaptor.getValue();
    assertThat(result, is(notNullValue()));
    assertThat(result.getHeaders().get(ProducerRepositoryImpl.PARTITION_KEY), is("id"));
    assertThat(result.getPayload(), is(notNullValue()));
    final MessageEnvelope messageEnvelope = MessageEnvelope.class.cast(result.getPayload());
    final Metadata metadata = Metadata.class.cast(messageEnvelope.getMetadata());
    assertThat(metadata, is(notNullValue()));
    assertThat(metadata.getDomain(), is((ProducerRepositoryImpl.CAR)));
    assertThat(metadata.getName(), is((ProducerRepositoryImpl.CAR)));
    assertThat(metadata.getAction(), is((ProducerRepositoryImpl.CREATE)));
    assertThat(metadata.getVersion(), is((ProducerRepositoryImpl.V_TEST)));
    assertThat(metadata.getTimestamp(), is(("2018-11-30T19:35:24+01:00")));
    final CarUpdated carUpdated = CarUpdated.class.cast(messageEnvelope.getPayload());
    assertThat(carUpdated, is(notNullValue()));
    assertThat(carUpdated.getSentAt(), is(5L));
    assertThat(carUpdated.getCar().getId(), is("id"));
    assertThat(carUpdated.getCar().getName(), is("name"));
  }

}