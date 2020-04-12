package com.xabe.spring.cloud.stream.producer.infrastructure.integration;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface IntegrationStreams {

  String CONSUMER_CAR_IN = "consumer-car-in";

  @Input(CONSUMER_CAR_IN)
  SubscribableChannel carInputChannel();

}
