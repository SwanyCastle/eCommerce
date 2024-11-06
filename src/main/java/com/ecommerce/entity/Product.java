package com.ecommerce.entity;

import com.ecommerce.type.ProductStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Entity
public class Product extends BaseEntity {

  @Column(name = "product_name", nullable = false)
  private String productName;

  private String description;

  @Column(name = "stock_quantity")
  private Integer stockQuantity;

  private BigDecimal price;

  @Enumerated(EnumType.STRING)
  private ProductStatus status;

  private BigDecimal rating;

  @ManyToOne
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

}
