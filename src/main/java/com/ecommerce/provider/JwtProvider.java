package com.ecommerce.provider;

import com.ecommerce.entity.Member;
import com.ecommerce.exception.MemberException;
import com.ecommerce.repository.MemberRepository;
import com.ecommerce.type.ResponseCode;
import com.ecommerce.type.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class JwtProvider {

  private static final long TOKEN_EXPIRE_TIME = 1000 * 60 * 60; // 1 hour

  private final MemberRepository memberRepository;

  @Value("${spring.jwt.secret}")
  private String secretKey;

  /**
   * 사용자 ID, 권한(Role) 정보를 포함한
   * JWT 토큰 생성
   * @param memberId
   * @return String
   */
  public String createToken(String memberId, Role role) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", role.name());

    return Jwts.builder()
        .signWith(SignatureAlgorithm.HS256, secretKey)
        .setClaims(claims)
        .setSubject(memberId)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRE_TIME))
        .compact();
  }

  /**
   * SecurityContextHolder 에 등록하기위한
   * 토큰에 있는 사용자 객체 및 권한 정보로
   * Authentication 객체 생성
   * @param jwt
   * @return Authentication
   */
  public Authentication getAuthentication(String jwt) {
    Member member = memberRepository.findByMemberId(getMemberId(jwt))
        .orElseThrow(() -> new MemberException(ResponseCode.MEMBER_NOT_FOUND));
    return new UsernamePasswordAuthenticationToken(member, null, member.getAuthorities());
  }

  /**
   * 토큰에 있는 사용자 ID 추출
   * @param token
   * @return String
   */
  public String getMemberId(String token) {
    return parseClaims(token).getSubject();
  }

  /**
   * 파싱한 토큰이 유효한지 검증
   * @param token
   * @return boolean
   */
  public boolean validateToken(String token) {
    if (!StringUtils.hasText(token)) {
      return false;
    }

    Claims claims = parseClaims(token);
    return !claims.getExpiration().before(new Date());
  }

  /**
   * 토큰 서명, 유효기간 검증 및
   * 파싱 (String -> Claims)
   * @param token
   * @return Claims
   */
  private Claims parseClaims(String token) {
    try {
      return Jwts.parserBuilder()
          .setSigningKey(secretKey)
          .build()
          .parseClaimsJws(token)
          .getBody();
    } catch (ExpiredJwtException e) {
      return e.getClaims();
    }
  }
}
