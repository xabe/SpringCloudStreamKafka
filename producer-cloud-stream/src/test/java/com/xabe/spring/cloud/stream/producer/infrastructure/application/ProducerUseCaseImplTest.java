package com.xabe.spring.cloud.stream.producer.infrastructure.application;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.xabe.spring.cloud.stream.producer.domain.entity.CarDO;
import com.xabe.spring.cloud.stream.producer.domain.repository.ProducerRepository;
import com.xabe.spring.cloud.stream.producer.infrastructure.persentation.payload.CarPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

class ProducerUseCaseImplTest {

  private Logger logger;

  private ProducerRepository producerRepository;

  private ProducerUseCase producerUseCase;

  @BeforeEach
  public void setUp() throws Exception {
    this.logger = mock(Logger.class);
    this.producerRepository = mock(ProducerRepository.class);
    this.producerUseCase = new ProducerUseCaseImpl(this.logger, this.producerRepository);
  }

  @Test
  public void shouldSaveCar() throws Exception {
    final CarPayload carPayload = CarPayload.builder().name("name").id("id").sentAt(5L).build();

    this.producerUseCase.createCar(carPayload);

    verify(this.producerRepository).saveCar(eq(CarDO.builder().sentAt(5L).name("name").id("id").build()));
  }

  @Test
  public void shouldUpdateCar() throws Exception {
    final CarPayload carPayload = CarPayload.builder().name("name").id("id").sentAt(5L).build();

    this.producerUseCase.updateCar(carPayload);

    verify(this.producerRepository).updateCar(eq(CarDO.builder().sentAt(5L).name("name").id("id").build()));
  }

  @Test
  public void shouldDeleteCar() throws Exception {
    final CarPayload carPayload = CarPayload.builder().name("name").id("id").sentAt(5L).build();

    this.producerUseCase.deleteCar(carPayload);

    verify(this.producerRepository).deleteCar(eq(CarDO.builder().sentAt(5L).name("name").id("id").build()));
  }

}