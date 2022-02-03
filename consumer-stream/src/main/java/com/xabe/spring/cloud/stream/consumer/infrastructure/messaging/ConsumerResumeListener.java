package com.xabe.spring.cloud.stream.consumer.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.kafka.event.ListenerContainerIdleEvent;
import org.springframework.stereotype.Component;

@Component
public class ConsumerResumeListener implements ApplicationListener<ListenerContainerIdleEvent> {

  private final Logger logger;

  public ConsumerResumeListener() {
    this.logger = LoggerFactory.getLogger(this.getClass());
  }

  @Override
  public void onApplicationEvent(final ListenerContainerIdleEvent event) {
    this.logger.info("Is paused consumer: {}, partitions: {} event: {}", !event.getConsumer().paused().isEmpty(),
        event.getConsumer().paused(), event);
    if (!event.getConsumer().paused().isEmpty()) {
      this.logger.info("Resuming paused partitions {}", event);
      event.getConsumer().resume(event.getConsumer().paused());
    }
  }
}
