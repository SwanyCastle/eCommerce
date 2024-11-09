package com.ecommerce.dto.cart;

import com.ecommerce.entity.Cart;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDto {

  private String memberId;
  private BigDecimal totalPrice;
  private List<CartItemDto.Response> cartItems;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static CartDto fromEntity(Cart cart) {

    List<CartItemDto.Response> cartItems = cart.getCartItems().stream()
        .map(CartItemDto.Response::fromEntity)
        .toList();

    BigDecimal totalPrice = cartItems.stream()
        .map(CartItemDto.Response::getPrice)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    return CartDto.builder()
        .memberId(cart.getMember().getMemberId())
        .totalPrice(totalPrice)
        .cartItems(cartItems)
        .createdAt(cart.getCreatedAt())
        .updatedAt(cart.getUpdateAt())
        .build();

  }

}
