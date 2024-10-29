package com.ecommerce.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.doThrow;

import com.ecommerce.dto.ResponseDto;
import com.ecommerce.dto.auth.CheckCertificationDto;
import com.ecommerce.dto.auth.EmailCertificationDto;
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
import com.ecommerce.type.Role;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplementTest {

  @InjectMocks
  private AuthServiceImplement authServiceImplement;

  @Mock
  private UserRepository userRepository;

  @Mock
  private EmailProvider emailProvider;

  @Mock
  private RedisService redisService;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Test
  @DisplayName("사용자 아이디 중복 확인 - 중복X")
  void testIdDuplicateCheck_UserNotExists() {
    // given
    IdDuplicateCheckDto.Request request = IdDuplicateCheckDto.Request.builder()
        .userId("testUser")
        .build();

    given(userRepository.existsByUserId("testUser")).willReturn(false);

    // when
    ResponseDto response = authServiceImplement.idDuplicateCheck(request);

    // then
    verify(userRepository, times(1))
        .existsByUserId(eq("testUser"));

    assertEquals(ResponseCode.AVAILABLE_USER_ID, response.getCode());
  }

  @Test
  @DisplayName("사용자 아이디 중복 확인 - 중복")
  void testIdDuplicateCheck_UserExists() {
    // given
    IdDuplicateCheckDto.Request request = IdDuplicateCheckDto.Request.builder()
        .userId("existsUser")
        .build();

    given(userRepository.existsByUserId("existsUser")).willReturn(true);

    // when
    UserException userException = assertThrows(UserException.class,
        () -> authServiceImplement.idDuplicateCheck(request));

    // then
    verify(userRepository, times(1))
        .existsByUserId(eq("existsUser"));

    assertThat(userException.getErrorCode()).isEqualTo(ResponseCode.USER_ALREADY_EXISTS);
  }

  @Test
  @DisplayName("이메일 전송 테스트 - 성공")
  void testEmailCertification_Success() {
    // given
    EmailCertificationDto.Request request = EmailCertificationDto.Request.builder()
        .userId("testUser")
        .email("test@email.com")
        .build();

    given(userRepository.existsByUserId("testUser")).willReturn(false);
    given(emailProvider.sendCertificationMail(eq("test@email.com"), anyString()))
        .willReturn(true);
    willDoNothing().given(redisService)
        .saveDataWithTTL(eq("testUser"), anyString(), eq(3L), eq(TimeUnit.MINUTES));

    // when
    ResponseDto response = authServiceImplement.emailCertification(request);

    // then
    verify(userRepository, times(1)).existsByUserId("testUser");
    verify(emailProvider, times(1))
        .sendCertificationMail(eq("test@email.com"), anyString());
    verify(redisService, times(1))
        .saveDataWithTTL(eq("testUser"), anyString(), eq(3L), eq(TimeUnit.MINUTES));

    assertThat(response.getCode()).isEqualTo(ResponseCode.MAIL_SEND_SUCCESS);
  }

  @Test
  @DisplayName("이메일 전송 테스트 - 실패 (사용자 아이디 중복)")
  void testEmailCertification_Fail_UserExists() {
    // given
    EmailCertificationDto.Request request = EmailCertificationDto.Request.builder()
        .userId("existsUser")
        .email("test@email.com")
        .build();

    given(userRepository.existsByUserId("existsUser")).willReturn(true);

    // when
    UserException userException = assertThrows(UserException.class,
        () -> authServiceImplement.emailCertification(request));

    // then
    verify(userRepository, times(1)).existsByUserId("existsUser");

    assertThat(userException.getErrorCode()).isEqualTo(ResponseCode.USER_ALREADY_EXISTS);
  }

  @Test
  @DisplayName("이메일 전송 테스트 - 실패 (메일 전송 실패)")
  void testEmailCertification_Fail_MailSendFail() {
    // given
    EmailCertificationDto.Request request = EmailCertificationDto.Request.builder()
        .userId("existsUser")
        .email("test@email.com")
        .build();

    given(userRepository.existsByUserId("existsUser")).willReturn(false);
    given(emailProvider.sendCertificationMail(eq("test@email.com"), anyString()))
        .willReturn(false);

    // when
    EmailException emailException = assertThrows(EmailException.class,
        () -> authServiceImplement.emailCertification(request));

    // then
    verify(userRepository, times(1)).existsByUserId("existsUser");
    verify(emailProvider, times(1))
        .sendCertificationMail(eq("test@email.com"), anyString());

    assertThat(emailException.getErrorCode()).isEqualTo(ResponseCode.MAIL_SEND_FAIL);
  }

  @Test
  @DisplayName("인증번호 확인 - 성공")
  void testCheckCertification_Success() {
    // given
    CheckCertificationDto.Request request = CheckCertificationDto.Request.builder()
        .userId("testUser")
        .certificationNumber("1234")
        .build();

    given(redisService.verifyCertificationNumber(eq("testUser"), eq("1234")))
        .willReturn(true);

    // when
    ResponseDto response = authServiceImplement.checkCertification(request);

    // then
    verify(redisService, times(1))
        .verifyCertificationNumber(eq("testUser"), eq("1234"));

    assertThat(response.getCode()).isEqualTo(ResponseCode.CERTIFICATION_NUMBER_SUCCESS);
  }

  @Test
  @DisplayName("인증번호 확인 - 실패")
  void testCheckCertification_Fail() {
    // given
    CheckCertificationDto.Request request = CheckCertificationDto.Request.builder()
        .userId("testUser")
        .certificationNumber("1234")
        .build();

    given(redisService.verifyCertificationNumber(eq("testUser"), eq("1234")))
        .willReturn(false);

    // when
    CertificationException certificationException = assertThrows(CertificationException.class,
        () -> authServiceImplement.checkCertification(request));

    // then
    verify(redisService, times(1))
        .verifyCertificationNumber(eq("testUser"), eq("1234"));

    assertThat(certificationException.getErrorCode()).isEqualTo(ResponseCode.CERTIFICATION_NUMBER_FAIL);
  }

  @Test
  @DisplayName("회원가입 - 성공")
  void testSignUp_Success() {
    // given
    SignUpDto.Request request = SignUpDto.Request.builder()
        .userId("testUser")
        .userName("test")
        .email("test@email.com")
        .password("testPassword")
        .phoneNumber("01011112222")
        .address("test시 test구 test로 111")
        .role(Role.CUSTOMER)
        .build();

    given(userRepository.existsByUserId("testUser")).willReturn(false);
    given(redisService.checkVerified("testUser:verified")).willReturn(true);
    given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");

    given(userRepository.save(any(User.class))).willReturn(
        User.builder()
            .userId("testUser")
            .userName("test")
            .email("test@email.com")
            .password("encodedPassword")
            .phoneNumber("01011112222")
            .address("test시 test구 test로 111")
            .role(Role.CUSTOMER)
            .loginType(LoginType.APP)
            .build()
    );

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

    // when
    UserDto signedUpUser = authServiceImplement.signUp(request);

    // then
    verify(userRepository, times(1)).existsByUserId("testUser");
    verify(redisService, times(1))
        .checkVerified(eq("testUser:verified"));
    verify(passwordEncoder, times(1)).encode(eq("testPassword"));
    verify(userRepository, times(1)).save(userCaptor.capture());

    assertThat(userCaptor.getValue()).isNotNull();
// ArgumentCaptor 가 캡쳐 하는 시점이 userRepository.save() 함수에 인자값을 전달하는
// 시기에 캡쳐하는거라 id 값은 가져 올수 없다.
//    assertThat(userCaptor.getValue().getId()).isEqualTo(1L);
    assertThat(userCaptor.getValue().getUserId()).isEqualTo("testUser");
    assertThat(userCaptor.getValue().getUsername()).isEqualTo("test");
    assertThat(userCaptor.getValue().getEmail()).isEqualTo("test@email.com");
    assertThat(userCaptor.getValue().getPassword()).isEqualTo("encodedPassword");
    assertThat(userCaptor.getValue().getPhoneNumber()).isEqualTo("01011112222");
    assertThat(userCaptor.getValue().getAddress()).isEqualTo("test시 test구 test로 111");
    assertThat(userCaptor.getValue().getRole()).isEqualTo(Role.CUSTOMER);
    assertThat(userCaptor.getValue().getLoginType()).isEqualTo(LoginType.APP);
  }

  @Test
  @DisplayName("회원가입 - 실패 (사용자 ID 중복)")
  void testSignUp_Fail_UserIdExists() {
    // given
    SignUpDto.Request request = SignUpDto.Request.builder()
        .userId("testUser")
        .userName("test")
        .email("test@email.com")
        .password("testPassword")
        .phoneNumber("01011112222")
        .address("test시 test구 test로 111")
        .role(Role.CUSTOMER)
        .build();

    given(userRepository.existsByUserId("testUser")).willReturn(true);

    // when
    UserException userException = assertThrows(UserException.class,
        () -> authServiceImplement.signUp(request));

    // then
    verify(userRepository, times(1)).existsByUserId("testUser");

    assertThat(userException.getErrorCode()).isEqualTo(ResponseCode.USER_ALREADY_EXISTS);
  }

  @Test
  @DisplayName("회원가입 - 실패 (이메일 인증 X)")
  void testSignUp_Fail_DidNotCertification() {
    // given
    SignUpDto.Request request = SignUpDto.Request.builder()
        .userId("testUser")
        .userName("test")
        .email("test@email.com")
        .password("testPassword")
        .phoneNumber("01011112222")
        .address("test시 test구 test로 111")
        .role(Role.CUSTOMER)
        .build();

    given(userRepository.existsByUserId("testUser")).willReturn(false);
    given(redisService.checkVerified("testUser:verified")).willReturn(false);

    // when
    CertificationException certificationException = assertThrows(CertificationException.class,
        () -> authServiceImplement.signUp(request));

    // then
    verify(userRepository, times(1)).existsByUserId("testUser");
    verify(redisService, times(1))
        .checkVerified(eq("testUser:verified"));

    assertThat(certificationException.getErrorCode())
        .isEqualTo(ResponseCode.DOSE_NOT_EXISTS_CERTIFICATION);
  }

  @Test
  @DisplayName("회원가입 - 실패 (User 엔티티 저장 실패)")
  void testSignUp_Fail_UserSaveError() {
    // given
    SignUpDto.Request request = SignUpDto.Request.builder()
        .userId("testUser")
        .userName("test")
        .email("test@email.com")
        .password("testPassword")
        .phoneNumber("01011112222")
        .address("test시 test구 test로 111")
        .role(Role.CUSTOMER)
        .build();

    given(userRepository.existsByUserId("testUser")).willReturn(false);
    given(redisService.checkVerified("testUser:verified")).willReturn(true);
    given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");

    doThrow(new RuntimeException("DataBase 저장 오류"))
        .when(userRepository).save(any(User.class));

    // when
    DataBaseException dataBaseException = assertThrows(DataBaseException.class,
        () -> authServiceImplement.signUp(request));

    // then
    verify(userRepository, times(1)).existsByUserId("testUser");
    verify(redisService, times(1))
        .checkVerified(eq("testUser:verified"));
    verify(passwordEncoder, times(1)).encode(eq("testPassword"));

    assertThat(dataBaseException.getErrorCode()).isEqualTo(ResponseCode.DATABASE_ERROR);
  }
}
