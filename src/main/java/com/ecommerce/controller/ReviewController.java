package com.ecommerce.controller;

import com.ecommerce.dto.review.ReviewDto;
import com.ecommerce.service.review.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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
   * @param request
   * @param token
   * @return ReviewDto.Response
   */
  @PostMapping
  public ReviewDto.Response createReview(
      @RequestBody @Valid ReviewDto.Request request,
      @RequestHeader("Authorization") String token
  ) {
    return reviewService.createReview(token, request);
  }

}
