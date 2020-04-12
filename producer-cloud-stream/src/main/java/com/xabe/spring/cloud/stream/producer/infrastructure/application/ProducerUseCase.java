package com.xabe.spring.cloud.stream.producer.infrastructure.application;

import com.xabe.spring.cloud.stream.producer.infrastructure.persentation.payload.CarPayload;

public interface ProducerUseCase {

  void createCar(CarPayload carPayload);

  void updateCar(CarPayload carPayload);

  void deleteCar(CarPayload carPayload);
}
