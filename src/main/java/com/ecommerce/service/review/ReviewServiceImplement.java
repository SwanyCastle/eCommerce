package com.ecommerce.service.review;

import com.ecommerce.dto.review.ReviewDto;
import com.ecommerce.entity.Member;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.Review;
import com.ecommerce.exception.ProductException;
import com.ecommerce.repository.review.ReviewRepository;
import com.ecommerce.service.auth.AuthService;
import com.ecommerce.service.member.MemberService;
import com.ecommerce.service.product.ProductService;
import com.ecommerce.type.ProductStatus;
import com.ecommerce.type.ResponseCode;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImplement implements ReviewService {

  private final ReviewRepository reviewRepository;

  private final AuthService authService;
  private final MemberService memberService;
  private final ProductService productService;

  /**
   * 리뷰 등록
   * @param token
   * @param request
   * @return ReviewDto.Response
   */
  @Override
  @Transactional
  public ReviewDto.Response createReview(String token, ReviewDto.Request request) {

    authService.equalToMemberIdFromToken(request.getMemberId(), token);

    Member member = memberService.getMemberByMemberId(request.getMemberId());

    Product product = productService.getProductById(request.getProductId());
    if (product.getStatus() != ProductStatus.IN_STOCK) {
      throw new ProductException(ResponseCode.PRODUCT_DISABLE);
    }

    Review savedReview = reviewRepository.save(
        Review.builder()
            .member(member)
            .product(product)
            .content(request.getContent())
            .rating(request.getRating())
            .build()
    );

    BigDecimal ratingAvg = reviewRepository
        .findAverageRatingByProductId(request.getProductId());

    product.setRating(ratingAvg);

    return ReviewDto.Response.fromEntity(savedReview);

  }

}
