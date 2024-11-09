package com.ecommerce.service.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ecommerce.dto.cart.CartDto;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Member;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.CartException;
import com.ecommerce.exception.MemberException;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.service.auth.AuthService;
import com.ecommerce.service.member.MemberService;
import com.ecommerce.type.LoginType;
import com.ecommerce.type.ProductStatus;
import com.ecommerce.type.ResponseCode;
import com.ecommerce.type.Role;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartServiceImplementTest {

  @Mock
  private CartRepository cartRepository;

  @Mock
  private AuthService authService;

  @Mock
  private MemberService memberService;

  @InjectMocks
  private CartServiceImplement cartServiceImplement;

  @Test
  @DisplayName("장바구니 내역 조회 - 성공")
  void testGetCartDetails_Success() {
    // given
    Member member = Member.builder()
        .memberId("testUser")
        .memberName("test")
        .email("test@email.com")
        .password("encodedPassword")
        .phoneNumber("01011112222")
        .address("test시 test구 test로 111")
        .role(Role.CUSTOMER)
        .loginType(LoginType.APP)
        .build();

    Product product1 = Product.builder()
        .productName("testProductName1")
        .description("testProductDescription1")
        .stockQuantity(3)
        .price(BigDecimal.valueOf(10001.0))
        .status(ProductStatus.NO_STOCK)
        .rating(BigDecimal.ZERO)
        .member(new Member())
        .build();

    Product product2 = Product.builder()
        .productName("testProductName2")
        .description("testProductDescription2")
        .stockQuantity(3)
        .price(BigDecimal.valueOf(10002.0))
        .status(ProductStatus.NO_STOCK)
        .rating(BigDecimal.ZERO)
        .member(new Member())
        .build();

    Cart cart = Cart.builder()
        .member(member)
        .cartItems(List.of())
        .build();

    cart.setCartItems(
        List.of(
            CartItem.builder()
                .cart(cart)
                .product(product1)
                .price(product1.getPrice().multiply(BigDecimal.valueOf(3)))
                .quantity(3)
                .build(),
            CartItem.builder()
                .cart(cart)
                .product(product2)
                .price(product2.getPrice().multiply(BigDecimal.valueOf(2)))
                .quantity(2)
                .build()
        )
    );

    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    given(memberService.getMemberByMemberId(eq("testUser")))
        .willReturn(member);
    given(cartRepository.findByMember(member))
        .willReturn(Optional.of(cart));

    // when
    CartDto cartDetails = cartServiceImplement.getCartDetails("testUser", "token");

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(memberService, times(1))
        .getMemberByMemberId(eq("testUser"));
    verify(cartRepository, times(1))
        .findByMember(eq(member));

    assertThat(cartDetails.getMemberId()).isEqualTo("testUser");
    assertThat(cartDetails.getTotalPrice()).isEqualTo(BigDecimal.valueOf(50007.0));
    assertThat(cartDetails.getCartItems().size()).isEqualTo(2);

  }

  @Test
  @DisplayName("장바구니 내역 조회 - 실패 (토큰에 있는 멤버 정보와 불일치)")
  void testGetCartDetails_Fail_MemberUnMatched() {
    // given
    doThrow(new MemberException(ResponseCode.MEMBER_UNMATCHED))
        .when(authService).equalToMemberIdFromToken(eq("testUser"), eq("token"));

    // when
    MemberException memberException = assertThrows(MemberException.class,
        () -> cartServiceImplement.getCartDetails("testUser", "token"));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    assertThat(memberException.getErrorCode()).isEqualTo(ResponseCode.MEMBER_UNMATCHED);
  }

  @Test
  @DisplayName("장바구니 내역 조회 - 실패 (존재하지 않는 멤버)")
  void testGetCartDetails_Fail_MemberNotFound() {
    // given
    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    doThrow(new MemberException(ResponseCode.MEMBER_NOT_FOUND))
        .when(memberService).getMemberByMemberId(eq("testUser"));

    // when
    MemberException memberException = assertThrows(MemberException.class,
        () -> cartServiceImplement.getCartDetails("testUser", "token"));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(memberService, times(1))
        .getMemberByMemberId(eq("testUser"));

    assertThat(memberException.getErrorCode()).isEqualTo(ResponseCode.MEMBER_NOT_FOUND);
  }

  @Test
  @DisplayName("장바구니 내역 조회 - 실패 (존재하지 않는 장바구니)")
  void testGetCartDetails_Fail_CartNotFound() {
    // given
    Member member = Member.builder()
        .memberId("testUser")
        .memberName("test")
        .email("test@email.com")
        .password("encodedPassword")
        .phoneNumber("01011112222")
        .address("test시 test구 test로 111")
        .role(Role.CUSTOMER)
        .loginType(LoginType.APP)
        .build();

    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    given(memberService.getMemberByMemberId(eq("testUser")))
        .willReturn(member);
    given(cartRepository.findByMember(member))
        .willReturn(Optional.empty());

    // when
    CartException cartException = assertThrows(CartException.class,
        () -> cartServiceImplement.getCartDetails("testUser", "token"));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(memberService, times(1))
        .getMemberByMemberId(eq("testUser"));
    verify(cartRepository, times(1))
        .findByMember(eq(member));

    assertThat(cartException.getErrorCode()).isEqualTo(ResponseCode.CART_NOT_FOUND);
  }

  @Test
  @DisplayName("Member Id 로 카트 조회 - 성공")
  void testGetCartByMemberId_Success() {
    // given
    Member member = Member.builder()
        .memberId("testUser")
        .memberName("test")
        .email("test@email.com")
        .password("encodedPassword")
        .phoneNumber("01011112222")
        .address("test시 test구 test로 111")
        .role(Role.CUSTOMER)
        .loginType(LoginType.APP)
        .build();

    Cart cart = Cart.builder()
        .member(member)
        .cartItems(List.of())
        .build();

    given(memberService.getMemberByMemberId(eq("testUser"))).willReturn(member);
    given(cartRepository.findByMember(eq(member))).willReturn(Optional.ofNullable(cart));

    // when
    Cart foundCart = cartServiceImplement.getCartByMemberId("testUser");

    // then
    assertThat(foundCart.getMember().getMemberId()).isEqualTo("testUser");
  }

  @Test
  @DisplayName("Member Id 로 카트 조회 - 실패 (존재하지 않는 멤버)")
  void testGetCartByMemberId_Fail_MemberNotFound() {
    // given
    doThrow(new MemberException(ResponseCode.MEMBER_NOT_FOUND))
        .when(memberService).getMemberByMemberId(eq("testUser"));

    // when
    MemberException memberException = assertThrows(MemberException.class,
        () -> cartServiceImplement.getCartByMemberId("testUser"));

    // then
    assertThat(memberException.getErrorCode()).isEqualTo(ResponseCode.MEMBER_NOT_FOUND);
  }

  @Test
  @DisplayName("Member Id 로 카트 조회 - 실패 (존재하지 않는 장바구니)")
  void testGetCartByMemberId_Fail_CartNotFound() {
    // given
    Member member = Member.builder()
        .memberId("testUser")
        .memberName("test")
        .email("test@email.com")
        .password("encodedPassword")
        .phoneNumber("01011112222")
        .address("test시 test구 test로 111")
        .role(Role.CUSTOMER)
        .loginType(LoginType.APP)
        .build();

    given(memberService.getMemberByMemberId(eq("testUser"))).willReturn(member);
    given(cartRepository.findByMember(eq(member))).willReturn(Optional.empty());

    // when
    CartException cartException = assertThrows(CartException.class,
        () -> cartServiceImplement.getCartByMemberId("testUser"));

    // then
    assertThat(cartException.getErrorCode()).isEqualTo(ResponseCode.CART_NOT_FOUND);
  }

}