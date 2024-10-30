package com.ecommerce.entity;

import com.ecommerce.type.LoginType;
import com.ecommerce.type.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Member extends BaseEntity implements UserDetails {

  @Column(name = "member_id", nullable = false)
  private String memberId;

  @Column(name = "member_name", nullable = false)
  private String memberName;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(name = "phone_number")
  private String phoneNumber;
  private String address;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Role role;

  @Column(name = "login_type", nullable = false)
  @Enumerated(EnumType.STRING)
  private LoginType loginType;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
  }

  @Override
  public String getUsername() {
    return this.memberName;
  }
}
