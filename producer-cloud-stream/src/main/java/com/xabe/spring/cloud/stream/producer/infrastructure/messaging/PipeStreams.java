package com.xabe.spring.cloud.stream.producer.infrastructure.messaging;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface PipeStreams {

  String PRODUCER_CAR_OUT = "producer-car-out";

  @Output(PRODUCER_CAR_OUT)
  @Qualifier("carOutputChannel")
  MessageChannel carOutputChannel();

}
