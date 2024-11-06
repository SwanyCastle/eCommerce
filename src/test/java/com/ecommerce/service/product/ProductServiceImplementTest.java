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

import com.ecommerce.dto.ResponseDto;
import com.ecommerce.dto.product.ProductDto;
import com.ecommerce.dto.product.UpdateProductDto;
import com.ecommerce.entity.Member;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.MemberException;
import com.ecommerce.exception.ProductException;
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
import java.util.Optional;
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

  @Test
  @DisplayName("특정 판매자 상품 목록 조회 - 성공 (상품 상태 X)")
  void testGetProductListByMemberId_Success_ProductStatusIsNull() {
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

    given(memberService.getMemberByMemberId(eq("testUser"))).willReturn(member);
    given(
        productRepository.findByMemberAndProductNameContaining(
            eq(member), eq("testProductName"), eq(pageable)
        )
    ).willReturn(products);

    // when
    Page<ProductDto.Response> productList =
        productServiceImplement
            .getProductListByMemberId(
                "testUser", 1, "testProductName", ProductStatus.NONE, SortType.LATEST
            );

    // then
    verify(memberService, times(1))
        .getMemberByMemberId(eq("testUser"));
    verify(productRepository, times(1))
        .findByMemberAndProductNameContaining(
            eq(member), eq("testProductName"), eq(pageable)
        );

    assertThat(productList).isNotNull();
    assertThat(productList.getSize()).isEqualTo(5);
    for (ProductDto.Response response : productList.getContent()) {
      assertThat(response.getSeller()).isEqualTo("testUser");
    }
  }

  @Test
  @DisplayName("특정 판매자 상품 목록 조회 - 성공 (상품 상태 O)")
  void testGetProductListByMemberId_Success_ProductStatus() {
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

    given(memberService.getMemberByMemberId(eq("testUser"))).willReturn(member);
    given(
        productRepository.findByMemberAndProductNameContainingAndStatus(
            eq(member), eq("testProductName"), eq(ProductStatus.NO_STOCK), eq(pageable)
        )
    ).willReturn(products);

    // when
    Page<ProductDto.Response> productList =
        productServiceImplement
            .getProductListByMemberId(
                "testUser", 1, "testProductName", ProductStatus.NO_STOCK, SortType.LATEST
            );

    // then
    verify(memberService, times(1))
        .getMemberByMemberId(eq("testUser"));
    verify(productRepository, times(1))
        .findByMemberAndProductNameContainingAndStatus(
            eq(member), eq("testProductName"), eq(ProductStatus.NO_STOCK), eq(pageable)
        );

    assertThat(productList).isNotNull();
    assertThat(productList.getSize()).isEqualTo(5);
    for (ProductDto.Response response : productList.getContent()) {
      assertThat(response.getStatus()).isEqualTo(ProductStatus.NO_STOCK);
      assertThat(response.getSeller()).isEqualTo("testUser");
    }
  }

  @Test
  @DisplayName("특정 판매자 상품 목록 조회 - 해당 결과 값 없음")
  void testGetProductListByMemberId_NoResult() {
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

    List<Product> mockProducts = List.of();

    Page<Product> products = new PageImpl<>(mockProducts);

    given(memberService.getMemberByMemberId(eq("testUser")))
        .willReturn(member);
    given(
        productRepository.findByMemberAndProductNameContainingAndStatus(
            eq(member), eq("aaa"), eq(ProductStatus.NO_STOCK), eq(pageable)
        )
    ).willReturn(products);

    // when
    Page<ProductDto.Response> productList =
        productServiceImplement
            .getProductListByMemberId(
                "testUser", 1, "aaa", ProductStatus.NO_STOCK, SortType.LATEST
            );

    // then
    verify(memberService, times(1))
        .getMemberByMemberId(eq("testUser"));
    verify(productRepository, times(1))
        .findByMemberAndProductNameContainingAndStatus(
            eq(member), eq("aaa"), eq(ProductStatus.NO_STOCK), eq(pageable)
        );

    assertThat(productList).isNotNull();
    assertThat(productList.getSize()).isEqualTo(0);
  }

  @Test
  @DisplayName("특정 판매자 상품 목록 조회 - 실패 (존재하지 않는 멤버)")
  void testGetProductListByMemberId_Fail_MemberNotFound() {
    // given
    doThrow(new MemberException(ResponseCode.MEMBER_NOT_FOUND))
        .when(memberService).getMemberByMemberId(eq("testUser"));

    // when
    MemberException memberException = assertThrows(MemberException.class,
        () -> productServiceImplement
            .getProductListByMemberId(
                "testUser", 1, "aaa", ProductStatus.NO_STOCK, SortType.LATEST
            ));

    // then
    verify(memberService, times(1))
        .getMemberByMemberId(eq("testUser"));

    assertThat(memberException.getErrorCode()).isEqualTo(ResponseCode.MEMBER_NOT_FOUND);
  }

  @Test
  @DisplayName("상품 정보 조회 - 성공")
  void testGetProductDetails_Success() {
    // given
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

    Product product = Product.builder()
        .productName("testProductName")
        .description("testProductDescription")
        .stockQuantity(3)
        .price(BigDecimal.valueOf(10001.0))
        .status(ProductStatus.IN_STOCK)
        .rating(BigDecimal.ZERO)
        .member(member)
        .build();

    given(productRepository.findById(1L)).willReturn(Optional.ofNullable(product));

    // when
    ProductDto.Response productDetails = productServiceImplement.getProductDetails(1L);

    // then
    verify(productRepository, times(1))
        .findById(eq(1L));

    assertThat(productDetails.getProductName()).isEqualTo("testProductName");
    assertThat(productDetails.getDescription()).isEqualTo("testProductDescription");
    assertThat(productDetails.getStockQuantity()).isEqualTo(3);
    assertThat(productDetails.getPrice()).isEqualTo(BigDecimal.valueOf(10001.0));
    assertThat(productDetails.getStatus()).isEqualTo(ProductStatus.IN_STOCK);
    assertThat(productDetails.getSeller()).isEqualTo(member.getMemberId());
  }

  @Test
  @DisplayName("상품 정보 조회 - 실패")
  void testGetProductDetails_Fail() {
    // given
    given(productRepository.findById(1L)).willReturn(Optional.empty());

    // when
    ProductException productException = assertThrows(ProductException.class,
        () -> productServiceImplement.getProductDetails(1L));

    // then
    verify(productRepository, times(1))
        .findById(eq(1L));

    assertThat(productException.getErrorCode()).isEqualTo(ResponseCode.PRODUCT_NOT_FOUND);
  }

  @Test
  @DisplayName("상품 정보 수정 - 성공")
  void testUpdateProduct_Success() {
    // given
    UpdateProductDto updateRequest = UpdateProductDto.builder()
        .productName("updateProductName")
        .description("updateProductDescription")
        .stockQuantity(3)
        .price(BigDecimal.valueOf(10001.0))
        .status(ProductStatus.NO_STOCK)
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

    Product product = Product.builder()
        .productName("testProductName")
        .description("testProductDescription")
        .stockQuantity(3)
        .price(BigDecimal.valueOf(10001.0))
        .status(ProductStatus.NO_STOCK)
        .rating(BigDecimal.ZERO)
        .member(member)
        .build();

    given(productRepository.findById(1L)).willReturn(Optional.ofNullable(product));

    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq(member.getMemberId()), eq("token"));

    // when
    ProductDto.Response updatedProduct =
        productServiceImplement.updateProduct(1L, "token", updateRequest);

    // then
    verify(productRepository, times(1)).findById(eq(1L));

    assertThat(updatedProduct.getProductName()).isEqualTo("updateProductName");
    assertThat(updatedProduct.getDescription()).isEqualTo("updateProductDescription");
  }

  @Test
  @DisplayName("상품 정보 수정 - 실패 (존재하지 않는 상품)")
  void testUpdateProduct_Fail_ProductNotFound() {
    // given
    UpdateProductDto updateRequest = UpdateProductDto.builder()
        .productName("updateProductName")
        .description("updateProductDescription")
        .stockQuantity(3)
        .price(BigDecimal.valueOf(10001.0))
        .status(ProductStatus.NO_STOCK)
        .build();

    given(productRepository.findById(1L)).willReturn(Optional.empty());

    // when
    ProductException productException = assertThrows(ProductException.class,
        () -> productServiceImplement.updateProduct(1L, "token", updateRequest));

    // then
    verify(productRepository, times(1)).findById(eq(1L));

    assertThat(productException.getErrorCode()).isEqualTo(ResponseCode.PRODUCT_NOT_FOUND);
  }

  @Test
  @DisplayName("상품 정보 수정 - 실패 (토큰에 있는 멤버 정보와 불일치)")
  void testUpdateProduct_Fail_MemberUnMatched() {
    // given
    UpdateProductDto updateRequest = UpdateProductDto.builder()
        .productName("updateProductName")
        .description("updateProductDescription")
        .stockQuantity(3)
        .price(BigDecimal.valueOf(10001.0))
        .status(ProductStatus.NO_STOCK)
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

    Product product = Product.builder()
        .productName("testProductName")
        .description("testProductDescription")
        .stockQuantity(3)
        .price(BigDecimal.valueOf(10001.0))
        .status(ProductStatus.NO_STOCK)
        .rating(BigDecimal.ZERO)
        .member(member)
        .build();

    given(productRepository.findById(1L)).willReturn(Optional.ofNullable(product));

    doThrow(new MemberException(ResponseCode.MEMBER_UNMATCHED))
        .when(authService).equalToMemberIdFromToken(eq(member.getMemberId()), eq("token"));

    // when
    MemberException memberException = assertThrows(MemberException.class,
        () -> productServiceImplement.updateProduct(1L, "token", updateRequest));

    // then
    verify(productRepository, times(1)).findById(eq(1L));
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq(member.getMemberId()), eq("token"));

    assertThat(memberException.getErrorCode()).isEqualTo(ResponseCode.MEMBER_UNMATCHED);
  }

  @Test
  @DisplayName("상품 정보 삭제 - 성공")
  void testDeleteProduct_Success() {
    // given
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

    Product product = Product.builder()
        .productName("testProductName")
        .description("testProductDescription")
        .stockQuantity(3)
        .price(BigDecimal.valueOf(10001.0))
        .status(ProductStatus.NO_STOCK)
        .rating(BigDecimal.ZERO)
        .member(member)
        .build();

    given(productRepository.findById(1L)).willReturn(Optional.ofNullable(product));

    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq(member.getMemberId()), eq("token"));

    // when
    ResponseDto responseDto = productServiceImplement.deleteProduct(1L, "token");

    // then
    verify(productRepository, times(1)).findById(eq(1L));
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq(member.getMemberId()), eq("token"));

    assertThat(responseDto.getCode()).isEqualTo(ResponseCode.PRODUCT_DELETE_SUCCESS);
  }

  @Test
  @DisplayName("상품 정보 삭제 - 실패 (존재하지 않는 상품)")
  void testDeleteProduct_Fail_ProductNotFound() {
    // given
    given(productRepository.findById(1L)).willReturn(Optional.empty());

    // when
    ProductException productException = assertThrows(ProductException.class,
        () -> productServiceImplement.deleteProduct(1L, "token"));

    // then
    verify(productRepository, times(1)).findById(eq(1L));

    assertThat(productException.getErrorCode()).isEqualTo(ResponseCode.PRODUCT_NOT_FOUND);
  }

  @Test
  @DisplayName("상품 정보 삭제 - 실패 (토큰에 있는 멤버 정보와 불일치)")
  void testDeleteProduct_Fail_MemberUnMatched() {
    // given
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

    Product product = Product.builder()
        .productName("testProductName")
        .description("testProductDescription")
        .stockQuantity(3)
        .price(BigDecimal.valueOf(10001.0))
        .status(ProductStatus.NO_STOCK)
        .rating(BigDecimal.ZERO)
        .member(member)
        .build();

    given(productRepository.findById(1L)).willReturn(Optional.ofNullable(product));

    doThrow(new MemberException(ResponseCode.MEMBER_UNMATCHED))
        .when(authService).equalToMemberIdFromToken(eq(member.getMemberId()), eq("token"));

    // when
    MemberException memberException = assertThrows(MemberException.class,
        () -> productServiceImplement.deleteProduct(1L, "token"));

    // then
    verify(productRepository, times(1)).findById(eq(1L));
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq(member.getMemberId()), eq("token"));

    assertThat(memberException.getErrorCode()).isEqualTo(ResponseCode.MEMBER_UNMATCHED);
  }

}