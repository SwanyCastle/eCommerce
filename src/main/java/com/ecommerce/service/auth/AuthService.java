package com.ecommerce.service.auth;

import com.ecommerce.dto.ResponseDto;
import com.ecommerce.dto.auth.CheckCertificationDto;
import com.ecommerce.dto.auth.EmailCertificationDto;
import com.ecommerce.dto.auth.IdDuplicateCheckDto;
import com.ecommerce.dto.auth.SignUpDto;
import com.ecommerce.dto.user.UserDto;
import com.ecommerce.entity.User;
import org.springframework.http.ResponseEntity;

public interface AuthService {

  ResponseDto idDuplicateCheck(IdDuplicateCheckDto.Request request);

  ResponseDto emailCertification(
      EmailCertificationDto.Request request);

  ResponseDto checkCertification(
      CheckCertificationDto.Request request);

  UserDto signUp(SignUpDto.Request request);

  void checkExistsUserId(String userId);

}
