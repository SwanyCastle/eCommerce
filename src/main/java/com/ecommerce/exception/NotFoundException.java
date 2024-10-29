package com.ecommerce.exception;

import com.ecommerce.type.ResponseCode;
import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {

  private final ResponseCode errorCode;
  private final String errorMessage;

  public NotFoundException(ResponseCode errorCode) {
    this.errorCode = errorCode;
    this.errorMessage = errorCode.getDescription();
  }

}
