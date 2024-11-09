package com.ecommerce.service.cart;

import com.ecommerce.dto.cart.CartDto;
import com.ecommerce.entity.Cart;

public interface CartService {

  CartDto getCartDetails(String memberId, String token);

  Cart getCartByMemberId(String memberId);

}
