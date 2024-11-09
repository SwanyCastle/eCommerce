package com.ecommerce.exception;

import com.ecommerce.type.ResponseCode;
import lombok.Getter;

@Getter
public class ReviewException extends RuntimeException {

  private final ResponseCode errorCode;
  private final String errorMessage;

  public ReviewException(ResponseCode errorCode) {
    this.errorCode = errorCode;
    this.errorMessage = errorCode.getDescription();
  }

}
