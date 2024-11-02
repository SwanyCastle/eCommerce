package com.ecommerce.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class NaverUserInfoDto {

  @Getter
  @Setter
  public static class NaverResponse {

    @JsonProperty("resultcode")
    private String resultCode;
    private String message;
    private UserInfoResponse response;

  }

  @Getter
  @Setter
  public static class UserInfoResponse {

    private String id;
    private String email;
    private String name;

  }

}
