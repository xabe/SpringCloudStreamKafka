package com.xabe.spring.cloud.stream.producer.infrastructure.integration.config;

import static java.lang.String.format;

public final class UrlUtil {

  private static final String SCHEMA_REGISTRY_CAR = "http://%s:%s/subjects/car.v1-value/versions";

  private static final UrlUtil INSTANCE = new UrlUtil();

  private final String urlSchemaRegistryCar;

  private UrlUtil() {
    final String registryHost = System.getProperty("schemaregistry.host", "localhost");
    final String registryPort = System.getProperty("schemaregistry.port", "8081");
    this.urlSchemaRegistryCar = format(SCHEMA_REGISTRY_CAR, registryHost, registryPort);
  }

  public static UrlUtil getInstance() {
    return INSTANCE;
  }

  public String getSchemaRegistryCar() {
    return this.urlSchemaRegistryCar;
  }
}
