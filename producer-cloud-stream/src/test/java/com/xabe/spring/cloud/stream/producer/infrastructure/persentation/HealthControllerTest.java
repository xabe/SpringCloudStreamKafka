package com.xabe.spring.cloud.stream.producer.infrastructure.persentation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;

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

    final ResponseEntity result = this.healthController.healthCheck();

    assertThat(result, is(notNullValue()));
    assertThat(result.getBody(), is(HealthController.OK));
  }

}