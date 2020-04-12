package com.xabe.spring.cloud.stream.producer.infrastructure.persentation;

import com.xabe.spring.cloud.stream.producer.infrastructure.application.ProducerUseCase;
import com.xabe.spring.cloud.stream.producer.infrastructure.persentation.payload.CarPayload;
import java.time.Clock;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/producer/car")
@RequiredArgsConstructor
public class ProducerController {

  public static final String DELETE = "delete";

  private final Logger logger;

  private final Clock clock;

  private final ProducerUseCase producerUseCase;

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity createCar(@Valid @RequestBody final CarPayload carPayload) {
    this.producerUseCase.createCar(carPayload.toBuilder().sentAt(this.clock.millis()).build());
    this.logger.info("Create carPayload {}", carPayload);
    return ResponseEntity.ok().build();
  }

  @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity updateCar(@Valid @RequestBody final CarPayload carPayload) {
    this.producerUseCase.updateCar(carPayload.toBuilder().sentAt(this.clock.millis()).build());
    this.logger.info("Update carPayload {}", carPayload);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping(value = "/{id}")
  public ResponseEntity deleteCar(final @PathVariable String id) {
    final CarPayload carPayload = CarPayload.builder().id(id).name(DELETE).sentAt(this.clock.millis()).build();
    this.producerUseCase.deleteCar(carPayload);
    this.logger.info("Delete carPayload {}", carPayload);
    return ResponseEntity.ok().build();
  }
}
