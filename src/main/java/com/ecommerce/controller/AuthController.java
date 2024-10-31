package com.ecommerce.controller;

import com.ecommerce.dto.ResponseDto;
import com.ecommerce.dto.auth.CheckCertificationDto;
import com.ecommerce.dto.auth.EmailCertificationDto;
import com.ecommerce.dto.auth.IdDuplicateCheckDto;
import com.ecommerce.dto.auth.SignInDto;
import com.ecommerce.dto.auth.SignUpDto;
import com.ecommerce.dto.member.MemberDto;
import com.ecommerce.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  /**
   * 사용자 ID
   * @param request
   * @return ResponseEntity<ResponseDto>
   */
  @PostMapping("/id-check")
  public ResponseEntity<ResponseDto> idCheck(
      @RequestBody @Valid IdDuplicateCheckDto.Request request
  ) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(authService.idDuplicateCheck(request));
  }

  /**
   * 이메일 인증
   * @param request
   * @return ResponseEntity<ResponseDto>
   */
  @PostMapping("/email-certification")
  public ResponseEntity<ResponseDto> enailCertification(
      @RequestBody @Valid EmailCertificationDto.Request request
  ) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(authService.emailCertification(request));
  }

  /**
   * 인증번호 확인
   * @param request
   * @return ResponseEntity<ResponseDto>
   */
  @PostMapping("/check-certification")
  public ResponseEntity<ResponseDto> checkCertification(
      @RequestBody @Valid CheckCertificationDto.Request request
  ) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(authService.checkCertification(request));
  }

  /**
   * 회원가입
   * @param request
   * @return UserDto
   */
  @PostMapping("/sign-up")
  public ResponseEntity<MemberDto> signUp(
      @RequestBody @Valid SignUpDto.Request request
  ) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(authService.signUp(request));
  }

  /**
   * 로그인
   * @param requestBody
   * @return ResponseEntity<SignInDto.Response>
   */
  @PostMapping("/sign-in")
  public ResponseEntity<SignInDto.Response> signIn(
      @RequestBody @Valid SignInDto.Request requestBody
  ) {
    return ResponseEntity.status(HttpStatus.OK).body(authService.signIn(requestBody));
  }

  /**
   * 로그아웃
   * @param memberId
   * @return ResponseEntity<ResponseDto>
   */
  @PostMapping("/sign-out/{memberId}")
  public ResponseEntity<ResponseDto> signOut(
      @PathVariable String memberId,
      @RequestHeader("Authorization") String token
  ) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(authService.signOut(memberId, token));
  }

}
