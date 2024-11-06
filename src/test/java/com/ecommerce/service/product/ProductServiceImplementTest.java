package com.ecommerce.service.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ecommerce.dto.product.ProductDto;
import com.ecommerce.entity.Member;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.MemberException;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.auth.AuthService;
import com.ecommerce.service.member.MemberService;
import com.ecommerce.type.LoginType;
import com.ecommerce.type.ProductStatus;
import com.ecommerce.type.ResponseCode;
import com.ecommerce.type.Role;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplementTest {

  @Mock
  private AuthService authService;

  @Mock
  private MemberService memberService;

  @Mock
  private ProductRepository productRepository;

  @InjectMocks
  private ProductServiceImplement productServiceImplement;

  @Test
  @DisplayName("상품 등록 - 성공")
  void testCreateProduct_Success() {
    // given
    ProductDto.Request request = ProductDto.Request.builder()
        .productName("testProductName")
        .description("testProductDescription")
        .stockQuantity(3)
        .price(BigDecimal.valueOf(10000))
        .status(ProductStatus.IN_STOCK)
        .build();

    Member member = Member.builder()
        .memberId("testUser")
        .memberName("test")
        .email("test@email.com")
        .password("encodedPassword")
        .phoneNumber("01011112222")
        .address("test시 test구 test로 111")
        .role(Role.SELLER)
        .loginType(LoginType.APP)
        .build();

    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    given(memberService.getMemberByMemberId("testUser"))
        .willReturn(member);

    given(productRepository.save(any(Product.class)))
        .willReturn(
            Product.builder()
                .productName("testProductName")
                .description("testProductDescription")
                .stockQuantity(3)
                .price(BigDecimal.valueOf(10000.0))
                .status(ProductStatus.IN_STOCK)
                .rating(BigDecimal.ZERO)
                .member(member)
                .build()
        );

    ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);

    // when
    ProductDto.Response savedProduct =
        productServiceImplement.createProduct("testUser", "token", request);

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(memberService, times(1))
        .getMemberByMemberId(eq("testUser"));
    verify(productRepository, times(1)).save(productCaptor.capture());

    assertThat(productCaptor.getValue()).isNotNull();
    assertThat(productCaptor.getValue().getProductName()).isEqualTo("testProductName");
    assertThat(productCaptor.getValue().getDescription()).isEqualTo("testProductDescription");
    assertThat(productCaptor.getValue().getStockQuantity()).isEqualTo(3);
    assertThat(productCaptor.getValue().getPrice()).isEqualTo(BigDecimal.valueOf(10000.0));
    assertThat(productCaptor.getValue().getStatus()).isEqualTo(ProductStatus.IN_STOCK);
    assertThat(productCaptor.getValue().getRating()).isEqualTo(BigDecimal.ZERO);
    assertThat(productCaptor.getValue().getMember()).isEqualTo(member);
  }

  @Test
  @DisplayName("상품 등록 - 실패 (토큰에 있는 멤버 정보와 불일치)")
  void testCreateProduct_Fail_MemberUnMatched() {
    // given
    ProductDto.Request request = ProductDto.Request.builder()
        .productName("testProductName")
        .description("testProductDescription")
        .stockQuantity(3)
        .price(BigDecimal.valueOf(10000))
        .status(ProductStatus.IN_STOCK)
        .build();

    doThrow(new MemberException(ResponseCode.MEMBER_UNMATCHED))
        .when(authService).equalToMemberIdFromToken(eq("testUser"), eq("token"));

    // when
    MemberException memberException = assertThrows(MemberException.class,
        () -> productServiceImplement.createProduct("testUser", "token", request));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    assertThat(memberException.getErrorCode()).isEqualTo(ResponseCode.MEMBER_UNMATCHED);
  }

  @Test
  @DisplayName("상품 등록 - 실패 (존재하지 않는 멤버)")
  void testCreateProduct_Fail_MemberNotFound() {
    // given
    ProductDto.Request request = ProductDto.Request.builder()
        .productName("testProductName")
        .description("testProductDescription")
        .stockQuantity(3)
        .price(BigDecimal.valueOf(10000))
        .status(ProductStatus.IN_STOCK)
        .build();

    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    doThrow(new MemberException(ResponseCode.MEMBER_NOT_FOUND))
        .when(memberService).getMemberByMemberId(eq("testUser"));

    // when
    MemberException memberException = assertThrows(MemberException.class,
        () -> productServiceImplement.createProduct("testUser", "token", request));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(memberService, times(1))
        .getMemberByMemberId(eq("testUser"));

    assertThat(memberException.getErrorCode()).isEqualTo(ResponseCode.MEMBER_NOT_FOUND);
  }

}