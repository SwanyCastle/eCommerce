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
import com.ecommerce.dto.auth.SignInDto;
import com.ecommerce.dto.auth.SignUpDto;
import com.ecommerce.dto.member.MemberDto;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.Member;
import com.ecommerce.exception.CertificationException;
import com.ecommerce.exception.DataBaseException;
import com.ecommerce.exception.EmailException;
import com.ecommerce.exception.MemberException;
import com.ecommerce.provider.EmailProvider;
import com.ecommerce.provider.JwtProvider;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.MemberRepository;
import com.ecommerce.service.redis.RedisService;
import com.ecommerce.type.LoginType;
import com.ecommerce.type.ResponseCode;
import com.ecommerce.type.Role;
import java.util.Optional;
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
  private MemberRepository memberRepository;

  @Mock
  private CartRepository cartRepository;

  @Mock
  private EmailProvider emailProvider;

  @Mock
  private JwtProvider jwtProvider;

  @Mock
  private RedisService redisService;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Test
  @DisplayName("사용자 아이디 중복 확인 - 중복X")
  void testIdDuplicateCheck_UserNotExists() {
    // given
    IdDuplicateCheckDto.Request request = IdDuplicateCheckDto.Request.builder()
        .memberId("testUser")
        .build();

    given(memberRepository.existsByMemberId("testUser")).willReturn(false);

    // when
    ResponseDto response = authServiceImplement.idDuplicateCheck(request);

    // then
    verify(memberRepository, times(1))
        .existsByMemberId(eq("testUser"));

    assertEquals(ResponseCode.MEMBER_ID_AVAILABLE, response.getCode());
  }

  @Test
  @DisplayName("사용자 아이디 중복 확인 - 중복")
  void testIdDuplicateCheck_UserExists() {
    // given
    IdDuplicateCheckDto.Request request = IdDuplicateCheckDto.Request.builder()
        .memberId("existsUser")
        .build();

    given(memberRepository.existsByMemberId("existsUser")).willReturn(true);

    // when
    MemberException memberException = assertThrows(MemberException.class,
        () -> authServiceImplement.idDuplicateCheck(request));

    // then
    verify(memberRepository, times(1))
        .existsByMemberId(eq("existsUser"));

    assertThat(memberException.getErrorCode()).isEqualTo(ResponseCode.MEMBER_ALREADY_EXISTS);
  }

  @Test
  @DisplayName("이메일 전송 테스트 - 성공")
  void testEmailCertification_Success() {
    // given
    EmailCertificationDto.Request request = EmailCertificationDto.Request.builder()
        .memberId("testUser")
        .email("test@email.com")
        .build();

    given(memberRepository.existsByMemberId("testUser")).willReturn(false);
    given(emailProvider.sendCertificationMail(eq("test@email.com"), anyString()))
        .willReturn(true);
    willDoNothing().given(redisService)
        .saveDataWithTTL(eq("testUser"), anyString(), eq(3L), eq(TimeUnit.MINUTES));

    // when
    ResponseDto response = authServiceImplement.emailCertification(request);

    // then
    verify(memberRepository, times(1)).existsByMemberId("testUser");
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
        .memberId("existsUser")
        .email("test@email.com")
        .build();

    given(memberRepository.existsByMemberId("existsUser")).willReturn(true);

    // when
    MemberException memberException = assertThrows(MemberException.class,
        () -> authServiceImplement.emailCertification(request));

    // then
    verify(memberRepository, times(1)).existsByMemberId("existsUser");

    assertThat(memberException.getErrorCode()).isEqualTo(ResponseCode.MEMBER_ALREADY_EXISTS);
  }

  @Test
  @DisplayName("이메일 전송 테스트 - 실패 (메일 전송 실패)")
  void testEmailCertification_Fail_MailSendFail() {
    // given
    EmailCertificationDto.Request request = EmailCertificationDto.Request.builder()
        .memberId("existsUser")
        .email("test@email.com")
        .build();

    given(memberRepository.existsByMemberId("existsUser")).willReturn(false);
    given(emailProvider.sendCertificationMail(eq("test@email.com"), anyString()))
        .willReturn(false);

    // when
    EmailException emailException = assertThrows(EmailException.class,
        () -> authServiceImplement.emailCertification(request));

    // then
    verify(memberRepository, times(1)).existsByMemberId("existsUser");
    verify(emailProvider, times(1))
        .sendCertificationMail(eq("test@email.com"), anyString());

    assertThat(emailException.getErrorCode()).isEqualTo(ResponseCode.MAIL_SEND_FAIL);
  }

  @Test
  @DisplayName("인증번호 확인 - 성공")
  void testCheckCertification_Success() {
    // given
    CheckCertificationDto.Request request = CheckCertificationDto.Request.builder()
        .memberId("testUser")
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
        .memberId("testUser")
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

    assertThat(certificationException.getErrorCode()).isEqualTo(
        ResponseCode.CERTIFICATION_NUMBER_FAIL);
  }

  @Test
  @DisplayName("회원가입 - 성공")
  void testSignUp_Success() {
    // given
    SignUpDto.Request request = SignUpDto.Request.builder()
        .memberId("testUser")
        .memberName("test")
        .email("test@email.com")
        .password("testPassword")
        .phoneNumber("01011112222")
        .address("test시 test구 test로 111")
        .role(Role.CUSTOMER)
        .build();

    Member member = Member.builder()
        .memberId("testUser")
        .memberName("test")
        .email("test@email.com")
        .password("encodedPassword")
        .phoneNumber("01011112222")
        .address("test시 test구 test로 111")
        .role(Role.CUSTOMER)
        .loginType(LoginType.APP)
        .build();

    Cart cart = Cart.builder().member(member).build();

    given(memberRepository.existsByMemberId("testUser")).willReturn(false);
    given(redisService.checkVerified("testUser:verified")).willReturn(true);
    given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");

    given(memberRepository.save(any(Member.class))).willReturn(member);
    given(cartRepository.save(any(Cart.class))).willReturn(cart);

    ArgumentCaptor<Member> userCaptor = ArgumentCaptor.forClass(Member.class);

    // when
    MemberDto signedUpMember = authServiceImplement.signUp(request);

    // then
    verify(memberRepository, times(1)).existsByMemberId("testUser");
    verify(redisService, times(1))
        .checkVerified(eq("testUser:verified"));
    verify(passwordEncoder, times(1)).encode(eq("testPassword"));
    verify(memberRepository, times(1)).save(userCaptor.capture());
    verify(cartRepository, times(1)).save(any(Cart.class));

    assertThat(userCaptor.getValue()).isNotNull();
// ArgumentCaptor 가 캡쳐 하는 시점이 userRepository.save() 함수에 인자값을 전달하는
// 시기에 캡쳐하는거라 id 값은 가져 올수 없다.
//    assertThat(userCaptor.getValue().getId()).isEqualTo(1L);
    assertThat(userCaptor.getValue().getMemberId()).isEqualTo("testUser");
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
        .memberId("testUser")
        .memberName("test")
        .email("test@email.com")
        .password("testPassword")
        .phoneNumber("01011112222")
        .address("test시 test구 test로 111")
        .role(Role.CUSTOMER)
        .build();

    given(memberRepository.existsByMemberId("testUser")).willReturn(true);

    // when
    MemberException memberException = assertThrows(MemberException.class,
        () -> authServiceImplement.signUp(request));

    // then
    verify(memberRepository, times(1)).existsByMemberId("testUser");

    assertThat(memberException.getErrorCode()).isEqualTo(ResponseCode.MEMBER_ALREADY_EXISTS);
  }

  @Test
  @DisplayName("회원가입 - 실패 (이메일 인증 X)")
  void testSignUp_Fail_DidNotCertification() {
    // given
    SignUpDto.Request request = SignUpDto.Request.builder()
        .memberId("testUser")
        .memberName("test")
        .email("test@email.com")
        .password("testPassword")
        .phoneNumber("01011112222")
        .address("test시 test구 test로 111")
        .role(Role.CUSTOMER)
        .build();

    given(memberRepository.existsByMemberId("testUser")).willReturn(false);
    given(redisService.checkVerified("testUser:verified")).willReturn(false);

    // when
    CertificationException certificationException = assertThrows(CertificationException.class,
        () -> authServiceImplement.signUp(request));

    // then
    verify(memberRepository, times(1)).existsByMemberId("testUser");
    verify(redisService, times(1))
        .checkVerified(eq("testUser:verified"));

    assertThat(certificationException.getErrorCode())
        .isEqualTo(ResponseCode.MAIL_CERTIFICATION_DOSE_NOT_EXISTS);
  }

  @Test
  @DisplayName("회원가입 - 실패 (User 엔티티 저장 실패)")
  void testSignUp_Fail_UserSaveError() {
    // given
    SignUpDto.Request request = SignUpDto.Request.builder()
        .memberId("testUser")
        .memberName("test")
        .email("test@email.com")
        .password("testPassword")
        .phoneNumber("01011112222")
        .address("test시 test구 test로 111")
        .role(Role.CUSTOMER)
        .build();

    given(memberRepository.existsByMemberId("testUser")).willReturn(false);
    given(redisService.checkVerified("testUser:verified")).willReturn(true);
    given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");

    doThrow(new RuntimeException("DataBase 저장 오류"))
        .when(memberRepository).save(any(Member.class));

    // when
    DataBaseException dataBaseException = assertThrows(DataBaseException.class,
        () -> authServiceImplement.signUp(request));

    // then
    verify(memberRepository, times(1)).existsByMemberId("testUser");
    verify(redisService, times(1))
        .checkVerified(eq("testUser:verified"));
    verify(passwordEncoder, times(1)).encode(eq("testPassword"));

    assertThat(dataBaseException.getErrorCode()).isEqualTo(ResponseCode.DATABASE_ERROR);
  }

  @Test
  @DisplayName("로그인 - 성공")
  void testSignIn_Success() {
    // given
    SignInDto.Request request = SignInDto.Request.builder()
        .memberId("testUser")
        .password("testPassword")
        .build();

    Member member = Member.builder()
        .memberId("testUser")
        .memberName("test")
        .email("test@email.com")
        .password("encodedPassword")
        .phoneNumber("01011112222")
        .address("test시 test구 test로 111")
        .role(Role.CUSTOMER)
        .loginType(LoginType.APP)
        .build();

    given(memberRepository.findByMemberId(eq("testUser")))
        .willReturn(Optional.ofNullable(member));
    given(passwordEncoder.matches(eq("testPassword"), eq("encodedPassword")))
        .willReturn(true);
    given(jwtProvider.createToken(eq("testUser"), eq(Role.CUSTOMER)))
        .willReturn("testAccessToken");

    willDoNothing().given(redisService)
        .saveDataWithTTL(eq("testUser"), eq("testAccessToken"), eq(1L), eq(TimeUnit.HOURS));

    // when
    SignInDto.Response response = authServiceImplement.signIn(request);

    // then
    verify(memberRepository, times(1))
        .findByMemberId(eq("testUser"));
    verify(passwordEncoder, times(1))
        .matches(eq("testPassword"), eq("encodedPassword"));
    verify(jwtProvider, times(1))
        .createToken(eq("testUser"), eq(Role.CUSTOMER));
    verify(redisService, times(1))
        .saveDataWithTTL(eq("testUser"), anyString(), eq(1L), eq(TimeUnit.HOURS));

    assertThat(response.getToken()).isEqualTo("testAccessToken");
  }

  @Test
  @DisplayName("로그인 - 실패 (존재하지 않는 멤버)")
  void testSignIn_Fail_MemberNotFound() {
    // given
    SignInDto.Request request = SignInDto.Request.builder()
        .memberId("testUser")
        .password("testPassword")
        .build();

    given(memberRepository.findByMemberId(eq("testUser")))
        .willReturn(Optional.empty());

    // when
    MemberException memberException = assertThrows(MemberException.class,
        () -> authServiceImplement.signIn(request));

    // then
    verify(memberRepository, times(1))
        .findByMemberId(eq("testUser"));

    assertThat(memberException.getErrorCode()).isEqualTo(ResponseCode.MEMBER_NOT_FOUND);
  }

  @Test
  @DisplayName("로그인 - 실패 (비밀번호 불일치)")
  void testSignIn_Fail_PasswordUnMatched() {
    // given
    SignInDto.Request request = SignInDto.Request.builder()
        .memberId("testUser")
        .password("testPassword")
        .build();

    Member member = Member.builder()
        .memberId("testUser")
        .memberName("test")
        .email("test@email.com")
        .password("encodedPassword")
        .phoneNumber("01011112222")
        .address("test시 test구 test로 111")
        .role(Role.CUSTOMER)
        .loginType(LoginType.APP)
        .build();

    given(memberRepository.findByMemberId(eq("testUser")))
        .willReturn(Optional.ofNullable(member));
    given(passwordEncoder.matches(eq("testPassword"), eq("encodedPassword")))
        .willReturn(false);

    // when
    MemberException memberException = assertThrows(MemberException.class,
        () -> authServiceImplement.signIn(request));

    // then
    verify(memberRepository, times(1))
        .findByMemberId(eq("testUser"));
    verify(passwordEncoder, times(1))
        .matches(eq("testPassword"), eq("encodedPassword"));

    assertThat(memberException.getErrorCode()).isEqualTo(ResponseCode.MEMBER_PASSWORD_UNMATCHED);
  }

  @Test
  @DisplayName("로그인 - 실패 (Redis 서버 에러)")
  void testSignIn_Fail_RedisServerError() {
    // given
    SignInDto.Request request = SignInDto.Request.builder()
        .memberId("testUser")
        .password("testPassword")
        .build();

    Member member = Member.builder()
        .memberId("testUser")
        .memberName("test")
        .email("test@email.com")
        .password("encodedPassword")
        .phoneNumber("01011112222")
        .address("test시 test구 test로 111")
        .role(Role.CUSTOMER)
        .loginType(LoginType.APP)
        .build();

    given(memberRepository.findByMemberId(eq("testUser")))
        .willReturn(Optional.ofNullable(member));
    given(passwordEncoder.matches(eq("testPassword"), eq("encodedPassword")))
        .willReturn(true);
    given(jwtProvider.createToken(eq("testUser"), eq(Role.CUSTOMER)))
        .willReturn("testAccessToken");

    doThrow(new DataBaseException(ResponseCode.DATABASE_ERROR)).when(redisService)
        .saveDataWithTTL(eq("testUser"), eq("testAccessToken"), eq(1L), eq(TimeUnit.HOURS));

    // when
    DataBaseException dataBaseException = assertThrows(DataBaseException.class,
        () -> authServiceImplement.signIn(request));

    // then
    verify(memberRepository, times(1))
        .findByMemberId(eq("testUser"));
    verify(passwordEncoder, times(1))
        .matches(eq("testPassword"), eq("encodedPassword"));
    verify(jwtProvider, times(1))
        .createToken(eq("testUser"), eq(Role.CUSTOMER));
    verify(redisService, times(1))
        .saveDataWithTTL(eq("testUser"), anyString(), eq(1L), eq(TimeUnit.HOURS));

    assertThat(dataBaseException.getErrorCode()).isEqualTo(ResponseCode.DATABASE_ERROR);
  }

  @Test
  @DisplayName("로그아웃 - 성공")
  void testSignOut_Success() {
    // given
    given(jwtProvider.equalMemberId(eq("testUser"), eq("accessToken")))
        .willReturn(true);

    willDoNothing().given(redisService).deleteToken(eq("testUser"));

    // when
    ResponseDto responseDto = authServiceImplement.signOut("testUser", "accessToken");

    // then
    verify(jwtProvider, times(1))
        .equalMemberId(eq("testUser"), eq("accessToken"));
    verify(redisService, times(1))
        .deleteToken(eq("testUser"));

    assertThat(responseDto.getCode()).isEqualTo(ResponseCode.SIGN_OUT_SUCCESS);
  }

  @Test
  @DisplayName("로그아웃 - 실패 (토큰에 있는 멤버 정보와 불일치)")
  void testSignOut_Fail_MemberUnMatched() {
    // given
    given(jwtProvider.equalMemberId(eq("testUser"), eq("accessToken")))
        .willReturn(false);

    // when
    MemberException memberException = assertThrows(MemberException.class,
        () -> authServiceImplement.signOut("testUser", "accessToken"));

    // then
    verify(jwtProvider, times(1))
        .equalMemberId(eq("testUser"), eq("accessToken"));

    assertThat(memberException.getErrorCode()).isEqualTo(ResponseCode.MEMBER_UNMATCHED);
  }

  @Test
  @DisplayName("로그아웃 - 실패 (Redis 서버 에러)")
  void testSignOut_Fail_RedisServerError() {
    // given
    given(jwtProvider.equalMemberId(eq("testUser"), eq("accessToken")))
        .willReturn(true);

    doThrow(new DataBaseException(ResponseCode.DATABASE_ERROR))
        .when(redisService).deleteToken(eq("testUser"));

    // when
    DataBaseException dataBaseException = assertThrows(DataBaseException.class,
        () -> authServiceImplement.signOut("testUser", "accessToken"));

    // then
    verify(jwtProvider, times(1))
        .equalMemberId(eq("testUser"), eq("accessToken"));
    verify(redisService, times(1))
        .deleteToken(eq("testUser"));

    assertThat(dataBaseException.getErrorCode()).isEqualTo(ResponseCode.DATABASE_ERROR);
  }

}
