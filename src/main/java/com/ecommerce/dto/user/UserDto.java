package com.ecommerce.dto.user;

import com.ecommerce.entity.User;
import com.ecommerce.type.LoginType;
import com.ecommerce.type.Role;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

  private Long id;
  private String userId;
  private String userName;
  private String email;
  private String phoneNumber;
  private String address;
  private Role role;
  private LoginType loginType;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static UserDto fromEntity(User user) {
    return UserDto.builder()
        .id(user.getId())
        .userId(user.getUserId())
        .userName(user.getUsername())
        .email(user.getEmail())
        .phoneNumber(user.getPhoneNumber())
        .address(user.getAddress())
        .role(user.getRole())
        .loginType(user.getLoginType())
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdateAt())
        .build();
  }

}
