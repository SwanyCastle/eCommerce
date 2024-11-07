package com.ecommerce.controller;

import com.ecommerce.dto.cart.CartDto;
import com.ecommerce.dto.cart.CartItemDto;
import com.ecommerce.service.cart.CartItemService;
import com.ecommerce.service.cart.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasRole('ROLE_CUSTOMER')")
@RequestMapping("/api/v1/carts/customer")
@RequiredArgsConstructor
public class CartController {

  private final CartService cartService;
  private final CartItemService cartItemService;

  /**
   * 특정 유저의 장바구니 목록 조회
   *
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

  /**
   * 장바구니에 상품 담기
   *
   * @param memberId
   * @param token
   * @param request
   * @return ResponseDto
   */
  @PostMapping("/{memberId}/cart-item")
  public CartItemDto.Response addCartItem(
      @PathVariable String memberId,
      @RequestHeader("Authorization") String token,
      @RequestBody @Valid CartItemDto.Request request
  ) {
    return cartItemService.addCartItem(memberId, token, request);
  }

}
