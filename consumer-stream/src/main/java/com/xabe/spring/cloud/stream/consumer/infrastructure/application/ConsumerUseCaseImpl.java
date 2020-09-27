package com.xabe.spring.cloud.stream.consumer.infrastructure.application;

import com.xabe.spring.cloud.stream.consumer.domain.entity.CarDO;
import com.xabe.spring.cloud.stream.consumer.domain.repository.ConsumerRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConsumerUseCaseImpl implements ConsumerUseCase {

  private final ConsumerRepository consumerRepository;

  @Override
  public List<CarDO> getCars() {
    return this.consumerRepository.getCarDOS();
  }
}
