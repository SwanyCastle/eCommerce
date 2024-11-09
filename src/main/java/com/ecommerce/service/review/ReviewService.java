package com.ecommerce.service.review;

import com.ecommerce.dto.review.ReviewDto;

public interface ReviewService {

  ReviewDto.Response createReview(String memberId, String token, ReviewDto.Request request);

  ReviewDto.Response getReviewDetail(Long reviewId, String memberId, String token);

}
