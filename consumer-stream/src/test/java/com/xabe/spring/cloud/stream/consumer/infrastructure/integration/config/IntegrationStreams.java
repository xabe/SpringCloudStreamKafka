package com.xabe.spring.cloud.stream.consumer.infrastructure.integration.config;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface IntegrationStreams {

  String PRODUCER_CAR_OUT = "producer-car-out";

  @Output(PRODUCER_CAR_OUT)
  MessageChannel carOutputChannel();

}
