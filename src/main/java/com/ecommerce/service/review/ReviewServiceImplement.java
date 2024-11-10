package com.ecommerce.service.review;

import com.ecommerce.dto.ResponseDto;
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
import com.ecommerce.type.SortType;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImplement implements ReviewService {

  private static final int REVIEW_PAGE_SIZE = 3;

  private final ReviewRepository reviewRepository;

  private final AuthService authService;
  private final MemberService memberService;
  private final ProductService productService;

  /**
   * 리뷰 등록
   *
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

    boolean isExists = reviewRepository.existsByMemberAndProduct(member, product);
    if (isExists) {
      throw new ReviewException(ResponseCode.REVIEW_ALREADY_EXISTS);
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

  /**
   * 리뷰 정보 조회
   *
   * @param reviewId
   * @param token
   * @return ReviewDto.Response
   */
  @Override
  @Transactional(readOnly = true)
  public ReviewDto.Response getReviewDetail(Long reviewId, String memberId, String token) {

    authService.equalToMemberIdFromToken(memberId, token);

    return ReviewDto.Response.fromEntity(getReview(reviewId));

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

    Review review = getReview(reviewId);

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

  /**
   * 리뷰 정보 삭제
   *
   * @param reviewId
   * @param memberId
   * @param token
   * @return ResponseDto
   */
  @Override
  @Transactional
  public ResponseDto deleteReview(Long reviewId, String memberId, String token) {

    authService.equalToMemberIdFromToken(memberId, token);

    Review review = getReview(reviewId);

    if (!review.getMember().getMemberId().equals(memberId)) {
      throw new ReviewException(ResponseCode.REVIEW_UNMATCHED_MEMBER);
    }

    reviewRepository.delete(review);

    BigDecimal ratingAvg = reviewRepository
        .findAverageRatingByProductId(review.getProduct().getId());

    review.getProduct().setRating(ratingAvg);

    return ResponseDto.getResponseBody(ResponseCode.REVIEW_DELETE_SUCCESS);
  }

  /**
   * reviewId 로 리뷰 정보 조회
   *
   * @param reviewId
   * @return Review
   */
  @Override
  @Transactional(readOnly = true)
  public Review getReview(Long reviewId) {

    return reviewRepository.findById(reviewId)
        .orElseThrow(() -> new ReviewException(ResponseCode.REVIEW_NOT_FOUND));

  }

  /**
   * 특정 상품에 대한 리뷰 목록 조회
   *
   * @param productId
   * @return Page<Review>
   */
  @Override
  @Transactional(readOnly = true)
  public Page<ReviewDto.Response> getReviewsByProduct(
      Long productId, Integer page, SortType sortType
  ) {

    Sort sort = setSortType(sortType);

    Pageable pageable = PageRequest.of(page - 1, REVIEW_PAGE_SIZE, sort);

    Product product = productService.getProductById(productId);

    return reviewRepository.findByProduct(product, pageable).map(ReviewDto.Response::fromEntity);

  }

  /**
   * 특정 회원이 작성한 리뷰 목록 조회
   *
   * @param memberId
   * @param page
   * @param sortType
   * @return Page<ReviewDto.Response>
   */
  @Override
  @Transactional(readOnly = true)
  public Page<ReviewDto.Response> getReviewsByMember(
      String memberId, Integer page, SortType sortType
  ) {

    Sort sort = setSortType(sortType);

    Pageable pageable = PageRequest.of(page - 1, REVIEW_PAGE_SIZE, sort);

    Member member = memberService.getMemberByMemberId(memberId);

    return reviewRepository.findByMember(member, pageable).map(ReviewDto.Response::fromEntity);

  }

  /**
   * 리뷰 정렬 기준 설정
   *
   * @param sortType
   * @return Sort
   */
  private Sort setSortType(SortType sortType) {

    switch (sortType) {
      case LOW_RATING -> {
        return Sort.by(Direction.ASC, "rating");
      }
      case HIGH_RATING -> {
        return Sort.by(Direction.DESC, "rating");
      }
      default -> {
        return Sort.by(Direction.DESC, "createdAt");
      }
    }

  }

}
