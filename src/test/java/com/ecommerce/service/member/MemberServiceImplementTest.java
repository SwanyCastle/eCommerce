package com.ecommerce.service.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ecommerce.dto.ResponseDto;
import com.ecommerce.dto.member.MemberDto;
import com.ecommerce.dto.member.UpdateMemberDto;
import com.ecommerce.entity.Member;
import com.ecommerce.exception.MemberException;
import com.ecommerce.repository.MemberRepository;
import com.ecommerce.service.auth.AuthService;
import com.ecommerce.type.LoginType;
import com.ecommerce.type.ResponseCode;
import com.ecommerce.type.Role;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplementTest {

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private AuthService authService;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private MemberServiceImplement memberServiceImplement;

  @Test
  @DisplayName("멤버 정보 조회 - 성공")
  void testGetMemberDetails_Success() {
    // given
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

    // when
    MemberDto foundMember = memberServiceImplement
        .getMemberDetails("testUser", "token");

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(memberRepository, times(1))
        .findByMemberId(eq("testUser"));

    assertThat(foundMember.getMemberId()).isEqualTo("testUser");
    assertThat(foundMember.getMemberName()).isEqualTo("test");
    assertThat(foundMember.getEmail()).isEqualTo("test@email.com");
    assertThat(foundMember.getPhoneNumber()).isEqualTo("01011112222");
    assertThat(foundMember.getAddress()).isEqualTo("test시 test구 test로 111");
    assertThat(foundMember.getRole()).isEqualTo(Role.CUSTOMER);
    assertThat(foundMember.getLoginType()).isEqualTo(LoginType.APP);
  }

  @Test
  @DisplayName("멤버 정보 조회 - 실패 (토큰에 있는 멤버 정보와 불일치)")
  void testGetMemberDetails_Fail_MemberUnMatched() {
    // given
    doThrow(new MemberException(ResponseCode.MEMBER_UNMATCHED))
        .when(authService).equalToMemberIdFromToken(eq("testUser"), eq("token"));

    // when
    MemberException memberException = assertThrows(MemberException.class,
        () -> memberServiceImplement.getMemberDetails("testUser", "token"));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    assertThat(memberException.getErrorCode()).isEqualTo(ResponseCode.MEMBER_UNMATCHED);
  }

  @Test
  @DisplayName("멤버 정보 조회 - 실패 (존재하지 않는 멤버)")
  void testGetMemberDetails_Fail_MemberNotFound() {
    // given
    given(memberRepository.findByMemberId(eq("testUser")))
        .willReturn(Optional.empty());

    // when
    MemberException memberException = assertThrows(MemberException.class,
        () -> memberServiceImplement.getMemberDetails("testUser", "token"));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(memberRepository, times(1))
        .findByMemberId(eq("testUser"));

    assertThat(memberException.getErrorCode()).isEqualTo(ResponseCode.MEMBER_NOT_FOUND);
  }

  @Test
  @DisplayName("멤버 정보 수정 - 성공")
  void testUpdateMember_Success() {
    // given
    UpdateMemberDto updateRequest = UpdateMemberDto.builder()
        .password("updatePassword")
        .address("업데이트된 주소")
        .phoneNumber("업데이트된 휴대폰 번호")
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

    // when
    MemberDto updateMember =
        memberServiceImplement.updateMember("testUser", updateRequest, "token");

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(passwordEncoder, times(1))
        .encode(eq("updatePassword"));
    verify(memberRepository, times(1))
        .findByMemberId(eq("testUser"));

    assertThat(updateMember.getMemberId()).isEqualTo("testUser");
    assertThat(updateMember.getMemberName()).isEqualTo("test");
    assertThat(updateMember.getEmail()).isEqualTo("test@email.com");
    assertThat(updateMember.getPhoneNumber()).isEqualTo("업데이트된 휴대폰 번호");
    assertThat(updateMember.getAddress()).isEqualTo("업데이트된 주소");
    assertThat(updateMember.getRole()).isEqualTo(Role.CUSTOMER);
    assertThat(updateMember.getLoginType()).isEqualTo(LoginType.APP);
  }

  @Test
  @DisplayName("멤버 정보 수정 - 실패 (토큰에 있는 멤버 정보와 불일치)")
  void testUpdateMember_Fail_MemberUnMatched() {
    // given
    UpdateMemberDto updateRequest = UpdateMemberDto.builder()
        .password("updatePassword")
        .address("업데이트된 주소")
        .phoneNumber("업데이트된 휴대폰 번호")
        .build();

    doThrow(new MemberException(ResponseCode.MEMBER_UNMATCHED))
        .when(authService).equalToMemberIdFromToken(eq("testUser"), eq("token"));

    // when
    MemberException memberException = assertThrows(MemberException.class,
        () -> memberServiceImplement.updateMember("testUser", updateRequest, "token"));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    assertThat(memberException.getErrorCode()).isEqualTo(ResponseCode.MEMBER_UNMATCHED);
  }

  @Test
  @DisplayName("멤버 정보 수정 - 실패 (존재하지 않는 멤버)")
  void testUpdateMember_Fail_MemberNotFound() {
    // given
    UpdateMemberDto updateRequest = UpdateMemberDto.builder()
        .password("updatePassword")
        .address("업데이트된 주소")
        .phoneNumber("업데이트된 휴대폰 번호")
        .build();

    given(memberRepository.findByMemberId(eq("testUser")))
        .willReturn(Optional.empty());

    // when
    MemberException memberException = assertThrows(MemberException.class,
        () -> memberServiceImplement.updateMember("testUser", updateRequest, "token"));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(memberRepository, times(1))
        .findByMemberId(eq("testUser"));

    assertThat(memberException.getErrorCode()).isEqualTo(ResponseCode.MEMBER_NOT_FOUND);
  }

  @Test
  @DisplayName("멤버 정보 삭제 - 성공")
  void testDeleteMember_Success() {
    // given
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
        .willReturn(Optional.of(member));

    // when
    ResponseDto responseDto = memberServiceImplement.deleteMember("testUser", "token");

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(memberRepository, times(1))
        .findByMemberId(eq("testUser"));
    verify(memberRepository, times(1))
        .delete(eq(member));

    assertThat(responseDto.getCode()).isEqualTo(ResponseCode.MEMBER_DELETE_SUCCESS);
  }

  @Test
  @DisplayName("멤버 정보 삭제 - 실패 (토큰에 있는 멤버 정보와 불일치)")
  void testDeleteMember_Fail_MemberUnMatched() {
    // given
    doThrow(new MemberException(ResponseCode.MEMBER_UNMATCHED))
        .when(authService).equalToMemberIdFromToken(eq("testUser"), eq("token"));

    // when
    MemberException memberException = assertThrows(MemberException.class,
        () -> memberServiceImplement.deleteMember("testUser", "token"));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    assertThat(memberException.getErrorCode()).isEqualTo(ResponseCode.MEMBER_UNMATCHED);
  }

  @Test
  @DisplayName("멤버 정보 삭제 - 실패 (존재하지 않는 멤버)")
  void testDeleteMember_Fail_MemberNotFound() {
    // given
    given(memberRepository.findByMemberId(eq("testUser")))
        .willReturn(Optional.empty());

    // when
    MemberException memberException = assertThrows(MemberException.class,
        () -> memberServiceImplement.deleteMember("testUser", "token"));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(memberRepository, times(1))
        .findByMemberId(eq("testUser"));

    assertThat(memberException.getErrorCode()).isEqualTo(ResponseCode.MEMBER_NOT_FOUND);
  }

}