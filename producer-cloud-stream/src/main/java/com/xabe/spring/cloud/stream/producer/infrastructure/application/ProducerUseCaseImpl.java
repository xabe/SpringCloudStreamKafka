package com.xabe.spring.cloud.stream.producer.infrastructure.application;

import com.xabe.spring.cloud.stream.producer.domain.entity.CarDO;
import com.xabe.spring.cloud.stream.producer.domain.repository.ProducerRepository;
import com.xabe.spring.cloud.stream.producer.infrastructure.persentation.payload.CarPayload;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ProducerUseCaseImpl implements ProducerUseCase {

  private final Logger logger;

  private final ProducerRepository producerRepository;

  @Override
  public void createCar(final CarPayload carPayload) {
    this.logger.info("Created carPayload {}", carPayload);
    this.producerRepository.saveCar(this.toCarDO(carPayload));
  }

  @Override
  public void updateCar(final CarPayload carPayload) {
    this.logger.info("Update carPayload {}", carPayload);
    this.producerRepository.updateCar(this.toCarDO(carPayload));
  }

  @Override
  public void deleteCar(final CarPayload carPayload) {
    this.logger.info("Delete carPayload {}", carPayload);
    this.producerRepository.deleteCar(this.toCarDO(carPayload));
  }

  private CarDO toCarDO(final CarPayload carPayload) {
    return CarDO.builder().id(carPayload.getId()).name(carPayload.getName()).sentAt(carPayload.getSentAt()).build();
  }
}
