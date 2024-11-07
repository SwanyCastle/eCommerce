package com.ecommerce.service.cart;

import com.ecommerce.dto.cart.CartDto;

public interface CartService {

  CartDto getCartDetails(String memberId, String token);

}
