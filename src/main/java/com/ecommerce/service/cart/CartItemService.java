package com.ecommerce.service.cart;

import com.ecommerce.dto.ResponseDto;
import com.ecommerce.dto.cart.CartItemDto;

public interface CartItemService {

  ResponseDto addCartItem(String memberId, String token, CartItemDto.Request request);

}
