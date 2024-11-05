package com.ecommerce.dto.product;

import com.ecommerce.type.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
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
  @Min(value = 0)
  private Integer stockQuantity;
  @DecimalMin(value = "0.0", inclusive = false)
  private BigDecimal price;
  private ProductStatus status;

}
