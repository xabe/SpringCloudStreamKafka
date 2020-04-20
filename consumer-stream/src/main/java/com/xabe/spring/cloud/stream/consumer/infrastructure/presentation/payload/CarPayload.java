package com.xabe.spring.cloud.stream.consumer.infrastructure.presentation.payload;

import com.xabe.spring.cloud.stream.consumer.domain.entity.CarDO;
import javax.validation.constraints.NotNull;
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
public class CarPayload {

  @NotNull
  private String id;

  @NotNull
  private String name;

  private Long sentAt;

  @Builder(builderMethodName = "builder")
  public static CarPayloadBuilder builder(final CarDO carDO) {
    final CarPayloadBuilder carDTOBuilder = new CarPayloadBuilder();
    carDTOBuilder.name = carDO.getName();
    carDTOBuilder.id = carDO.getId();
    carDTOBuilder.sentAt = carDO.getSentAt();
    return carDTOBuilder;
  }

  @Builder(builderMethodName = "builder")
  public static CarPayloadBuilder builder() {
    return new CarPayloadBuilder();
  }
}
