package com.ecommerce.dto.product;

import com.ecommerce.entity.Product;
import com.ecommerce.type.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ProductDto {

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Request {

    @NotBlank
    private String productName;

    @NotBlank
    private String description;

    @NotNull
    @Min(value = 1)
    private Integer stockQuantity;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal price;

    @Builder.Default
    private ProductStatus status = ProductStatus.IN_STOCK;

  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Response {

    private Long id;
    private String productName;
    private String description;
    private Integer stockQuantity;
    private BigDecimal price;
    private ProductStatus status;
    private BigDecimal rating;
    private String seller;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Response fromEntity(Product product) {

      return Response.builder()
          .id(product.getId())
          .productName(product.getProductName())
          .description(product.getDescription())
          .stockQuantity(product.getStockQuantity())
          .price(product.getPrice())
          .status(product.getStatus())
          .rating(product.getRating())
          .seller(product.getMember().getMemberId())
          .createdAt(product.getCreatedAt())
          .updatedAt(product.getUpdateAt())
          .build();

    }

  }

}
