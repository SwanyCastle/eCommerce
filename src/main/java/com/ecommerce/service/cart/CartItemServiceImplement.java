package com.ecommerce.service.cart;

import com.ecommerce.dto.cart.CartItemDto;
import com.ecommerce.dto.cart.UpdateCartItemDto;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Member;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.CartException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.service.auth.AuthService;
import com.ecommerce.service.member.MemberService;
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
  private final CartRepository cartRepository;

  private final AuthService authService;
  private final MemberService memberService;
  private final ProductService productService;

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

    Cart cart = getCartByMemberId(memberId);

    Product product = productService.getProductById(request.getProductId());

    if (product.getStatus() != ProductStatus.IN_STOCK) {
      throw new CartException(ResponseCode.CANNOT_ADDED_PRODUCT);
    }

    boolean isExists = cartItemRepository.existsByCartAndProduct(cart, product);
    if (isExists) {
      throw new CartException(ResponseCode.CART_ITEM_ALREADY_EXISTS);
    }

    return CartItemDto.Response.fromEntity(
        cartItemRepository.save(
            CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(request.getQuantity())
                .price(product.getPrice())
                .build()
        )
    );

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

    if (updateRequest.getQuantity() > cartItem.getProduct().getStockQuantity()) {
      throw new CartException(ResponseCode.CART_ITEM_CANNOT_UPDATE_QUANTITY);
    }

    cartItem.setQuantity(updateRequest.getQuantity());

    return CartItemDto.Response.fromEntity(cartItem);

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

  /**
   * memberId 에 해당하는 Cart 조회
   *
   * @param memberId
   * @return Cart
   */
  @Transactional(readOnly = true)
  public Cart getCartByMemberId(String memberId) {

    Member member = memberService.getMemberByMemberId(memberId);

    return cartRepository.findByMember(member)
        .orElseThrow(() -> new CartException(ResponseCode.CART_NOT_FOUND));

  }

}
