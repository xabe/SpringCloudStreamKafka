package com.xabe.spring.cloud.stream.consumer.infrastructure.config;

import com.xabe.avro.v1.CarCreated;
import com.xabe.avro.v1.CarDeleted;
import com.xabe.avro.v1.CarUpdated;
import com.xabe.spring.cloud.stream.consumer.domain.exception.BusinessException;
import com.xabe.spring.cloud.stream.consumer.domain.repository.ConsumerRepository;
import com.xabe.spring.cloud.stream.consumer.infrastructure.messaging.CarMapper;
import com.xabe.spring.cloud.stream.consumer.infrastructure.messaging.CarProcessor;
import com.xabe.spring.cloud.stream.consumer.infrastructure.messaging.EventHandler;
import com.xabe.spring.cloud.stream.consumer.infrastructure.messaging.PipeStreams;
import com.xabe.spring.cloud.stream.consumer.infrastructure.messaging.SimpleEventHandler;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.schema.registry.client.CachingRegistryClient;
import org.springframework.cloud.schema.registry.client.ConfluentSchemaRegistryClient;
import org.springframework.cloud.schema.registry.client.SchemaRegistryClient;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;

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

  @Bean
  public IntegrationFlow buildErrorChannelFlow(final CarProcessor carProcessor) {
    return IntegrationFlows.from("car.v1.car.errors").filter(carProcessor::filterMessageForDlq)
        .transform(carProcessor::transformMessageForDlq).channel(PipeStreams.PRODUCER_CAR_DLQ_OUT).get();
  }

  @Bean
  public IntegrationFlow buildErrorDlqChannelFlow(final CarProcessor carProcessor) {
    //topic.group.errors channel de errors
    return IntegrationFlows.from("car.dlq.v1.carDlq.errors").filter(carProcessor::filterMessageForDlq)
        .transform(carProcessor::transformMessageForDlq).channel(PipeStreams.PRODUCER_CAR_DLQ_OUT).get();
  }

  @Bean
  public CircuitBreakerRegistry circuitBreaker() {
    final CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom().failureRateThreshold(20L)
        .enableAutomaticTransitionFromOpenToHalfOpen().ignoreExceptions(BusinessException.class)
        .slidingWindowType(SlidingWindowType.COUNT_BASED).slidingWindowSize(100).minimumNumberOfCalls(10)
        .permittedNumberOfCallsInHalfOpenState(100).waitDurationInOpenState(Duration.ofSeconds(15)).build();
    return CircuitBreakerRegistry.of(circuitBreakerConfig);
  }

  @Bean
  public SchemaRegistryClient schemaRegistryClient(@Value("${spring.cloud.stream.schemaRegistryClient.endpoint}") String endpoint) {
    final ConfluentSchemaRegistryClient confluentSchemaRegistryClient = new ConfluentSchemaRegistryClient();
    confluentSchemaRegistryClient.setEndpoint(endpoint);
    return new CachingRegistryClient(confluentSchemaRegistryClient);
  }

}
