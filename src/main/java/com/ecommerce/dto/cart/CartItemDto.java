package com.ecommerce.dto.cart;

import com.ecommerce.entity.CartItem;
import com.ecommerce.type.ProductStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class CartItemDto {

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Request {

    @NotNull
    private Long productId;

    @NotNull
    @Min(value = 1)
    private Integer quantity;

  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Response {

    private Long id;
    private Long cartId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
    private ProductStatus status;

    public static Response fromEntity(CartItem cartItem) {
      return Response.builder()
          .id(cartItem.getId())
          .cartId(cartItem.getCart().getId())
          .productId(cartItem.getProduct().getId())
          .productName(cartItem.getProduct().getProductName())
          .quantity(cartItem.getQuantity())
          .price(
              BigDecimal.valueOf(cartItem.getQuantity())
                  .multiply(cartItem.getProduct().getPrice())
          )
          .status(cartItem.getProduct().getStatus())
          .build();
    }

  }

}
