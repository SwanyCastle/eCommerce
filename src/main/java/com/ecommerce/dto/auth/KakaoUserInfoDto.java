package com.ecommerce.dto.auth;

import lombok.Getter;
import lombok.Setter;

public class KakaoUserInfoDto {

  @Getter
  @Setter
  public static class KakaoResponse {

    private Long id;
    private String connected_at;
    private Properties properties;
    private KakaoAccount kakao_account;

  }

  @Getter
  @Setter
  public static class Properties {

    private String nickname;

  }

  @Getter
  @Setter
  public static class KakaoAccount {

    private Boolean profile_nickname_needs_agreement;
    private KakaoProfile profile;
    private Boolean has_email;
    private Boolean email_needs_agreement;
    private Boolean is_email_valid;
    private Boolean is_email_verified;
    private String email;

  }

  @Getter
  @Setter
  public static class KakaoProfile {

    private String nickname;
    private Boolean is_default_nickname;

  }
}
