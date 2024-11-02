package com.ecommerce.utils;

import com.ecommerce.dto.auth.SignInDto;
import com.ecommerce.entity.CustomOAuth2Member;
import com.ecommerce.provider.JwtProvider;
import com.ecommerce.service.redis.RedisService;
import com.ecommerce.type.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JwtProvider jwtProvider;
  private final RedisService redisService;
  private final ObjectMapper objectMapper;

  /**
   * OAuth 2 Login 성공 후 Ecommerce Application 의 토큰 발급 처리 및 응답
   *
   * @param request
   * @param response
   * @param authentication
   * @throws IOException
   * @throws ServletException
   */
  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication
  ) throws IOException, ServletException {

    CustomOAuth2Member oAuth2User = (CustomOAuth2Member) authentication.getPrincipal();

    String userId = oAuth2User.getName();

    String token = jwtProvider.createToken(userId, Role.CUSTOMER);

    redisService.saveDataWithTTL(userId, token, 1L, TimeUnit.HOURS);

    SignInDto.Response signInResponse = SignInDto.Response.builder()
        .token(token)
        .build();

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.setStatus(HttpServletResponse.SC_OK);

    response.getWriter().write(
        objectMapper.writeValueAsString(signInResponse)
    );

  }

}
