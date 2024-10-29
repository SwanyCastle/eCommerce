package com.ecommerce.config;

import com.ecommerce.dto.ResponseDto;
import com.ecommerce.filter.JwtAuthenticationFilter;
import com.ecommerce.type.ResponseCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configurable
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final FailedAuthenticationEntryPoint failedAuthenticationEntryPoint;

  /**
   * Security Filter Chain 설정
   * @param httpSecurity
   * @return SecurityFilterChain
   * @throws Exception
   */
  @Bean
  protected SecurityFilterChain configure(HttpSecurity httpSecurity) throws Exception {

    httpSecurity
        .cors(cors -> cors
            .configurationSource(corsConfigurationSorce())
        )
        .csrf(CsrfConfigurer::disable)
        .httpBasic(HttpBasicConfigurer::disable)
        .sessionManagement(sessionManagement -> sessionManagement
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .authorizeHttpRequests(request -> request
            .requestMatchers("/", "/api/v1/auth/**", "/oauth2/**").permitAll()
            .anyRequest().authenticated()
        )
        .exceptionHandling(exceptionHandling -> exceptionHandling
            .authenticationEntryPoint(failedAuthenticationEntryPoint)
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return httpSecurity.build();

  }

  /**
   * Cors 정책 설정
   * @return CorsConfigurationSource
   */
  @Bean
  protected CorsConfigurationSource corsConfigurationSorce() {

    CorsConfiguration corsConfigurationV1 = new CorsConfiguration();
    corsConfigurationV1.addAllowedOrigin("*");
    corsConfigurationV1.addAllowedMethod("*");
    corsConfigurationV1.addAllowedHeader("*");

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/v1/**", corsConfigurationV1);

    return source;
  }

  /**
   * 사용자 비밀번호 암호화를 위한 인코더 설정
   * @return PasswordEncoder
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

}

@Component
@RequiredArgsConstructor
class FailedAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  /**
   * 접근 권한이 없거나 접근 실패시 에러 처리
   * @param request
   * @param response
   * @param authException
   * @throws IOException
   * @throws ServletException
   */
  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException
  ) throws IOException, ServletException {

    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/json; charset=UTF-8");
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);

    response.getWriter().write(
        objectMapper.writeValueAsString(ResponseDto.getResponseBody(ResponseCode.NO_PERMISSION))
    );

  }

}