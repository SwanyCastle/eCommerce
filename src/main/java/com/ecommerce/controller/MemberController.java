package com.ecommerce.controller;

import com.ecommerce.dto.ResponseDto;
import com.ecommerce.dto.member.MemberDto;
import com.ecommerce.dto.member.UpdateMemberDto;
import com.ecommerce.service.member.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

  private final MemberService memberService;

  /**
   * 회원 정보 조회
   *
   * @param memberId
   * @return ResponseEntity<MemberDto>
   */
  @GetMapping("/{memberId}")
  public ResponseEntity<MemberDto> getMemberDetails(@PathVariable String memberId) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(memberService.getMemberDetails(memberId));
  }

  /**
   * 회원 정보 수정
   *
   * @param memberId
   * @param updateRequest
   * @return ResponseEntity<MemberDto>
   */
  @PatchMapping("/{memberId}")
  public ResponseEntity<MemberDto> updateMember(
      @PathVariable String memberId,
      @RequestBody @Valid UpdateMemberDto updateRequest
  ) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(memberService.updateMember(memberId, updateRequest));
  }

  /**
   * 회원 정보 삭제
   *
   * @param memberId
   * @return ResponseEntity<ResponseDto>
   */
  @DeleteMapping("/{memberId}")
  public ResponseEntity<ResponseDto> deleteMember(
      @PathVariable String memberId
  ) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(memberService.deleteMember(memberId));
  }

}
