package com.ecommerce.controller;

import com.ecommerce.dto.cart.CartDto;
import com.ecommerce.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CartController {

  private final CartService cartService;

  /**
   * 특정 유저의 장바구니 목록 조회
   * @param memberId
   * @param token
   * @return CartDto
   */
  @GetMapping("/{memberId}")
  public CartDto getCartDetails(
      @PathVariable String memberId,
      @RequestHeader("Authorization") String token
  ) {
    return cartService.getCartDetails(memberId, token);
  }

}
