package com.ecommerce.repository.review;

import com.ecommerce.entity.Member;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewCustomRepository{

  boolean existsByMemberAndProduct(Member member, Product product);

  Page<Review> findByProduct(Product product, Pageable pageable);

  Page<Review> findByMember(Member member, Pageable pageable);

}
