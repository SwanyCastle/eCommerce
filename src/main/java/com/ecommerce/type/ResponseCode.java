package com.ecommerce.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCode {

  DATABASE_ERROR("데이터베이스 오류 입니다."),
  VALIDATION_FAIL("데이터가 유효하지 않습니다."),
  NO_PERMISSION("접근 권한이 없습니다."),
  INVALID_REQUEST("잘못된 요청입니다."),
  INTERNAL_SERVER_ERROR("내부 서버 오류가 발생했습니다."),

  CERTIFICATION_NUMBER_FAIL("인증되지 않았습니다. 정확한 인증번호를 입력해 주세요."),
  CERTIFICATION_NUMBER_SUCCESS("정상적으로 인증되었습니다."),

  MEMBER_ID_AVAILABLE("사용 가능한 사용자 ID 입니다."),
  MEMBER_NOT_FOUND("사용자가 존재하지 않습니다."),
  MEMBER_DELETE_SUCCESS("사용자 정보를 정상적으로 삭제 했습니다."),
  MEMBER_PASSWORD_UNMATCHED("비밀번호가 일치하지 않습니다."),
  MEMBER_ALREADY_EXISTS("이미 존재하는 사용자 입니다."),
  MEMBER_UNMATCHED("사용자 정보가 일치하지 않습니다."),
  SIGN_IN_FAIL("로그인을 실패했습니다."),
  SIGN_OUT_SUCCESS("성공적으로 로그아웃 되었습니다."),

  MAIL_SEND_SUCCESS("메일이 정상적으로 전송되었습니다."),
  MAIL_SEND_FAIL("메일 전송을 실패했습니다."),
  MAIL_CERTIFICATION_DOSE_NOT_EXISTS("이메일 인증이 진행되지 않았습니다. 이메일 인증을 진행해 주세요."),

  REDIS_DATA_NOT_FOUND("해당 Key 값의 데이터가 존재하지 않습니다."),

  PRODUCT_NOT_FOUND("상품이 존재하지 않습니다."),
  PRODUCT_DELETE_SUCCESS("상품을 정상적으로 삭제했습니다."),
  PRODUCT_DISABLE("비활성화된 상품 입니다."),

  CART_NOT_FOUND("장바구니가 존재하지 않습니다."),
  CART_ITEM_CANNOT_ADDED_PRODUCT("장바구니에 추가할수 없는 상품입니다."),
  CART_ITEM_ADDED("상품이 장바구니에 추가 되었습니다."),
  CART_ITEM_ALREADY_EXISTS("장바구니에 이미 존재하는 상품입니다."),
  CART_ITEM_NOT_FOUND("장바구니에 존재하지 않는 상품입니다."),
  CART_ITEM_EXCEED_QUANTITY("상품의 재고 수량 보다 많은 수량을 추가 할 수 없습니다."),
  CART_ITEM_DELETE_SUCCESS("장바구니의 상품 성공적으로 삭제 되었습니다."),

  REVIEW_NOT_FOUND("리뷰가 존재하지 않습니다."),
  REVIEW_ALREADY_EXISTS("리뷰가 이미 존재 합니다."),
  REVIEW_UNMATCHED_MEMBER("요청한 멤버와 작성자가 일치하지 않습니다."),
  REVIEW_DELETE_SUCCESS("리뷰를 정상적으로 삭제했습니다."),

  UNSUPPORTED_OAUTH_PROVIDER("지원되지 않는 OAuth 입니다.");

  private final String description;

}
