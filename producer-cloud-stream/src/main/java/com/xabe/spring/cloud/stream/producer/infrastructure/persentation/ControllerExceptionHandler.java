package com.xabe.spring.cloud.stream.producer.infrastructure.persentation;

import com.xabe.spring.cloud.stream.producer.infrastructure.persentation.payload.ErrorPayload;
import com.xabe.spring.cloud.stream.producer.infrastructure.persentation.payload.ErrorPayload.ErrorPayloadBuilder;
import com.xabe.spring.cloud.stream.producer.infrastructure.persentation.payload.ErrorPayload.FieldValidationErrorPayload;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ControllerExceptionHandler {

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseBody
  public ErrorPayload handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
    final ErrorPayloadBuilder builder = ErrorPayload.builder();
    final List<FieldValidationErrorPayload> result = e.getBindingResult().getFieldErrors().stream()
        .map(fieldError -> FieldValidationErrorPayload.builder(fieldError.getField(), fieldError.getDefaultMessage()).build())
        .collect(Collectors.toList());
    return builder.fieldErrors(result).build();
  }

}
