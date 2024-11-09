package com.ecommerce.dto.review;

import com.ecommerce.entity.Review;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ReviewDto {

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Request {

    @NotNull
    private String memberId;

    @NotNull
    private Long productId;

    @NotBlank
    private String content;

    @NotNull
    @DecimalMin(value = "1.0")
    private BigDecimal rating;

  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Response {

    private Long id;
    private String memberId;
    private String productName;
    private String content;
    private BigDecimal rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReviewDto.Response fromEntity(Review review) {
      return Response.builder()
          .id(review.getId())
          .memberId(review.getMember().getMemberId())
          .productName(review.getProduct().getProductName())
          .content(review.getContent())
          .rating(review.getRating())
          .createdAt(review.getCreatedAt())
          .updatedAt(review.getUpdateAt())
          .build();
    }

  }

}
