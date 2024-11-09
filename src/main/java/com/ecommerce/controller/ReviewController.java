package com.ecommerce.controller;

import com.ecommerce.dto.ResponseDto;
import com.ecommerce.dto.review.ReviewDto;
import com.ecommerce.dto.review.UpdateReviewDto;
import com.ecommerce.service.review.ReviewService;
import com.ecommerce.type.SortType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

  private final ReviewService reviewService;

  /**
   * 리뷰 등록
   *
   * @param memberId
   * @param token
   * @param request
   * @return ReviewDto.Response
   */
  @PreAuthorize("hasRole('ROLE_CUSTOMER')")
  @PostMapping("/author/{memberId}")
  public ReviewDto.Response createReview(
      @PathVariable String memberId,
      @RequestHeader("Authorization") String token,
      @RequestBody @Valid ReviewDto.Request request
  ) {
    return reviewService.createReview(memberId, token, request);
  }

  /**
   * 리뷰 정보 조회
   *
   * @param reviewId
   * @param token
   * @return ReviewDto.Response
   */
  @GetMapping("/{reviewId}/author/{memberId}")
  public ReviewDto.Response getReviewDetail(
      @PathVariable Long reviewId,
      @PathVariable String memberId,
      @RequestHeader("Authorization") String token
  ) {
    return reviewService.getReviewDetail(reviewId, memberId, token);
  }

  /**
   * 리뷰 정보 수정
   *
   * @param reviewId
   * @param memberId
   * @param token
   * @param updateRequest
   * @return ReviewDto.Response
   */
  @PreAuthorize("hasRole('ROLE_CUSTOMER')")
  @PutMapping("/{reviewId}/author/{memberId}")
  public ReviewDto.Response updateReview(
      @PathVariable Long reviewId,
      @PathVariable String memberId,
      @RequestHeader("Authorization") String token,
      @RequestBody @Valid UpdateReviewDto updateRequest
  ) {
    return reviewService.updateReview(reviewId, memberId, token, updateRequest);
  }

  /**
   * 리뷰 정보 삭제
   *
   * @param reviewId
   * @param memberId
   * @param token
   * @return ResponseDto
   */
  @PreAuthorize("hasRole('ROLE_CUSTOMER')")
  @DeleteMapping("/{reviewId}/author/{memberId}")
  public ResponseDto deleteReview(
      @PathVariable Long reviewId,
      @PathVariable String memberId,
      @RequestHeader("Authorization") String token
  ) {
    return reviewService.deleteReview(reviewId, memberId, token);
  }

  /**
   * 특정 상품에 대한 리뷰 목록 조회
   *
   * @param productId
   * @param page
   * @param sortType
   * @return Page<ReviewDto.Response>
   */
  @GetMapping("/products/{productId}")
  public Page<ReviewDto.Response> getReviewsByProduct(
      @PathVariable Long productId,
      @RequestParam(required = false, defaultValue = "1") Integer page,
      @RequestParam(required = false, defaultValue = "LATEST") SortType sortType
  ) {
    return reviewService.getReviewsByProduct(productId, page, sortType);
  }

}
