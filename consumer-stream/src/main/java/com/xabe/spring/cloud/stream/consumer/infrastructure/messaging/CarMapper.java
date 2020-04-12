package com.xabe.spring.cloud.stream.consumer.infrastructure.messaging;

import com.xabe.avro.v1.CarCreated;
import com.xabe.avro.v1.CarDeleted;
import com.xabe.avro.v1.CarUpdated;
import com.xabe.spring.cloud.stream.consumer.domain.entity.CarDO;

public interface CarMapper {

  CarDO toCarCreateCarDTO(CarCreated carCreated);

  CarDO toCarUpdateCarDTO(CarUpdated carUpdated);

  CarDO toCarDeleteCarDTO(CarDeleted carDeleted);
}
