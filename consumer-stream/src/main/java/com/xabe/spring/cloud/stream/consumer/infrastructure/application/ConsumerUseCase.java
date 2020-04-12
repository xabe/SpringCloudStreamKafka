package com.xabe.spring.cloud.stream.consumer.infrastructure.application;

import com.xabe.spring.cloud.stream.consumer.domain.entity.CarDO;
import java.util.List;

public interface ConsumerUseCase {

  List<CarDO> getCars();

}
