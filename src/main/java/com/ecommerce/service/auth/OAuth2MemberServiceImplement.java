package com.ecommerce.service.auth;

import com.ecommerce.dto.auth.KakaoUserInfoDto.KakaoResponse;
import com.ecommerce.dto.auth.NaverUserInfoDto.NaverResponse;
import com.ecommerce.entity.CustomOAuth2Member;
import com.ecommerce.entity.Member;
import com.ecommerce.repository.MemberRepository;
import com.ecommerce.type.LoginType;
import com.ecommerce.type.ResponseCode;
import com.ecommerce.type.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuth2MemberServiceImplement extends DefaultOAuth2UserService {

  private final MemberRepository memberRepository;

  /**
   * OAuth Server 에 요청을 보낸 후 redirect 로 각 소셜 로그인으로 로그인한 유저의 정보를 받아 Ecommerce Application DB 에 저장후
   * Ecommerce Application 의 토큰 생성을 위해 CustomOAuth2Member 객체로 반환
   *
   * @param request
   * @return CustomOAuth2Member
   * @throws OAuth2AuthenticationException
   */
  @Override
  public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {

    OAuth2User oAuth2User = super.loadUser(request);
    String oauthClientName = request.getClientRegistration().getClientName();

    ObjectMapper objectMapper = new ObjectMapper();

    Member member = null;

    switch (oauthClientName) {
      case "kakao" -> {
        KakaoResponse kakaoResponse = objectMapper.convertValue(oAuth2User.getAttributes(),
            KakaoResponse.class);

        String kakaoMemberId = "kakao_" + kakaoResponse.getId();

        member = memberRepository.findByMemberId(kakaoMemberId)
            .orElse(Member.builder()
                .memberId(kakaoMemberId)
                .memberName(kakaoResponse.getKakao_account().getProfile().getNickname())
                .email(kakaoResponse.getKakao_account().getEmail())
                .password("kakao_password")
                .role(Role.CUSTOMER)
                .loginType(LoginType.KAKAO)
                .build());
      }
      case "naver" -> {
        NaverResponse naverResponse = objectMapper.convertValue(oAuth2User.getAttributes(),
            NaverResponse.class);

        String naverMemberId = "naver_" + naverResponse.getResponse().getId();

        member = memberRepository.findByMemberId(naverMemberId)
            .orElse(Member.builder()
                .memberId(naverMemberId)
                .memberName(naverResponse.getResponse().getName())
                .email(naverResponse.getResponse().getEmail())
                .password("naver_password")
                .role(Role.CUSTOMER)
                .loginType(LoginType.NAVER)
                .build());

      }
      default -> throw new OAuth2AuthenticationException(
          ResponseCode.UNSUPPORTED_OAUTH_PROVIDER.getDescription());
    }

    memberRepository.save(member);

    return new CustomOAuth2Member(member.getMemberId());

  }

}
