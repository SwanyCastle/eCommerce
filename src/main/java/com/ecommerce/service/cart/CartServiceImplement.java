package com.ecommerce.service.cart;

import com.ecommerce.dto.cart.CartDto;
import com.ecommerce.entity.Member;
import com.ecommerce.exception.CartException;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.service.auth.AuthService;
import com.ecommerce.service.member.MemberService;
import com.ecommerce.type.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartServiceImplement implements CartService {

  private final CartRepository cartRepository;

  private final AuthService authService;
  private final MemberService memberService;

  /**
   * 장바구니 조회
   *
   * @param memberId
   * @param token
   * @return CartDto
   */
  @Override
  @Transactional(readOnly = true)
  public CartDto getCartDetails(String memberId, String token) {

    authService.equalToMemberIdFromToken(memberId, token);

    Member member = memberService.getMemberByMemberId(memberId);

    return CartDto.fromEntity(
        cartRepository.findByMember(member)
            .orElseThrow(() -> new CartException(ResponseCode.CART_NOT_FOUND))
    );

  }

}
