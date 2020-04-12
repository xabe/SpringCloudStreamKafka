package com.xabe.spring.cloud.stream.producer.domain.repository;

import com.xabe.spring.cloud.stream.producer.domain.entity.CarDO;

public interface ProducerRepository {

  void saveCar(CarDO carDO);

  void updateCar(CarDO carDO);

  void deleteCar(CarDO carDO);
}
