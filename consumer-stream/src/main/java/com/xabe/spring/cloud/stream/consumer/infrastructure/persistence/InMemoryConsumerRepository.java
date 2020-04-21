package com.xabe.spring.cloud.stream.consumer.infrastructure.persistence;

import com.xabe.spring.cloud.stream.consumer.domain.entity.CarDO;
import com.xabe.spring.cloud.stream.consumer.domain.repository.ConsumerRepository;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InMemoryConsumerRepository implements ConsumerRepository {

  private final Logger logger;

  private final LinkedList<CarDO> carDOS = new LinkedList<>();

  @Override
  public List<CarDO> getCarDOS() {
    return Collections.unmodifiableList(this.carDOS);
  }

  @Override
  public void addCar(final CarDO carDO) {
    this.logger.info("Add car {}", carDO);
    this.carDOS.add(carDO);
  }

  @Override
  public void updateCar(final CarDO carDO) {
    this.logger.info("update car {}", carDO);
    this.carDOS.stream().filter(carDTO1 -> carDTO1.getId().equalsIgnoreCase(carDO.getId())).findFirst().ifPresent(item -> {
      this.carDOS.remove(item);
      this.carDOS.add(carDO);
    });
  }

  @Override
  public void deleteCar(final CarDO carDO) {
    this.logger.info("delete car {}", carDO);
    this.carDOS.stream().filter(carDTO1 -> carDTO1.getId().equalsIgnoreCase(carDO.getId())).findFirst().ifPresent(this.carDOS::remove);
  }

  @Override
  public void clean() {
    this.carDOS.clear();
  }
}
