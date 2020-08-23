package com.xabe.spring.cloud.stream.consumer.infrastructure.persistence;

import com.xabe.spring.cloud.stream.consumer.domain.entity.CarDO;
import com.xabe.spring.cloud.stream.consumer.domain.repository.ConsumerRepository;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InMemoryConsumerRepository implements ConsumerRepository {

  private final Logger logger;

  private final AtomicInteger error = new AtomicInteger(0);

  private final LinkedList<CarDO> carDOS = new LinkedList<>();

  @Override
  public List<CarDO> getCarDOS() {
    this.logger.info("Get cars size {}", this.carDOS.size());
    return Collections.unmodifiableList(this.carDOS);
  }

  @Override
  public void addCar(final CarDO carDO) {
    if ("error".equalsIgnoreCase(carDO.getId())) {
      if (this.error.getAndIncrement() < 3) {
        this.logger.error("Error to add car {}", this.error.get());
        throw new RuntimeException();
      }
    }
    this.error.set(0);
    this.carDOS.add(carDO);
    this.logger.info("Add car {} size {}", carDO, this.carDOS.size());
  }

  @Override
  public void updateCar(final CarDO carDO) {
    this.logger.info("update car {}", carDO);
    this.carDOS.stream().filter(carDTO1 -> carDTO1.getId().equalsIgnoreCase(carDO.getId())).findFirst().ifPresent(item -> {
      this.carDOS.remove(item);
      this.carDOS.add(carDO);
      this.logger.info("update car {} size {}", carDO, this.carDOS.size());
    });
  }

  @Override
  public void deleteCar(final CarDO carDO) {
    this.carDOS.stream().filter(carDTO1 -> carDTO1.getId().equalsIgnoreCase(carDO.getId())).findFirst().ifPresent(this.carDOS::remove);
    this.logger.info("delete car {}", carDO);
  }

  @Override
  public void clean() {
    this.carDOS.clear();
  }
}
