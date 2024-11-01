package com.ecommerce.dto.member;

import com.ecommerce.entity.Member;
import com.ecommerce.type.LoginType;
import com.ecommerce.type.Role;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDto {

  private Long id;
  private String memberId;
  private String memberName;
  private String email;
  private String phoneNumber;
  private String address;
  private Role role;
  private LoginType loginType;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static MemberDto fromEntity(Member member) {
    return MemberDto.builder()
        .id(member.getId())
        .memberId(member.getMemberId())
        .memberName(member.getMemberName())
        .email(member.getEmail())
        .phoneNumber(member.getPhoneNumber())
        .address(member.getAddress())
        .role(member.getRole())
        .loginType(member.getLoginType())
        .createdAt(member.getCreatedAt())
        .updatedAt(member.getUpdateAt())
        .build();
  }

}
