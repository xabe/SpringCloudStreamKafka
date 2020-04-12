package com.xabe.spring.cloud.stream.consumer.infrastructure.messaging;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface PipeStreams {

  String CONSUMER_CAR_IN = "consumer-car-in";

  @Input(CONSUMER_CAR_IN)
  SubscribableChannel carInputChannel();

}
