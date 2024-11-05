package com.ecommerce.dto.product;

import com.ecommerce.type.ProductStatus;
import java.math.BigDecimal;
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
public class UpdateProductDto {

  private String productName;
  private String description;
  private Integer stockQuantity;
  private BigDecimal price;
  private ProductStatus status;

}
