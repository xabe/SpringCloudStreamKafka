package com.xabe.spring.cloud.stream.consumer.infrastructure.messaging;

import org.apache.avro.specific.SpecificRecord;

public interface EventHandler<T extends SpecificRecord> {

  void handle(T paylooad);

}
