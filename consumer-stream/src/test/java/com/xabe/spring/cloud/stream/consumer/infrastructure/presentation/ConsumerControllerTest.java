package com.xabe.spring.cloud.stream.consumer.infrastructure.presentation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.xabe.spring.cloud.stream.consumer.domain.entity.CarDO;
import com.xabe.spring.cloud.stream.consumer.infrastructure.application.ConsumerUseCase;
import com.xabe.spring.cloud.stream.consumer.infrastructure.presentation.payload.CarPayload;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;

class ConsumerControllerTest {

  private Logger logger;

  private ConsumerUseCase consumerUseCase;

  private ConsumerController consumerController;

  @BeforeEach
  public void setUp() throws Exception {
    this.logger = mock(Logger.class);
    this.consumerUseCase = mock(ConsumerUseCase.class);
    this.consumerController = new ConsumerController(this.logger, this.consumerUseCase);
  }

  @Test
  public void shouldGetAllEventConsumer() throws Exception {
    //Given
    final CarDO carDO = CarDO.builder().build();
    when(this.consumerUseCase.getCars()).thenReturn(Collections.singletonList(carDO));

    //When
    final ResponseEntity result = this.consumerController.getCars();

    //Then
    assertThat(result, is(notNullValue()));
    assertThat(result.getBody(), is(notNullValue()));
    final List<CarPayload> list = List.class.cast(result.getBody());
    assertThat(list, is(hasSize(1)));
    assertThat(list, is(hasItem(CarPayload.builder(carDO).build())));
  }

}