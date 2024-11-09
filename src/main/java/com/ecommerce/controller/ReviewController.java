package com.ecommerce.controller;

import com.ecommerce.dto.review.ReviewDto;
import com.ecommerce.service.review.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasRole('ROLE_CUSTOMER')")
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

  private final ReviewService reviewService;

  /**
   * 리뷰 등록
   * @param memberId
   * @param token
   * @param request
   * @return ReviewDto.Response
   */
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
   * @param reviewId
   * @param token
   * @return ReviewDto.Response
   */
  @GetMapping("/{reviewId}/author/{memberId}")
  public ReviewDto.Response createReview(
      @PathVariable Long reviewId,
      @PathVariable String memberId,
      @RequestHeader("Authorization") String token
  ) {
    return reviewService.getReviewDetail(reviewId, memberId, token);
  }

}
