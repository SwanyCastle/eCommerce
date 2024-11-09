package com.ecommerce.service.review;

import com.ecommerce.dto.review.ReviewDto;
import com.ecommerce.dto.review.UpdateReviewDto;
import com.ecommerce.entity.Review;

public interface ReviewService {

  ReviewDto.Response createReview(String memberId, String token, ReviewDto.Request request);

  ReviewDto.Response getReviewDetail(Long reviewId, String memberId, String token);

  ReviewDto.Response updateReview(Long reviewId, String memberId, String token,
      UpdateReviewDto updateRequest);

  Review getReview(Long reviewId);

}
