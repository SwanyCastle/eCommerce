package com.ecommerce.controller;

import com.ecommerce.dto.ResponseDto;
import com.ecommerce.dto.cart.CartDto;
import com.ecommerce.dto.cart.CartItemDto;
import com.ecommerce.dto.cart.UpdateCartItemDto;
import com.ecommerce.service.cart.CartItemService;
import com.ecommerce.service.cart.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
   * @return CartItemDto.Response
   */
  @PostMapping("/{memberId}/cart-item")
  public CartItemDto.Response addCartItem(
      @PathVariable String memberId,
      @RequestHeader("Authorization") String token,
      @RequestBody @Valid CartItemDto.Request request
  ) {
    return cartItemService.addCartItem(memberId, token, request);
  }

  /**
   * 장바구니 상품 수량 수정
   *
   * @param memberId
   * @param cartItemId
   * @param token
   * @param updateRequest
   * @return CartItemDto.Response
   */
  @PutMapping("/{memberId}/cart-item/{cartItemId}")
  public CartItemDto.Response updateCartItem(
      @PathVariable String memberId,
      @PathVariable Long cartItemId,
      @RequestHeader("Authorization") String token,
      @RequestBody @Valid UpdateCartItemDto updateRequest
  ) {
    return cartItemService.updateCartItem(memberId, cartItemId, token, updateRequest);
  }

  /**
   * 장바구니 상품 삭제
   *
   * @param memberId
   * @param cartItemId
   * @param token
   * @return ResponseDto
   */
  @DeleteMapping("/{memberId}/cart-item/{cartItemId}")
  public ResponseDto deleteCartItem(
      @PathVariable String memberId,
      @PathVariable Long cartItemId,
      @RequestHeader("Authorization") String token
  ) {
    return cartItemService.deleteCartItem(memberId, cartItemId, token);
  }

  /**
   * 장바구니 상품 전체 삭제
   *
   * @param memberId
   * @param token
   * @return ResponseDto
   */
  @DeleteMapping("/{memberId}/cart-item")
  public ResponseDto deleteAllCartItem(
      @PathVariable String memberId,
      @RequestHeader("Authorization") String token
  ) {
    return cartItemService.deleteAllCartItem(memberId, token);
  }

}
