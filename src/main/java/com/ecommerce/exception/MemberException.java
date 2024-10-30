package com.ecommerce.exception;

import com.ecommerce.type.ResponseCode;
import lombok.Getter;

@Getter
public class MemberException extends RuntimeException {

  private final ResponseCode errorCode;
  private final String errorMessage;

  public MemberException(ResponseCode errorCode) {
    this.errorCode = errorCode;
    this.errorMessage = errorCode.getDescription();
  }

}
