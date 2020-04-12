package com.xabe.spring.cloud.stream.consumer.infrastructure.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;

import com.xabe.spring.cloud.stream.consumer.domain.entity.CarDO;
import com.xabe.spring.cloud.stream.consumer.domain.repository.ConsumerRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

class InMemoryConsumerRepositoryTest {

  private Logger logger;

  private ConsumerRepository consumerRepository;

  @BeforeEach
  public void setUp() throws Exception {
    this.logger = mock(Logger.class);
    this.consumerRepository = new InMemoryConsumerRepository(this.logger);
  }

  @Test
  public void shouldGetCars() throws Exception {
    //Given

    //When
    final List<CarDO> result = this.consumerRepository.getCarDOS();

    //Then
    assertThat(result, is(notNullValue()));
  }

  @Test
  public void shouldAddCar() throws Exception {
    //Given
    final CarDO carDO = CarDO.builder().build();

    //When
    this.consumerRepository.addCar(carDO);

    //Then
    assertThat(this.consumerRepository.getCarDOS().size(), is(greaterThanOrEqualTo(1)));
  }

  @Test
  public void shouldUpdateCar() throws Exception {
    //Given
    final CarDO carDO = CarDO.builder().id("update").name("update").build();
    this.consumerRepository.addCar(carDO);
    final int sizeBefore = this.consumerRepository.getCarDOS().size();

    //When
    this.consumerRepository.updateCar(carDO.toBuilder().name("name").build());

    //Then
    assertThat(this.consumerRepository.getCarDOS().size(), is(sizeBefore));
    assertThat(this.consumerRepository.getCarDOS().get(0).getName(), is("name"));
  }

  @Test
  public void shouldDeleteCar() throws Exception {
    //Given
    final CarDO carDO = CarDO.builder().id("update").build();
    this.consumerRepository.addCar(carDO);
    final int sizeBefore = this.consumerRepository.getCarDOS().size();

    //When
    this.consumerRepository.deleteCar(carDO);

    //Then
    assertThat(this.consumerRepository.getCarDOS().size(), is(sizeBefore - 1));
  }

}