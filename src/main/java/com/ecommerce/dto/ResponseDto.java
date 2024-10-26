package com.ecommerce.dto;

import com.ecommerce.type.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ResponseDto {

  private ResponseCode code;
  private String message;

  public static ResponseDto getResponseBody(ResponseCode code) {
    return ResponseDto.builder()
        .code(code)
        .message(code.getDescription())
        .build();
  }

}
