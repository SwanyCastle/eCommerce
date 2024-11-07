package com.ecommerce.service.cart;

import com.ecommerce.dto.cart.CartItemDto;

public interface CartItemService {

  CartItemDto.Response addCartItem(String memberId, String token, CartItemDto.Request request);

}
