package com.ecommerce.service.auth;

import com.ecommerce.dto.ResponseDto;
import com.ecommerce.dto.auth.CheckCertificationDto;
import com.ecommerce.dto.auth.EmailCertificationDto;
import com.ecommerce.dto.auth.IdDuplicateCheckDto;
import com.ecommerce.dto.auth.SignUpDto;
import com.ecommerce.dto.user.UserDto;
import com.ecommerce.exception.CertificationException;
import com.ecommerce.exception.DataBaseException;
import com.ecommerce.exception.EmailException;
import com.ecommerce.exception.UserException;
import com.ecommerce.provider.EmailProvider;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.redis.RedisService;
import com.ecommerce.type.ResponseCode;
import com.ecommerce.utils.CertificationNumber;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImplement implements AuthService {

  private final UserRepository userRepository;
  private final EmailProvider emailProvider;
  private final RedisService redisService;

  private final PasswordEncoder passwordEncoder;

  /**
   * 사용자 ID 중복 체크
   *
   * @param request
   * @return ResponseDto
   */
  @Override
  public ResponseDto idDuplicateCheck(IdDuplicateCheckDto.Request request) {

    checkExistsUserId(request.getUserId());

    return ResponseDto.getResponseBody(ResponseCode.AVAILABLE_USER_ID);

  }

  /**
   * 이메일 인증
   *
   * @param request
   * @return ResponseDto
   */
  @Override
  public ResponseDto emailCertification(EmailCertificationDto.Request request) {

    String userId = request.getUserId();

    checkExistsUserId(request.getUserId());

    String certificationNumber = CertificationNumber.getCertificationNumber();

    boolean isSucceed =
        emailProvider.sendCertificationMail(request.getEmail(), certificationNumber);
    if (!isSucceed) {
      throw new EmailException(ResponseCode.MAIL_SEND_FAIL);
    }

    // Redis 에 UserId 를 키값으로 CertificationNumber 를 저장. (유효시간 3분으로 설정)
    redisService.saveDataWithTTL(userId, certificationNumber, 3, TimeUnit.MINUTES);

    return ResponseDto.getResponseBody(ResponseCode.MAIL_SEND_SUCCESS);

  }

  /**
   * 인증번호 확인
   *
   * @param request
   * @return ResponseDto
   */
  @Override
  public ResponseDto checkCertification(CheckCertificationDto.Request request) {

    boolean isVerified = redisService.verifyCertificationNumber(
        request.getUserId(), request.getCertificationNumber()
    );
    if (!isVerified) {
      throw new CertificationException(ResponseCode.CERTIFICATION_NUMBER_FAIL);
    }

    return ResponseDto.getResponseBody(ResponseCode.CERTIFICATION_NUMBER_SUCCESS);

  }

  /**
   * 회원가입
   *
   * @param request
   * @return UserDto
   */
  @Override
  public UserDto signUp(SignUpDto.Request request) {

    String userId = request.getUserId();

    checkExistsUserId(request.getUserId());

    boolean isCheckVerified = redisService.checkVerified(userId + ":verified");
    if (!isCheckVerified) {
      throw new CertificationException(ResponseCode.DOSE_NOT_EXISTS_CERTIFICATION);
    }

    try {
      String encodedPassword = passwordEncoder.encode(request.getPassword());

      return UserDto.fromEntity(
          userRepository.save(SignUpDto.Request.toEntity(request, encodedPassword))
      );
    } catch (Exception e) {
      e.printStackTrace();
      throw new DataBaseException(ResponseCode.DATABASE_ERROR);
    }

  }

  @Override
  public void checkExistsUserId(String userId) {
    boolean isExists = userRepository.existsByUserId(userId);
    if (isExists) {
      throw new UserException(ResponseCode.USER_ALREADY_EXISTS);
    }
  }

}
