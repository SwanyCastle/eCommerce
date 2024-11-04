package com.ecommerce.repository;

import com.ecommerce.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

  Optional<Member> findByMemberId(String userId);

  boolean existsByMemberId(String userId);

}
