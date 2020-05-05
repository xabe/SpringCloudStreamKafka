package com.xabe.spring.cloud.stream.consumer.infrastructure.messaging;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface PipeStreams {

  String CONSUMER_CAR_IN = "consumer-car-in";
  String CONSUMER_CAR_DLQ_IN = "consumer-car-dlq-in";
  String PRODUCER_CAR_DLQ_OUT = "producer-car-dlq-out";

  @Input(CONSUMER_CAR_IN)
  SubscribableChannel carInputChannel();

  @Input(CONSUMER_CAR_DLQ_IN)
  SubscribableChannel carDlqInputChannel();

  @Output(PRODUCER_CAR_DLQ_OUT)
  @Qualifier("carDlqOutputChannel")
  MessageChannel carDlqOutputChannel();

}
