package com.ecommerce.service.member;

import com.ecommerce.dto.ResponseDto;
import com.ecommerce.dto.member.MemberDto;
import com.ecommerce.dto.member.UpdateMemberDto;
import com.ecommerce.entity.Member;
import com.ecommerce.exception.MemberException;
import com.ecommerce.repository.MemberRepository;
import com.ecommerce.type.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberServiceImplement implements MemberService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * 회원 정보 조회
   *
   * @param memberId
   * @return MemberDto
   */
  @Override
  public MemberDto getMemberDetails(String memberId) {

    return MemberDto.fromEntity(getMemberByMemberId(memberId));

  }

  /**
   * 회원 정보 수정
   *
   * @param memberId
   * @param request
   * @return MemberDto
   */
  @Override
  @Transactional
  public MemberDto updateMember(String memberId, UpdateMemberDto request) {

    Member member = getMemberByMemberId(memberId);

    if (request.getPassword() != null && !request.getPassword().isEmpty()) {
      String encodedPassword = passwordEncoder.encode(request.getPassword());
      member.setPassword(encodedPassword);
    }

    if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
      member.setPhoneNumber(request.getPhoneNumber());
    }

    if (request.getAddress() != null && !request.getAddress().isEmpty()) {
      member.setAddress(request.getAddress());
    }

    return MemberDto.fromEntity(member);

  }

  /**
   * 회원 정보 삭제
   *
   * @param memberId
   * @return ResponseDto
   */
  @Override
  @Transactional
  public ResponseDto deleteMember(String memberId) {

    memberRepository.delete(getMemberByMemberId(memberId));

    return ResponseDto.getResponseBody(ResponseCode.MEMBER_DELETE_SUCCESS);

  }

  /**
   * Member Id 로 MemberRepository 에서 멤버 정보 조회
   *
   * @param memberId
   * @return Member
   */
  @Override
  @Transactional(readOnly = true)
  public Member getMemberByMemberId(String memberId) {

    return memberRepository.findByMemberId(memberId)
        .orElseThrow(() -> new MemberException(ResponseCode.MEMBER_NOT_FOUND));

  }

}
