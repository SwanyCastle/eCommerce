package com.ecommerce.service.auth;

import com.ecommerce.dto.ResponseDto;
import com.ecommerce.dto.auth.CheckCertificationDto;
import com.ecommerce.dto.auth.EmailCertificationDto;
import com.ecommerce.dto.auth.IdDuplicateCheckDto;
import com.ecommerce.dto.auth.SignInDto;
import com.ecommerce.dto.auth.SignUpDto;
import com.ecommerce.dto.member.MemberDto;

public interface AuthService {

  ResponseDto idDuplicateCheck(IdDuplicateCheckDto.Request request);

  ResponseDto emailCertification(EmailCertificationDto.Request request);

  ResponseDto checkCertification(CheckCertificationDto.Request request);

  MemberDto signUp(SignUpDto.Request request);

  SignInDto.Response signIn(SignInDto.Request request);

  ResponseDto signOut(String memberId, String token);

  void checkExistsUserId(String memberId);

  void equalToMemberIdFromToken(String memberId, String token);

}
