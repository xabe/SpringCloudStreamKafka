package com.xabe.spring.cloud.stream.producer.infrastructure.persentation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xabe.spring.cloud.stream.producer.infrastructure.application.ProducerUseCase;
import com.xabe.spring.cloud.stream.producer.infrastructure.persentation.payload.CarPayload;
import com.xabe.spring.cloud.stream.producer.infrastructure.persentation.payload.ErrorPayload;
import java.time.Clock;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(ProducerController.class)
class ProducerControllerValidatorTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private Logger logger;

  @MockBean
  private Clock clock;

  @MockBean
  private CacheManager cacheManager;

  @MockBean
  private ProducerUseCase producerUseCase;

  @Test
  public void givenACarPayloadInvalidWhenInvokeCreateCarThenReturnResponseEntity() throws Exception {

    final MvcResult result = this.mockMvc.perform(post("/producer/car").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andDo(print()).andExpect(status().isBadRequest()).andReturn();

    final String contentAsString = result.getResponse().getContentAsString();

    assertThat(contentAsString, is(notNullValue()));
    final ErrorPayload errorPayload = this.objectMapper.readValue(contentAsString, ErrorPayload.class);
    assertThat(errorPayload.getFieldErrors(), is(hasSize(2)));
  }

  @Test
  public void givenACarPayloadValidWhenInvokeCreateCarThenReturnResponseEntity() throws Exception {
    final CarPayload carPayload = CarPayload.builder().id("id").name("name").build();

    when(this.clock.millis()).thenReturn(1L);

    this.mockMvc
        .perform(post("/producer/car").contentType(MediaType.APPLICATION_JSON).content(this.objectMapper.writeValueAsString(carPayload)))
        .andDo(print()).andExpect(status().isOk());

    verify(this.producerUseCase, times(1)).createCar(eq(carPayload.toBuilder().sentAt(1L).build()));
  }

  @Test
  public void givenACarPayloadInvalidWhenInvokeUpdateCarThenReturnResponseEntity() throws Exception {

    final MvcResult result = this.mockMvc.perform(put("/producer/car").contentType(MediaType.APPLICATION_JSON).content("{}")).andDo(print())
        .andExpect(status().isBadRequest()).andReturn();

    final String contentAsString = result.getResponse().getContentAsString();

    assertThat(contentAsString, is(notNullValue()));
    final ErrorPayload errorPayload = this.objectMapper.readValue(contentAsString, ErrorPayload.class);
    assertThat(errorPayload.getFieldErrors(), is(hasSize(2)));
  }

  @Test
  public void givenACarPayloadValidWhenInvokeUpdateCarThenReturnResponseEntity() throws Exception {
    final CarPayload carPayload = CarPayload.builder().id("id").name("name").build();

    when(this.clock.millis()).thenReturn(5L);

    this.mockMvc
        .perform(put("/producer/car").contentType(MediaType.APPLICATION_JSON).content(this.objectMapper.writeValueAsString(carPayload)))
        .andDo(print()).andExpect(status().isOk());

    verify(this.producerUseCase).updateCar(eq(carPayload.toBuilder().sentAt(5L).build()));
  }


}