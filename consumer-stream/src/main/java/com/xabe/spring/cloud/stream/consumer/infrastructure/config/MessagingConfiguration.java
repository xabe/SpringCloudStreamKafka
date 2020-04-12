package com.xabe.spring.cloud.stream.consumer.infrastructure.config;

import com.xabe.avro.v1.CarCreated;
import com.xabe.avro.v1.CarDeleted;
import com.xabe.avro.v1.CarUpdated;
import com.xabe.spring.cloud.stream.consumer.domain.repository.ConsumerRepository;
import com.xabe.spring.cloud.stream.consumer.infrastructure.messaging.CarMapper;
import com.xabe.spring.cloud.stream.consumer.infrastructure.messaging.EventHandler;
import com.xabe.spring.cloud.stream.consumer.infrastructure.messaging.PipeStreams;
import com.xabe.spring.cloud.stream.consumer.infrastructure.messaging.SimpleEventHandler;
import java.util.HashMap;
import java.util.Map;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBinding({PipeStreams.class})
public class MessagingConfiguration {

  @Bean("carHandlers")
  public Map<Class, EventHandler> carEventHandlers(final CarMapper carMapper, final ConsumerRepository consumerRepository) {
    final Map<Class, EventHandler> eventHandlers = new HashMap<>();
    eventHandlers.put(CarCreated.class, new SimpleEventHandler<>(carMapper::toCarCreateCarDTO, consumerRepository::addCar));
    eventHandlers.put(CarUpdated.class, new SimpleEventHandler<>(carMapper::toCarUpdateCarDTO, consumerRepository::updateCar));
    eventHandlers.put(CarDeleted.class, new SimpleEventHandler<>(carMapper::toCarDeleteCarDTO, consumerRepository::deleteCar));
    return eventHandlers;
  }

}
