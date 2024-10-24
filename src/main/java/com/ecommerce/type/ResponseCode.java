package com.ecommerce.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCode {

  SUCCESS("정상적으로 처리 되었습니다."),
  DATABASE_ERROR("데이터베이스 오류 입니다."),
  VALIDATION_FAIL("데이터가 유효하지 않습니다."),
  CERTIFICATION_FAIL("인증에 실패했습니다."),

  INVALID_REQUEST("잘못된 요청입니다."),
  INTERNAL_SERVER_ERROR("내부 서버 오류가 발생했습니다."),

  USER_NOT_FOUND("사용자가 존재하지 않습니다."),
  PASSWORD_UNMATCHED("비밀번호가 일치하지 않습니다."),
  USER_ALREADY_EXISTS("이미 존재하는 사용자 입니다."),
  MAIL_SEND_FAIL("메일 전송을 실패했습니다."),
  SIGN_IN_FAIL("로그인을 실패했습니다."),

  REDIS_DATA_NOT_FOUND("해당 Key 값의 데이터가 존재하지 않습니다."),

  REVIEW_NOT_FOUND("리뷰가 존재하지 않습니다."),
  REVIEW_ALREADY_EXISTS("리뷰가 이미 존재 합니다."),
  REVIEW_NO_AUTHORIZATION("리뷰 작성할 권한이 없습니다.");

  private final String description;

}
