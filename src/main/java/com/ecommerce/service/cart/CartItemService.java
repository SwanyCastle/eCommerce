package com.ecommerce.service.cart;

import com.ecommerce.dto.cart.CartItemDto;
import com.ecommerce.dto.cart.UpdateCartItemDto;

public interface CartItemService {

  CartItemDto.Response addCartItem(String memberId, String token, CartItemDto.Request request);

  CartItemDto.Response updateCartItem(String memberId, Long cartItemId, String token, UpdateCartItemDto updateRequest);

}
