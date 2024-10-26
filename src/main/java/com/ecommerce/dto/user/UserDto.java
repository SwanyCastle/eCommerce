package com.ecommerce.dto.user;

import com.ecommerce.entity.User;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

  private String userId;
  private String email;

  public static UserDto fromEntity(User user) {
    return UserDto.builder()
        .userId(user.getUserId())
        .email(user.getEmail())
        .build();
  }

}
