package com.ecommerce.service.review;

import com.ecommerce.dto.review.ReviewDto;

public interface ReviewService {

  ReviewDto.Response createReview(String token, ReviewDto.Request request);

}
