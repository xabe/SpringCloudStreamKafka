package com.xabe.spring.cloud.stream.producer.infrastructure.persentation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xabe.spring.cloud.stream.producer.infrastructure.application.ProducerUseCase;
import com.xabe.spring.cloud.stream.producer.infrastructure.persentation.payload.CarPayload;
import java.time.Clock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;

class ProducerControllerTest {

  private Logger logger;

  private Clock clock;

  private ProducerUseCase producerUseCase;

  private ProducerController producerController;

  @BeforeEach
  public void setUp() throws Exception {
    this.logger = mock(Logger.class);
    this.clock = mock(Clock.class);
    when(this.clock.millis()).thenReturn(1L);
    this.producerUseCase = mock(ProducerUseCase.class);
    this.producerController = new ProducerController(this.logger, this.clock, this.producerUseCase);
  }

  @Test
  public void givenACarPayloadWhenInvokeCreateCarThenReturnResponseEntity() throws Exception {
    final CarPayload carPayload = CarPayload.builder().build();

    final ResponseEntity result = this.producerController.createCar(carPayload);

    assertThat(result, is(notNullValue()));
    assertThat(result.getStatusCode().is2xxSuccessful(), is(true));
    verify(this.producerUseCase).createCar(eq(carPayload.toBuilder().sentAt(1L).build()));
  }

  @Test
  public void givenACarPayloadWhenInvokeUpdateCarThenReturnResponseEntity() throws Exception {
    final CarPayload carPayload = CarPayload.builder().build();

    final ResponseEntity result = this.producerController.updateCar(carPayload);

    assertThat(result, is(notNullValue()));
    assertThat(result.getStatusCode().is2xxSuccessful(), is(true));
    verify(this.producerUseCase).updateCar(eq(carPayload.toBuilder().sentAt(1L).build()));
  }

  @Test
  public void givenACarPayloadWhenInvokeDeleteCarThenReturnResponseEntity() throws Exception {
    final String id = "id";

    final ResponseEntity result = this.producerController.deleteCar(id);

    assertThat(result, is(notNullValue()));
    assertThat(result.getStatusCode().is2xxSuccessful(), is(true));
    verify(this.producerUseCase).deleteCar(eq(CarPayload.builder().id(id).name(ProducerController.DELETE).sentAt(1L).build()));
  }

}