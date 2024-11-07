package com.ecommerce.exception.handler;

import com.ecommerce.dto.ResponseDto;
import com.ecommerce.exception.CartException;
import com.ecommerce.exception.CertificationException;
import com.ecommerce.exception.DataBaseException;
import com.ecommerce.exception.EmailException;
import com.ecommerce.exception.MemberException;
import com.ecommerce.exception.NotFoundException;
import com.ecommerce.exception.ProductException;
import com.ecommerce.type.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
  public ResponseEntity<ResponseDto> methodArgumentNotValidExceptionHandler(Exception e) {
    log.error("{} 에러가 발생했습니다. (validation)", e.getClass());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ResponseDto.getResponseBody(ResponseCode.VALIDATION_FAIL));
  }

  @ExceptionHandler(MemberException.class)
  public ResponseEntity<ResponseDto> userExceptionHandler(MemberException e) {
    log.error("{} 에러가 발생했습니다. (user)", e.getErrorCode());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ResponseDto.getResponseBody(e.getErrorCode()));
  }

  @ExceptionHandler(CertificationException.class)
  public ResponseEntity<ResponseDto> certificationExceptionHandler(CertificationException e) {
    log.error("{} 에러가 발생했습니다. (certification)", e.getErrorCode());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ResponseDto.getResponseBody(e.getErrorCode()));
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ResponseDto> notFoundExceptionHandler(NotFoundException e) {
    log.error("{} 에러가 발생했습니다. (redis)", e.getErrorCode());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ResponseDto.getResponseBody(e.getErrorCode()));
  }

  @ExceptionHandler(EmailException.class)
  public ResponseEntity<ResponseDto> emailExceptionHandler(EmailException e) {
    log.error("{} 에러가 발생했습니다. (email)", e.getErrorCode());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ResponseDto.getResponseBody(e.getErrorCode()));
  }

  @ExceptionHandler(ProductException.class)
  public ResponseEntity<ResponseDto> productExceptionHandler(ProductException e) {
    log.error("{} 에러가 발생했습니다. (product)", e.getErrorCode());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ResponseDto.getResponseBody(e.getErrorCode()));
  }

  @ExceptionHandler(CartException.class)
  public ResponseEntity<ResponseDto> cartExceptionHandler(CartException e) {
    log.error("{} 에러가 발생했습니다. (cart)", e.getErrorCode());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ResponseDto.getResponseBody(e.getErrorCode()));
  }

  @ExceptionHandler(DataBaseException.class)
  public ResponseEntity<ResponseDto> dataBaseExceptionHandler(DataBaseException e) {
    log.error("{} 에러가 발생했습니다. (database)", e.getErrorCode());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ResponseDto.getResponseBody(e.getErrorCode()));
  }

  @ExceptionHandler(OAuth2AuthenticationException.class)
  public ResponseEntity<ResponseDto> oAuth2AuthenticationHandler(OAuth2AuthenticationException e) {
    log.error("{} 에러가 발생했습니다. (oauth)", e.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ResponseDto.getResponseBody(ResponseCode.UNSUPPORTED_OAUTH_PROVIDER));
  }

}
