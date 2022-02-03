package com.xabe.spring.cloud.stream.consumer.infrastructure.config;

import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.apache.kafka.common.errors.SerializationException;

public class SubjectNamingStrategy implements org.springframework.cloud.schema.registry.avro.SubjectNamingStrategy,
    io.confluent.kafka.serializers.subject.strategy.SubjectNameStrategy<Schema> {

  private static final String SUBJECT_PROP = "subject";

  @Override
  public String subjectName(final String topic, final boolean isKey, final Schema schema) {
    if (schema != null && schema.getType() == Type.RECORD) {
      return schema.getProp(SUBJECT_PROP);
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

  @Override
  public String toSubject(final String subjectNamePrefix, final Schema schema) {
    final String subject = schema.getProp(SUBJECT_PROP);

    if (subject == null) {
      throw new IllegalArgumentException("'" + SUBJECT_PROP + "' attribute is required for schema");
    }

    return subject;
  }
}
