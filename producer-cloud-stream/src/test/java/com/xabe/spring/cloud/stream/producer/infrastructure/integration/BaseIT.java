package com.xabe.spring.cloud.stream.producer.infrastructure.integration;

import com.xabe.spring.cloud.stream.producer.App;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import kong.unirest.Unirest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(classes = App.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public abstract class BaseIT {

  @LocalServerPort
  protected int serverPort;

  @BeforeAll
  public static void createMapping() throws IOException, InterruptedException {
    TimeUnit.SECONDS.sleep(2L);
    final InputStream car = EventsProcessingIT.class.getClassLoader().getResourceAsStream("avro-car.json");
    Unirest.post(UrlUtil.getInstance().getSchemaRegistryCar()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(IOUtils.toString(car, StandardCharsets.UTF_8)).asJson();

  }

}
