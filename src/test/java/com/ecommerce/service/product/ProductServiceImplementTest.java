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
import com.ecommerce.type.SortType;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplementTest {

  private static final int TEST_PAGE_SIZE = 5;

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

  @Test
  @DisplayName("전체 상품 목록 조회 - 성공 (상품 상태 X)")
  void testGetProductList_Success_ProductStatusIsNull() {
    // given
    Sort sort = Sort.by(Direction.DESC, "createdAt");
    Pageable pageable = PageRequest.of(0, TEST_PAGE_SIZE, sort);

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

    List<Product> mockProducts = List.of(
        Product.builder()
            .productName("testProductName1")
            .description("testProductDescription1")
            .stockQuantity(3)
            .price(BigDecimal.valueOf(10001.0))
            .status(ProductStatus.IN_STOCK)
            .rating(BigDecimal.ZERO)
            .member(member)
            .build(),
        Product.builder()
            .productName("testProductName2")
            .description("testProductDescription2")
            .stockQuantity(3)
            .price(BigDecimal.valueOf(10002.0))
            .status(ProductStatus.IN_STOCK)
            .rating(BigDecimal.ZERO)
            .member(member)
            .build(),
        Product.builder()
            .productName("testProductName3")
            .description("testProductDescription3")
            .stockQuantity(3)
            .price(BigDecimal.valueOf(10003.0))
            .status(ProductStatus.IN_STOCK)
            .rating(BigDecimal.ZERO)
            .member(member)
            .build(),
        Product.builder()
            .productName("testProductName4")
            .description("testProductDescription4")
            .stockQuantity(3)
            .price(BigDecimal.valueOf(10004.0))
            .status(ProductStatus.IN_STOCK)
            .rating(BigDecimal.ZERO)
            .member(member)
            .build(),
        Product.builder()
            .productName("testProductName5")
            .description("testProductDescription5")
            .stockQuantity(3)
            .price(BigDecimal.valueOf(10005.0))
            .status(ProductStatus.IN_STOCK)
            .rating(BigDecimal.ZERO)
            .member(member)
            .build()
    );

    Page<Product> products = new PageImpl<>(mockProducts);

    given(
        productRepository.findByProductNameContaining(
            eq("testProductName"), eq(pageable)
        )
    ).willReturn(products);

    // when
    Page<ProductDto.Response> productList =
        productServiceImplement
            .getProductList(1, "testProductName", ProductStatus.NONE, SortType.LATEST);

    // then
    verify(productRepository, times(1))
        .findByProductNameContaining(
            eq("testProductName"), eq(pageable)
        );

    assertThat(productList).isNotNull();
    assertThat(productList.getSize()).isEqualTo(5);
  }

  @Test
  @DisplayName("전체 상품 목록 조회 - 성공 (상품 상태 O)")
  void testGetProductList_Success_ProductStatus() {
    // given
    Sort sort = Sort.by(Direction.DESC, "createdAt");
    Pageable pageable = PageRequest.of(0, TEST_PAGE_SIZE, sort);

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

    List<Product> mockProducts = List.of(
        Product.builder()
            .productName("testProductName1")
            .description("testProductDescription1")
            .stockQuantity(3)
            .price(BigDecimal.valueOf(10001.0))
            .status(ProductStatus.NO_STOCK)
            .rating(BigDecimal.ZERO)
            .member(member)
            .build(),
        Product.builder()
            .productName("testProductName2")
            .description("testProductDescription2")
            .stockQuantity(3)
            .price(BigDecimal.valueOf(10002.0))
            .status(ProductStatus.NO_STOCK)
            .rating(BigDecimal.ZERO)
            .member(member)
            .build(),
        Product.builder()
            .productName("testProductName3")
            .description("testProductDescription3")
            .stockQuantity(3)
            .price(BigDecimal.valueOf(10003.0))
            .status(ProductStatus.NO_STOCK)
            .rating(BigDecimal.ZERO)
            .member(member)
            .build(),
        Product.builder()
            .productName("testProductName4")
            .description("testProductDescription4")
            .stockQuantity(3)
            .price(BigDecimal.valueOf(10004.0))
            .status(ProductStatus.NO_STOCK)
            .rating(BigDecimal.ZERO)
            .member(member)
            .build(),
        Product.builder()
            .productName("testProductName5")
            .description("testProductDescription5")
            .stockQuantity(3)
            .price(BigDecimal.valueOf(10005.0))
            .status(ProductStatus.NO_STOCK)
            .rating(BigDecimal.ZERO)
            .member(member)
            .build()
    );

    Page<Product> products = new PageImpl<>(mockProducts);

    given(
        productRepository.findByProductNameContainingAndStatus(
            eq("testProductName"), eq(ProductStatus.NO_STOCK), eq(pageable)
        )
    ).willReturn(products);

    // when
    Page<ProductDto.Response> productList =
        productServiceImplement
            .getProductList(1, "testProductName", ProductStatus.NO_STOCK, SortType.LATEST);

    // then
    verify(productRepository, times(1))
        .findByProductNameContainingAndStatus(
            eq("testProductName"), eq(ProductStatus.NO_STOCK), eq(pageable)
        );

    assertThat(productList).isNotNull();
    assertThat(productList.getSize()).isEqualTo(5);
    assertThat(productList.getContent().get(0).getStatus()).isEqualTo(ProductStatus.NO_STOCK);
  }

  @Test
  @DisplayName("전체 상품 목록 조회 - 해당 결과 값 없음")
  void testGetProductList_NoResult() {
    // given
    Sort sort = Sort.by(Direction.DESC, "createdAt");
    Pageable pageable = PageRequest.of(0, TEST_PAGE_SIZE, sort);

    List<Product> mockProducts = List.of();

    Page<Product> products = new PageImpl<>(mockProducts);

    given(
        productRepository.findByProductNameContainingAndStatus(
            eq("aaa"), eq(ProductStatus.NO_STOCK), eq(pageable)
        )
    ).willReturn(products);

    // when
    Page<ProductDto.Response> productList =
        productServiceImplement
            .getProductList(1, "aaa", ProductStatus.NO_STOCK, SortType.LATEST);

    // then
    verify(productRepository, times(1))
        .findByProductNameContainingAndStatus(
            eq("aaa"), eq(ProductStatus.NO_STOCK), eq(pageable)
        );

    assertThat(productList).isNotNull();
    assertThat(productList.getSize()).isEqualTo(0);
  }

}