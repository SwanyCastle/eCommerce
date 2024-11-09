package com.ecommerce.service.cart;

import com.ecommerce.dto.ResponseDto;
import com.ecommerce.dto.cart.CartItemDto;
import com.ecommerce.dto.cart.UpdateCartItemDto;
import com.ecommerce.entity.CartItem;

public interface CartItemService {

  CartItemDto.Response addCartItem(String memberId, String token, CartItemDto.Request request);

  CartItemDto.Response updateCartItem(String memberId, Long cartItemId, String token, UpdateCartItemDto updateRequest);

  void checkExceedStockQuantity(int totalQuantity, int stockQuantity);

  ResponseDto deleteCartItem(String memberId, Long cartItemId, String token);

  ResponseDto deleteAllCartItem(String memberId, String token);

  CartItem getCartItemById(Long cartItemId);

}
