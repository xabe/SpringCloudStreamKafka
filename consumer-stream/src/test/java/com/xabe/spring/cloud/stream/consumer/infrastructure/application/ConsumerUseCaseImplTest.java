package com.xabe.spring.cloud.stream.consumer.infrastructure.application;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.xabe.spring.cloud.stream.consumer.domain.entity.CarDO;
import com.xabe.spring.cloud.stream.consumer.domain.repository.ConsumerRepository;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

class ConsumerUseCaseImplTest {

  private Logger logger;

  private ConsumerRepository consumerRepository;

  private ConsumerUseCase consumerUseCase;

  @BeforeEach
  public void setUp() throws Exception {
    this.logger = mock(Logger.class);
    this.consumerRepository = mock(ConsumerRepository.class);
    this.consumerUseCase = new ConsumerUseCaseImpl(this.logger, this.consumerRepository);
  }

  @Test
  public void shouldGetCars() throws Exception {
    //Given
    final CarDO carDO = CarDO.builder().build();
    when(this.consumerRepository.getCarDOS()).thenReturn(Collections.singletonList(carDO));

    //When
    final List<CarDO> result = this.consumerUseCase.getCars();

    //Then
    assertThat(result, is(notNullValue()));
    assertThat(result, is(hasSize(1)));
    assertThat(result, is(hasItem(carDO)));
  }

}