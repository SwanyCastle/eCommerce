package com.ecommerce.service.auth;

import com.ecommerce.dto.ResponseDto;
import com.ecommerce.dto.auth.CheckCertificationDto;
import com.ecommerce.dto.auth.EmailCertificationDto.Request;
import com.ecommerce.dto.auth.IdDuplicateCheckDto;
import com.ecommerce.dto.auth.SignUpDto;
import com.ecommerce.dto.user.UserDto;
import com.ecommerce.entity.User;
import com.ecommerce.exception.CertificationException;
import com.ecommerce.exception.DataBaseException;
import com.ecommerce.exception.EmailException;
import com.ecommerce.exception.UserException;
import com.ecommerce.provider.EmailProvider;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.redis.RedisService;
import com.ecommerce.type.LoginType;
import com.ecommerce.type.ResponseCode;
import com.ecommerce.utils.CertificationNumber;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImplement implements AuthService {

  private final UserRepository userRepository;
  private final EmailProvider emailProvider;
  private final RedisService redisService;

  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  /**
   * 사용자 ID 중복 체크
   *
   * @param request
   * @return ResponseEntity<ResponseDto>
   */
  @Override
  public ResponseEntity<ResponseDto> idDuplicateCheck(IdDuplicateCheckDto.Request request) {

    checkExistUserByUserId(request.getUserId());

    return ResponseEntity.status(HttpStatus.OK)
        .body(ResponseDto.getResponseBody(ResponseCode.SUCCESS));
  }

  /**
   * 이메일 인증
   *
   * @param request
   * @return ResponseEntity<ResponseDto>
   */
  @Override
  public ResponseEntity<ResponseDto> emailCertification(
      Request request) {

    String userId = request.getUserId();

    checkExistUserByUserId(userId);

    String certificationNumber = CertificationNumber.getCertificationNumber();

    boolean isSucceed =
        emailProvider.sendCertificationMail(request.getEmail(), certificationNumber);

    if (!isSucceed) {
      throw new EmailException(ResponseCode.MAIL_SEND_FAIL);
    }

    redisService.saveDataWithTTL(userId, certificationNumber, 1, TimeUnit.HOURS);

    return ResponseEntity.status(HttpStatus.OK)
        .body(ResponseDto.getResponseBody(ResponseCode.SUCCESS));

  }

  /**
   * 인증번호 확인
   *
   * @param request
   * @return ResponseEntity<ResponseDto>
   */
  @Override
  public ResponseEntity<ResponseDto> checkCertification(
      CheckCertificationDto.Request request) {

    boolean isVerified = redisService.verifyCertificationNumber(
        request.getUserId(),
        request.getCertificationNumber()
    );

    if (!isVerified) {
      throw new CertificationException(ResponseCode.CERTIFICATION_FAIL);
    }

    return ResponseEntity.status(HttpStatus.OK)
        .body(ResponseDto.getResponseBody(ResponseCode.SUCCESS));

  }

  /**
   * 회원가입
   *
   * @param request
   * @return ResponseEntity<ResponseDto>
   */
  @Override
  public UserDto signUp(SignUpDto.Request request) {

    String userId = request.getUserId();

    checkExistUserByUserId(userId);

    boolean isVerified = redisService.checkVerified(userId + ":verified");

    if (!isVerified) {
      throw new CertificationException(ResponseCode.CERTIFICATION_FAIL);
    }

    try {
      String encodedPassword = passwordEncoder.encode(request.getPassword());

      return UserDto.fromEntity(
          userRepository.save(
              User.builder()
                  .userId(request.getUserId())
                  .userName(request.getUserName())
                  .email(request.getEmail())
                  .password(encodedPassword)
                  .phoneNumber(request.getPhoneNumber())
                  .address(request.getAddress())
                  .role(request.getRole())
                  .loginType(LoginType.APP)
                  .build()
          )
      );
    } catch (Exception e) {
      e.printStackTrace();
      throw new DataBaseException(ResponseCode.DATABASE_ERROR);
    }

  }

  /**
   * 사용자 ID 를 가진 사용자가 있는지 중복 체크
   *
   * @param userId
   */
  private void checkExistUserByUserId(String userId) {
    if (userRepository.existsByUserId(userId)) {
      throw new UserException(ResponseCode.USER_ALREADY_EXISTS);
    }
  }

  /**
   * 사용자 ID 에 해당하는 사용자 조회
   *
   * @param userId
   * @return User
   */
  @Override
  public User getMemberByUserId(String userId) {
    return userRepository.findByUserId(userId)
        .orElseThrow(() -> new UserException(ResponseCode.USER_NOT_FOUND));
  }

}
