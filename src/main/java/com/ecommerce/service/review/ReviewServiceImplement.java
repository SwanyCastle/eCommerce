package com.ecommerce.service.review;

import com.ecommerce.dto.review.ReviewDto;
import com.ecommerce.dto.review.UpdateReviewDto;
import com.ecommerce.entity.Member;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.Review;
import com.ecommerce.exception.ReviewException;
import com.ecommerce.repository.review.ReviewRepository;
import com.ecommerce.service.auth.AuthService;
import com.ecommerce.service.member.MemberService;
import com.ecommerce.service.product.ProductService;
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
  public ReviewDto.Response createReview(String memberId, String token, ReviewDto.Request request) {

    authService.equalToMemberIdFromToken(memberId, token);

    Member member = memberService.getMemberByMemberId(memberId);

    Product product = productService.getProductById(request.getProductId());

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

  /**
   * 리뷰 정보 조회
   * @param reviewId
   * @param token
   * @return ReviewDto.Response
   */
  @Override
  public ReviewDto.Response getReviewDetail(Long reviewId, String memberId, String token) {

    authService.equalToMemberIdFromToken(memberId, token);

    return ReviewDto.Response.fromEntity(
        reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ReviewException(ResponseCode.REVIEW_NOT_FOUND))
    );

  }

  /**
   * 리뷰 정보 수정
   *
   * @param reviewId
   * @param memberId
   * @param token
   * @return ReviewDto.Response
   */
  @Override
  @Transactional
  public ReviewDto.Response updateReview(Long reviewId, String memberId, String token,
      UpdateReviewDto updateRequest) {

    authService.equalToMemberIdFromToken(memberId, token);

    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new ReviewException(ResponseCode.REVIEW_NOT_FOUND));

    if (!review.getMember().getMemberId().equals(memberId)) {
      throw new ReviewException(ResponseCode.REVIEW_UNMATCHED_MEMBER);
    }

    review.setContent(updateRequest.getContent());
    review.setRating(updateRequest.getRating());

    BigDecimal ratingAvg = reviewRepository
        .findAverageRatingByProductId(review.getProduct().getId());

    review.getProduct().setRating(ratingAvg);

    return ReviewDto.Response.fromEntity(review);
  }

}
