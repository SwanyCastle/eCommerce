package com.ecommerce.exception;

import com.ecommerce.type.ResponseCode;
import lombok.Getter;

@Getter
public class RedisException extends RuntimeException {

  private final ResponseCode errorCode;
  private final String errorMessage;

  public RedisException(ResponseCode errorCode) {
    this.errorCode = errorCode;
    this.errorMessage = errorCode.getDescription();
  }

}
