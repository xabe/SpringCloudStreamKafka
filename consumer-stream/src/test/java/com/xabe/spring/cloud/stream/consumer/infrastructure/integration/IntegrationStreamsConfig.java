package com.xabe.spring.cloud.stream.consumer.infrastructure.integration;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBinding(IntegrationStreams.class)
public class IntegrationStreamsConfig {

}
