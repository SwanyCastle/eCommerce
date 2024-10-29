package com.ecommerce.dto.auth;

import com.ecommerce.entity.User;
import com.ecommerce.type.LoginType;
import com.ecommerce.type.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class SignUpDto {

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Request {

    @NotBlank
    private String userId;

    @NotBlank
    private String userName;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]{8,13}$")
    private String password;

    private String phoneNumber;
    private String address;

    @NotNull
    private Role role;

    public static User toEntity(SignUpDto.Request request, String encodedPassword) {
      return User.builder()
          .userId(request.getUserId())
          .userName(request.getUserName())
          .email(request.getEmail())
          .password(encodedPassword)
          .phoneNumber(request.getPhoneNumber())
          .address(request.getAddress())
          .role(request.getRole())
          .loginType(LoginType.APP)
          .build();
    }

  }

}
