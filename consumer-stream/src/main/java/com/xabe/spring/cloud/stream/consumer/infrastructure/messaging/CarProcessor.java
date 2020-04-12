package com.xabe.spring.cloud.stream.consumer.infrastructure.messaging;

import com.xabe.avro.v1.MessageEnvelope;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecord;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CarProcessor {

  private final Logger logger;

  @Qualifier("carHandlers")
  private final Map<Class, EventHandler> handlers;

  @StreamListener(PipeStreams.CONSUMER_CAR_IN)
  public void processCarEvent(final MessageEnvelope message) {
    this.logger.info("Consumer message {}", message);
    final Class<?> msgClass = message.getPayload().getClass();
    final SpecificRecord payload = SpecificRecord.class.cast(message.getPayload());
    final EventHandler handler = this.handlers.get(msgClass);
    if (handler == null) {
      this.logger.warn("Received a non supported message. Type: {}, toString: {}", msgClass.getName(), payload.toString());
    } else {
      handler.handle(payload);
    }
  }


}
