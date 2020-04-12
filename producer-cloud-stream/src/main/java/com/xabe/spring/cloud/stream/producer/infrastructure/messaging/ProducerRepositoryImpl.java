package com.xabe.spring.cloud.stream.producer.infrastructure.messaging;

import com.xabe.avro.v1.Car;
import com.xabe.avro.v1.CarCreated;
import com.xabe.avro.v1.CarDeleted;
import com.xabe.avro.v1.CarUpdated;
import com.xabe.avro.v1.MessageEnvelope;
import com.xabe.avro.v1.Metadata;
import com.xabe.spring.cloud.stream.producer.domain.entity.CarDO;
import com.xabe.spring.cloud.stream.producer.domain.repository.ProducerRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProducerRepositoryImpl implements ProducerRepository {

  public static final String PARTITION_KEY = "partitionKey";

  public static final String V_TEST = "vTest";

  public static final String CAR = "car";

  public static final String CREATE = "create";

  private final Logger logger;

  @Qualifier("carOutputChannel")
  private final MessageChannel messageChannel;

  private final Clock clock;

  @Override
  public void saveCar(final CarDO carDO) {
    final Car car = Car.newBuilder().setId(carDO.getId()).setName(carDO.getName()).build();
    final CarCreated carCreated = CarCreated.newBuilder().setSentAt(carDO.getSentAt()).setCar(car).build();
    final MessageEnvelope messageEnvelope = MessageEnvelope.newBuilder().setMetadata(this.createMetaData(CREATE)).setPayload(carCreated)
        .build();
    this.logger.info("Send Command CarCreated {}", carCreated);
    this.messageChannel.send(MessageBuilder.withPayload(messageEnvelope).setHeader(PARTITION_KEY, car.getId()).build());
  }

  @Override
  public void updateCar(final CarDO carDO) {
    final Car car = Car.newBuilder().setId(carDO.getId()).setName(carDO.getName()).build();
    final CarUpdated carUpdated = CarUpdated.newBuilder().setSentAt(carDO.getSentAt()).setCar(car).setCarBeforeUpdate(car).build();
    final MessageEnvelope messageEnvelope = MessageEnvelope.newBuilder().setMetadata(this.createMetaData(CREATE)).setPayload(carUpdated)
        .build();
    this.logger.info("Send Command CarUpdate {}", carUpdated);
    this.messageChannel.send(MessageBuilder.withPayload(messageEnvelope).setHeader(PARTITION_KEY, car.getId()).build());
  }

  @Override
  public void deleteCar(final CarDO carDO) {
    final Car car = Car.newBuilder().setId(carDO.getId()).setName(carDO.getName()).build();
    final CarDeleted carDeleted = CarDeleted.newBuilder().setSentAt(carDO.getSentAt()).setCar(car).build();
    final MessageEnvelope messageEnvelope = MessageEnvelope.newBuilder().setMetadata(this.createMetaData(CREATE)).setPayload(carDeleted)
        .build();
    this.logger.info("Send Command CarDelete {}", carDeleted);
    this.messageChannel.send(MessageBuilder.withPayload(messageEnvelope).setHeader(PARTITION_KEY, car.getId()).build());

  }

  private Metadata createMetaData(final String action) {
    return Metadata.newBuilder().setDomain(CAR).setName(CAR).setAction(action).setVersion(V_TEST)
        .setTimestamp(DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now(this.clock))).build();
  }
}
