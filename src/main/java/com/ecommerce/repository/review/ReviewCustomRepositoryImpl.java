package com.ecommerce.repository.review;

import com.ecommerce.entity.QReview;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewCustomRepositoryImpl implements ReviewCustomRepository {

  private final JPAQueryFactory jpaQueryFactory;

  /**
   * 특정 상품의 리뷰 평점 평균
   * @param productId
   * @return BigDecimal
   */
  @Override
  public BigDecimal findAverageRatingByProductId(Long productId) {
    QReview review = QReview.review;

    Double ratingAvg = jpaQueryFactory
        .select(review.rating.avg())
        .from(review)
        .where(review.product.id.eq(productId))
        .fetchOne();

    return ratingAvg != null ? BigDecimal.valueOf(ratingAvg) : BigDecimal.ZERO;
  }

}
