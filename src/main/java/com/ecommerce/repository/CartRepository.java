package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart,Long> {

  Optional<Cart> findByMember(Member member);

  boolean existsByMember(Member member);

}
