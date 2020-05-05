package com.xabe.spring.cloud.stream.consumer.domain.exception;

public class BusinessException extends RuntimeException {

  public BusinessException(final RuntimeException e) {
    super(e);
  }

  public BusinessException(final String message) {
    super(message);
  }
}
