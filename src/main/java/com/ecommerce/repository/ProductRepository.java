package com.ecommerce.repository;

import com.ecommerce.entity.Member;
import com.ecommerce.entity.Product;
import com.ecommerce.type.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

  Page<Product> findByProductNameContainingAndStatus(
      String productName, ProductStatus status, Pageable pageable
  );

  Page<Product> findByProductNameContaining(
      String productName, Pageable pageable
  );

  Page<Product> findByMemberAndProductNameContainingAndStatus(
     Member member, String productName, ProductStatus status, Pageable pageable
  );

  Page<Product> findByMemberAndProductNameContaining(
     Member member, String productName, Pageable pageable
  );

}
