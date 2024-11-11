package com.ecommerce.repository.review;

import java.math.BigDecimal;

public interface ReviewCustomRepository {

  BigDecimal findAverageRatingByProductId(Long productId);

}
