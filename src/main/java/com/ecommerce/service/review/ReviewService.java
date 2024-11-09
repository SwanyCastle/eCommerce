package com.ecommerce.service.review;

import com.ecommerce.dto.ResponseDto;
import com.ecommerce.dto.review.ReviewDto;
import com.ecommerce.dto.review.UpdateReviewDto;
import com.ecommerce.entity.Review;
import com.ecommerce.type.SortType;
import org.springframework.data.domain.Page;

public interface ReviewService {

  ReviewDto.Response createReview(String memberId, String token, ReviewDto.Request request);

  ReviewDto.Response getReviewDetail(Long reviewId, String memberId, String token);

  ReviewDto.Response updateReview(Long reviewId, String memberId, String token,
      UpdateReviewDto updateRequest);

  ResponseDto deleteReview(Long reviewId, String memberId, String token);

  Review getReview(Long reviewId);

  Page<ReviewDto.Response> getReviewsByProduct(Long productId, Integer page, SortType sortType);

  Page<ReviewDto.Response> getReviewsByMember(String memberId, Integer page, SortType sortType);

}
