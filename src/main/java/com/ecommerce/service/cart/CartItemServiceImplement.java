package com.ecommerce.service.cart;

import com.ecommerce.dto.ResponseDto;
import com.ecommerce.dto.cart.CartItemDto;
import com.ecommerce.dto.cart.UpdateCartItemDto;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.CartException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.service.auth.AuthService;
import com.ecommerce.service.product.ProductService;
import com.ecommerce.type.ProductStatus;
import com.ecommerce.type.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartItemServiceImplement implements CartItemService {

  private final CartItemRepository cartItemRepository;

  private final AuthService authService;
  private final ProductService productService;
  private final CartService cartService;

  /**
   * 장바구니에 상품 담기
   *
   * @param memberId
   * @param token
   * @param request
   * @return ResponseDto
   */
  @Override
  @Transactional
  public CartItemDto.Response addCartItem(String memberId, String token,
      CartItemDto.Request request) {

    authService.equalToMemberIdFromToken(memberId, token);

    Cart cart = cartService.getCartByMemberId(memberId);

    Product product = productService.getProductById(request.getProductId());

    if (product.getStatus() != ProductStatus.IN_STOCK) {
      throw new CartException(ResponseCode.CART_ITEM_CANNOT_ADDED_PRODUCT);
    }

    checkExceedStockQuantity(request.getQuantity(), product.getStockQuantity());

    CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
        .map(existsCartItem -> {
          int totalQuantity = existsCartItem.getQuantity() + request.getQuantity();
          checkExceedStockQuantity(totalQuantity, product.getStockQuantity());

          existsCartItem.setQuantity(totalQuantity);
          return existsCartItem;
        }).orElseGet(
            () -> cartItemRepository.save(
                CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .price(product.getPrice())
                    .build()
        ));

    return CartItemDto.Response.fromEntity(cartItem);

  }

  /**
   * 장바구니 상품 수량 수정
   *
   * @param memberId
   * @param token
   * @param updateRequest
   * @return CartItemDto.Response
   */
  @Override
  @Transactional
  public CartItemDto.Response updateCartItem(
      String memberId, Long cartItemId, String token, UpdateCartItemDto updateRequest
  ) {

    authService.equalToMemberIdFromToken(memberId, token);

    CartItem cartItem = getCartItemById(cartItemId);

    int totalQuantity = cartItem.getQuantity() + updateRequest.getQuantity();
    checkExceedStockQuantity(totalQuantity, cartItem.getProduct().getStockQuantity());

    cartItem.setQuantity(updateRequest.getQuantity());

    return CartItemDto.Response.fromEntity(cartItem);

  }

  /**
   * 장바구니 상품 수 가 상품의 재고 수를 초고화는지 확인
   * @param totalQuantity
   * @param stockQuantity
   */
  @Override
  public void checkExceedStockQuantity(int totalQuantity, int stockQuantity) {
    if (totalQuantity > stockQuantity) {
      throw new CartException(ResponseCode.CART_ITEM_EXCEED_QUANTITY);
    }
  }

  /**
   * 장바구니 상품 제거
   *
   * @param memberId
   * @param cartItemId
   * @param token
   * @return ResponseDto
   */
  @Override
  @Transactional
  public ResponseDto deleteCartItem(String memberId, Long cartItemId, String token) {

    authService.equalToMemberIdFromToken(memberId, token);

    CartItem cartItem = getCartItemById(cartItemId);

    cartItemRepository.delete(cartItem);

    return ResponseDto.getResponseBody(ResponseCode.CART_ITEM_DELETE_SUCCESS);

  }

  /**
   * 장바구니 상품 전체 제거
   *
   * @param memberId
   * @param token
   * @return ResponseDto
   */
  @Override
  @Transactional
  public ResponseDto deleteAllCartItem(String memberId, String token) {

    authService.equalToMemberIdFromToken(memberId, token);

    Cart cart = cartService.getCartByMemberId(memberId);

    cartItemRepository.deleteAllByCart(cart);

    return ResponseDto.getResponseBody(ResponseCode.CART_ITEM_DELETE_SUCCESS);

  }

  /**
   * CartItem ID 로 조회
   *
   * @param cartItemId
   * @return CartItem
   */
  @Transactional(readOnly = true)
  public CartItem getCartItemById(Long cartItemId) {

    return cartItemRepository.findById(cartItemId)
        .orElseThrow(() -> new CartException(ResponseCode.CART_ITEM_NOT_FOUND));

  }

}
