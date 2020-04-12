package com.xabe.spring.cloud.stream.producer.infrastructure.persentation.payload;

import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ErrorPayload {

  @Builder.Default
  private final List<FieldValidationErrorPayload> fieldErrors = Collections.emptyList();

  @Value
  @Builder(toBuilder = true)
  @EqualsAndHashCode
  @ToString
  @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  public static class FieldValidationErrorPayload {

    private final String field;

    private final String message;

    @Builder(builderMethodName = "builder")
    public static FieldValidationErrorPayloadBuilder builder(final String field, final String message) {
      final FieldValidationErrorPayloadBuilder carDTOBuilder = new FieldValidationErrorPayloadBuilder();
      carDTOBuilder.field = field;
      carDTOBuilder.message = message;
      return carDTOBuilder;
    }

  }
}
