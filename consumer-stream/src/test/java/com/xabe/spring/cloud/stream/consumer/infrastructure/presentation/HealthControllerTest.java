package com.xabe.spring.cloud.stream.consumer.infrastructure.presentation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;

class HealthControllerTest {

  private Logger logger;

  private HealthController healthController;

  @BeforeEach
  public void setUp() throws Exception {
    this.logger = mock(Logger.class);
    this.healthController = new HealthController(this.logger);
  }

  @Test
  public void shouldGetStatus() throws Exception {
    //Given

    //When
    final ResponseEntity result = this.healthController.healthCheck();

    //Then
    assertThat(result, is(notNullValue()));
    assertThat(result.getBody(), is(HealthController.OK));
    verify(this.logger).info(any());
  }
}