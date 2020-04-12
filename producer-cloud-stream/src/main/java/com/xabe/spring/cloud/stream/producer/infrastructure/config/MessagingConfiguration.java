package com.xabe.spring.cloud.stream.producer.infrastructure.config;

import com.xabe.spring.cloud.stream.producer.infrastructure.messaging.PipeStreams;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBinding({PipeStreams.class})
public class MessagingConfiguration {

}
