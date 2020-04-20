package com.xabe.spring.cloud.stream.consumer.infrastructure.presentation;

import com.xabe.spring.cloud.stream.consumer.domain.entity.CarDO;
import com.xabe.spring.cloud.stream.consumer.infrastructure.application.ConsumerUseCase;
import com.xabe.spring.cloud.stream.consumer.infrastructure.presentation.payload.CarPayload;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/consumer")
@RequiredArgsConstructor
public class ConsumerController {

  private final Logger logger;

  private final ConsumerUseCase consumerUseCase;

  @GetMapping
  public ResponseEntity getCars() {
    this.logger.info("get all events consumer");
    return ResponseEntity.ok(this.consumerUseCase.getCars().stream().map(this::mapper).collect(Collectors.toList()));
  }

  private CarPayload mapper(final CarDO carDO) {
    return CarPayload.builder().sentAt(carDO.getSentAt()).id(carDO.getId()).name(carDO.getName()).build();
  }
}
