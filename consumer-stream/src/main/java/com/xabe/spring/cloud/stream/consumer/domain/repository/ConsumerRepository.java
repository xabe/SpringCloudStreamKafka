package com.xabe.spring.cloud.stream.consumer.domain.repository;

import com.xabe.spring.cloud.stream.consumer.domain.entity.CarDO;
import java.util.List;

public interface ConsumerRepository {

  List<CarDO> getCarDOS();

  void addCar(CarDO carDO);

  void updateCar(CarDO carDO);

  void deleteCar(CarDO carDO);

  void clean();
}
