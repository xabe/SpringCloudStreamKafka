package com.xabe.spring.cloud.stream.consumer.infrastructure.presentation.payload;

import javax.validation.constraints.NotNull;

import com.xabe.spring.cloud.stream.consumer.domain.entity.CarDO;
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
public class CarDTO {

    @NotNull
    private String id;

    @NotNull
    private String name;

    private Long sentAt;

    @Builder(builderMethodName = "builder")
    public static CarDTOBuilder builder(final CarDO carDO) {
        final CarDTOBuilder carDTOBuilder = new CarDTOBuilder();
        carDTOBuilder.name = carDO.getName();
        carDTOBuilder.id = carDO.getId();
        carDTOBuilder.sentAt = carDO.getSentAt();
        return carDTOBuilder;
    }

    @Builder(builderMethodName = "builder")
    public static CarDTOBuilder builder() {
        return new CarDTOBuilder();
    }
}
