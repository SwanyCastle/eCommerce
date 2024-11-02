package com.ecommerce.entity;

import java.util.Collection;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

@NoArgsConstructor
@AllArgsConstructor
public class CustomOAuth2Member implements OAuth2User {

  // 소셜 로그인한 사용자에대한 Ecommerce Application 의 토큰을 발급하기 위한
  // memberId 값 만 받아서 사용
  private String memberId;

  @Override
  public Map<String, Object> getAttributes() {
    return null;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return null;
  }

  @Override
  public String getName() {
    return this.memberId;
  }

}
