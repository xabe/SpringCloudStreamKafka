package com.xabe.spring.cloud.stream.consumer.infrastructure.messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationListener;
import org.springframework.kafka.event.ListenerContainerIdleEvent;

class ConsumerResumeListenerTest {

  private ApplicationListener<ListenerContainerIdleEvent> listener;

  @BeforeEach
  public void setUp() {
    this.listener = new ConsumerResumeListener();
  }

  @Test
  public void givenAListenerContainerIdleEventWhenInvokeOnApplicationEventNotResumeEvent() throws Exception {
    final Consumer consumer = spy(new MockConsumer(OffsetResetStrategy.EARLIEST));
    final ListenerContainerIdleEvent listenerContainerIdleEvent = new ListenerContainerIdleEvent(new Object(), new Object(), 1L, "id",
        Collections.emptyList(), consumer, false);

    this.listener.onApplicationEvent(listenerContainerIdleEvent);

    verify(consumer, never()).resume(any());
  }

  @Test
  public void givenAListenerContainerIdleEventWhenInvokeOnApplicationEventResumeEvent() throws Exception {
    final Consumer consumer = spy(new MockConsumer(OffsetResetStrategy.EARLIEST));
    when(consumer.paused()).thenReturn(Collections.singleton(new TopicPartition("myTopic", 0)));
    doNothing().when(consumer).resume(any());
    final ListenerContainerIdleEvent listenerContainerIdleEvent = new ListenerContainerIdleEvent(new Object(), new Object(), 1L, "id",
        Collections.emptyList(), consumer, false);

    this.listener.onApplicationEvent(listenerContainerIdleEvent);

    verify(consumer).resume(any());
  }

}