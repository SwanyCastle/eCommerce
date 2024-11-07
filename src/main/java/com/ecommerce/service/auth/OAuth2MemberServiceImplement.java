package com.ecommerce.service.auth;

import com.ecommerce.dto.auth.KakaoUserInfoDto.KakaoResponse;
import com.ecommerce.dto.auth.NaverUserInfoDto.NaverResponse;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CustomOAuth2Member;
import com.ecommerce.entity.Member;
import com.ecommerce.repository.CartRepository;
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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuth2MemberServiceImplement extends DefaultOAuth2UserService {

  private final MemberRepository memberRepository;
  private final CartRepository cartRepository;
  private final ObjectMapper objectMapper;

  /**
   * OAuth Server 에 요청을 보낸 후 redirect 로 각 소셜 로그인으로 로그인한 유저의 정보를 받아 Ecommerce Application DB 에 저장후
   * 저장된 유저의 정보로 장바구니 생성, Ecommerce Application 의 토큰 생성을 위해 CustomOAuth2Member 객체로 반환
   *
   * @param request
   * @return CustomOAuth2Member
   * @throws OAuth2AuthenticationException
   */
  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {

    OAuth2User oAuth2User = super.loadUser(request);
    String oauthClientName = request.getClientRegistration().getClientName();

    Member member = createSnsMember(oauthClientName, oAuth2User);

    Member savedMember = memberRepository.save(member);

    boolean isExists = cartRepository.existsByMember(savedMember);
    if (!isExists) {
      cartRepository.save(
          Cart.builder().member(savedMember).build()
      );
    }

    return new CustomOAuth2Member(member.getMemberId());

  }

  /**
   * OAuth Server 에 요청을 보낸 후 redirect 로 각 소셜 로그인으로 로그인한 유저의 정보를 받아 Ecommerce Application DB 에 저장하기
   * 위해 Member 생성
   *
   * @param oauthClientName
   * @param oAuth2User
   * @return Member
   */
  private Member createSnsMember(String oauthClientName, OAuth2User oAuth2User) {

    switch (oauthClientName) {

      case "kakao" -> {
        KakaoResponse kakaoResponse = objectMapper.convertValue(oAuth2User.getAttributes(),
            KakaoResponse.class);

        String kakaoMemberId = "kakao_" + kakaoResponse.getId();

        return memberRepository.findByMemberId(kakaoMemberId)
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

        return memberRepository.findByMemberId(naverMemberId)
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

  }

}
