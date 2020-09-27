package com.xabe.spring.cloud.stream.consumer.infrastructure.config;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import java.util.Map;
import org.apache.avro.Schema;
import org.apache.kafka.common.errors.SerializationException;

public class SubjectNamingStrategy implements org.springframework.cloud.schema.registry.avro.SubjectNamingStrategy,
    io.confluent.kafka.serializers.subject.strategy.SubjectNameStrategy {

  private static final String SUBJECT_PROP = "subject";

  @Override
  public String toSubject(final Schema schema) {
    final String subject = schema.getProp(SUBJECT_PROP);

    if (subject == null) {
      throw new IllegalArgumentException("'" + SUBJECT_PROP + "' attribute is required for schema");
    }

    return subject;
  }

  @Override
  public String subjectName(final String topic, final boolean isKey, final ParsedSchema parsedSchema) {
    if (parsedSchema != null) {
      return parsedSchema.name();
    } else if (isKey) {
      return "key_" + topic;
    } else {
      throw new SerializationException("In configuration value.subject.name.strategy = "
          + this.getClass().getName() + ", the message value must only be an Avro record schema");
    }
  }

  @Override
  public void configure(final Map<String, ?> map) {

  }
}
