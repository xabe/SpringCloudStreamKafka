package com.xabe.spring.cloud.stream.producer.infrastructure.config;

import com.xabe.spring.cloud.stream.producer.infrastructure.messaging.PipeStreams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.schema.registry.client.CachingRegistryClient;
import org.springframework.cloud.schema.registry.client.ConfluentSchemaRegistryClient;
import org.springframework.cloud.schema.registry.client.SchemaRegistryClient;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBinding({PipeStreams.class})
public class MessagingConfiguration {

    @Bean
    public SchemaRegistryClient schemaRegistryClient(@Value("${spring.cloud.stream.schemaRegistryClient.endpoint}") String endpoint) {
        final ConfluentSchemaRegistryClient confluentSchemaRegistryClient = new ConfluentSchemaRegistryClient();
        confluentSchemaRegistryClient.setEndpoint(endpoint);
        return new CachingRegistryClient(confluentSchemaRegistryClient);
    }

}
