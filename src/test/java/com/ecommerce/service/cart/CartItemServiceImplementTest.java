package com.ecommerce.service.cart;

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
import com.ecommerce.dto.cart.CartItemDto;
import com.ecommerce.dto.cart.UpdateCartItemDto;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Member;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.CartException;
import com.ecommerce.exception.MemberException;
import com.ecommerce.exception.ProductException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.service.auth.AuthService;
import com.ecommerce.service.product.ProductService;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartItemServiceImplementTest {

  @Mock
  private CartItemRepository cartItemRepository;

  @Mock
  private AuthService authService;

  @Mock
  private ProductService productService;

  @Mock
  private CartService cartService;

  @InjectMocks
  private CartItemServiceImplement cartItemServiceImplement;

  @Test
  @DisplayName("장바구니에 상품 담기 - 성공 (cart, product 에 해당하는 cartItem 존재 X)")
  void testGetCartDetails_Success_NotFoundCartItem() {
    // given
    CartItemDto.Request request = CartItemDto.Request.builder()
        .productId(1L)
        .quantity(3)
        .build();

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

    Product product = Product.builder()
        .productName("testProductName1")
        .description("testProductDescription1")
        .stockQuantity(3)
        .price(BigDecimal.valueOf(10001.0))
        .status(ProductStatus.IN_STOCK)
        .rating(BigDecimal.ZERO)
        .member(new Member())
        .build();

    Cart cart = Cart.builder()
        .member(member)
        .cartItems(List.of())
        .build();

    CartItem cartItem = CartItem.builder()
        .cart(cart)
        .product(product)
        .price(product.getPrice().multiply(BigDecimal.valueOf(3)))
        .quantity(3)
        .build();

    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    given(cartService.getCartByMemberId(eq("testUser"))).willReturn(cart);
    given(productService.getProductById(eq(1L))).willReturn(product);
    given(cartItemRepository.findByCartAndProduct(eq(cart), eq(product)))
        .willReturn(Optional.empty());
    given(cartItemRepository.save(any(CartItem.class)))
        .willReturn(cartItem);

    ArgumentCaptor<CartItem> cartItemCaptor = ArgumentCaptor.forClass(CartItem.class);

    // when
    CartItemDto.Response response
        = cartItemServiceImplement.addCartItem("testUser", "token", request);

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(cartService, times(1))
        .getCartByMemberId(eq("testUser"));
    verify(productService, times(1))
        .getProductById(eq(1L));
    verify(cartItemRepository, times(1))
        .findByCartAndProduct(eq(cart), eq(product));
    verify(cartItemRepository, times(1))
        .save(cartItemCaptor.capture());

    assertThat(cartItemCaptor.getValue()).isNotNull();
    assertThat(cartItemCaptor.getValue().getCart()).isEqualTo(cart);
    assertThat(cartItemCaptor.getValue().getProduct()).isEqualTo(product);
    assertThat(cartItemCaptor.getValue().getQuantity()).isEqualTo(3);
    assertThat(cartItemCaptor.getValue().getPrice()).isEqualTo(product.getPrice());
  }

  @Test
  @DisplayName("장바구니에 상품 담기 - 성공 (cart, product 에 해당하는 cartItem 존재 O)")
  void testGetCartDetails_Success_FoundCartItem() {
    // given
    CartItemDto.Request request = CartItemDto.Request.builder()
        .productId(1L)
        .quantity(3)
        .build();

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

    Product product = Product.builder()
        .productName("testProductName1")
        .description("testProductDescription1")
        .stockQuantity(10)
        .price(BigDecimal.valueOf(10001.0))
        .status(ProductStatus.IN_STOCK)
        .rating(BigDecimal.ZERO)
        .member(new Member())
        .build();

    Cart cart = Cart.builder()
        .member(member)
        .cartItems(List.of())
        .build();

    CartItem cartItem = CartItem.builder()
        .cart(cart)
        .product(product)
        .price(product.getPrice().multiply(BigDecimal.valueOf(3)))
        .quantity(3)
        .build();

    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    given(cartService.getCartByMemberId(eq("testUser"))).willReturn(cart);
    given(productService.getProductById(eq(1L))).willReturn(product);
    given(cartItemRepository.findByCartAndProduct(eq(cart), eq(product)))
        .willReturn(Optional.of(cartItem));

    // when
    CartItemDto.Response response
        = cartItemServiceImplement.addCartItem("testUser", "token", request);

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(cartService, times(1))
        .getCartByMemberId(eq("testUser"));
    verify(productService, times(1))
        .getProductById(eq(1L));
    verify(cartItemRepository, times(1))
        .findByCartAndProduct(eq(cart), eq(product));

    assertThat(response.getQuantity()).isEqualTo(6);
  }

  @Test
  @DisplayName("장바구니에 상품 담기 - 실패 (토큰에 있는 멤버 정보와 불일치)")
  void testGetCartDetails_Fail_MemberUnMatched() {
    // given
    CartItemDto.Request request = CartItemDto.Request.builder()
        .productId(1L)
        .quantity(3)
        .build();

    doThrow(new MemberException(ResponseCode.MEMBER_UNMATCHED))
        .when(authService).equalToMemberIdFromToken(eq("testUser"), eq("token"));

    // when
    MemberException memberException = assertThrows(MemberException.class,
        () -> cartItemServiceImplement.addCartItem("testUser", "token", request));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    assertThat(memberException.getErrorCode()).isEqualTo(ResponseCode.MEMBER_UNMATCHED);
  }

  @Test
  @DisplayName("장바구니에 상품 담기 - 실패 (존재하지 않는 멤버)")
  void testGetCartDetails_Fail_MemberNotFound() {
    // given
    CartItemDto.Request request = CartItemDto.Request.builder()
        .productId(1L)
        .quantity(3)
        .build();

    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    doThrow(new MemberException(ResponseCode.MEMBER_NOT_FOUND))
        .when(cartService).getCartByMemberId(eq("testUser"));

    // when
    MemberException memberException = assertThrows(MemberException.class,
        () -> cartItemServiceImplement.addCartItem("testUser", "token", request));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(cartService, times(1))
        .getCartByMemberId(eq("testUser"));

    assertThat(memberException.getErrorCode()).isEqualTo(ResponseCode.MEMBER_NOT_FOUND);
  }

  @Test
  @DisplayName("장바구니에 상품 담기 - 실패 (존재하지 않는 장바구니)")
  void testGetCartDetails_Fail_CartNotFound() {
    // given
    CartItemDto.Request request = CartItemDto.Request.builder()
        .productId(1L)
        .quantity(3)
        .build();

    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    doThrow(new CartException(ResponseCode.CART_NOT_FOUND))
        .when(cartService).getCartByMemberId(eq("testUser"));

    // when
    CartException cartException = assertThrows(CartException.class,
        () -> cartItemServiceImplement.addCartItem("testUser", "token", request));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(cartService, times(1))
        .getCartByMemberId(eq("testUser"));

    assertThat(cartException.getErrorCode()).isEqualTo(ResponseCode.CART_NOT_FOUND);
  }

  @Test
  @DisplayName("장바구니에 상품 담기 - 실패 (존재하지 않는 상품)")
  void testGetCartDetails_Fail_ProductNotFound() {
    // given
    CartItemDto.Request request = CartItemDto.Request.builder()
        .productId(1L)
        .quantity(3)
        .build();

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

    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    given(cartService.getCartByMemberId(eq("testUser"))).willReturn(cart);

    doThrow(new ProductException(ResponseCode.PRODUCT_NOT_FOUND))
        .when(productService).getProductById(eq(1L));

    // when
    ProductException productException = assertThrows(ProductException.class,
        () -> cartItemServiceImplement.addCartItem("testUser", "token", request));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(cartService, times(1))
        .getCartByMemberId(eq("testUser"));
    verify(productService, times(1))
        .getProductById(eq(1L));

    assertThat(productException.getErrorCode()).isEqualTo(ResponseCode.PRODUCT_NOT_FOUND);
  }

  @Test
  @DisplayName("장바구니에 상품 담기 - 실패 (IN_STOCK 이 아닌 상품)")
  void testGetCartDetails_Fail_ProductDoesNotIN_STOCK() {
    // given
    CartItemDto.Request request = CartItemDto.Request.builder()
        .productId(1L)
        .quantity(3)
        .build();

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

    Cart cart = Cart.builder()
        .member(member)
        .cartItems(List.of())
        .build();

    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    given(cartService.getCartByMemberId(eq("testUser"))).willReturn(cart);
    given(productService.getProductById(eq(1L))).willReturn(product1);

    // when
    CartException cartException = assertThrows(CartException.class,
        () -> cartItemServiceImplement.addCartItem("testUser", "token", request));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(cartService, times(1))
        .getCartByMemberId(eq("testUser"));
    verify(productService, times(1))
        .getProductById(eq(1L));

    assertThat(cartException.getErrorCode()).isEqualTo(ResponseCode.CART_ITEM_CANNOT_ADDED_PRODUCT);
  }

  @Test
  @DisplayName("장바구니에 상품 담기 - 실패 (상품 재고 부족 - request 수량)")
  void testGetCartDetails_Fail_ExceedProductStockQuantity_RequestQuantity() {
    // given
    CartItemDto.Request request = CartItemDto.Request.builder()
        .productId(1L)
        .quantity(3)
        .build();

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

    Product product = Product.builder()
        .productName("testProductName1")
        .description("testProductDescription1")
        .stockQuantity(2)
        .price(BigDecimal.valueOf(10001.0))
        .status(ProductStatus.IN_STOCK)
        .rating(BigDecimal.ZERO)
        .member(new Member())
        .build();

    Cart cart = Cart.builder()
        .member(member)
        .cartItems(List.of())
        .build();

    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    given(cartService.getCartByMemberId(eq("testUser"))).willReturn(cart);
    given(productService.getProductById(eq(1L))).willReturn(product);

    // when
    CartException cartException = assertThrows(CartException.class,
        () -> cartItemServiceImplement.addCartItem("testUser", "token", request));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(cartService, times(1))
        .getCartByMemberId(eq("testUser"));
    verify(productService, times(1))
        .getProductById(eq(1L));

    assertThat(cartException.getErrorCode()).isEqualTo(ResponseCode.CART_ITEM_EXCEED_QUANTITY);
  }

  @Test
  @DisplayName("장바구니에 상품 담기 - 실패 (상품 재고 부족 - request 수량 + 기존 cartItem 수량)")
  void testGetCartDetails_Fail_ExceedProductStockQuantity_TotalQuantity() {
    // given
    CartItemDto.Request request = CartItemDto.Request.builder()
        .productId(1L)
        .quantity(3)
        .build();

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

    Product product = Product.builder()
        .productName("testProductName1")
        .description("testProductDescription1")
        .stockQuantity(3)
        .price(BigDecimal.valueOf(10001.0))
        .status(ProductStatus.IN_STOCK)
        .rating(BigDecimal.ZERO)
        .member(new Member())
        .build();

    Cart cart = Cart.builder()
        .member(member)
        .cartItems(List.of())
        .build();

    CartItem cartItem = CartItem.builder()
        .cart(cart)
        .product(product)
        .price(product.getPrice().multiply(BigDecimal.valueOf(3)))
        .quantity(3)
        .build();

    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    given(cartService.getCartByMemberId(eq("testUser"))).willReturn(cart);
    given(productService.getProductById(eq(1L))).willReturn(product);
    given(cartItemRepository.findByCartAndProduct(eq(cart), eq(product)))
        .willReturn(Optional.of(cartItem));

    // when
    CartException cartException = assertThrows(CartException.class,
        () -> cartItemServiceImplement.addCartItem("testUser", "token", request));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(cartService, times(1))
        .getCartByMemberId(eq("testUser"));
    verify(productService, times(1))
        .getProductById(eq(1L));
    verify(cartItemRepository, times(1))
        .findByCartAndProduct(eq(cart), eq(product));

    assertThat(cartException.getErrorCode()).isEqualTo(ResponseCode.CART_ITEM_EXCEED_QUANTITY);
  }

  @Test
  @DisplayName("장바구니 상품 수정 - 성공")
  void testUpdateCartItem_Success() {
    // given
    UpdateCartItemDto updateRequest = UpdateCartItemDto.builder()
        .quantity(2)
        .build();

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

    Product product = Product.builder()
        .productName("testProductName1")
        .description("testProductDescription1")
        .stockQuantity(3)
        .price(BigDecimal.valueOf(10001.0))
        .status(ProductStatus.IN_STOCK)
        .rating(BigDecimal.ZERO)
        .member(new Member())
        .build();

    Cart cart = Cart.builder()
        .member(member)
        .cartItems(List.of())
        .build();

    CartItem cartItem = CartItem.builder()
        .cart(cart)
        .product(product)
        .price(product.getPrice().multiply(BigDecimal.valueOf(3)))
        .quantity(1)
        .build();

    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    given(cartItemRepository.findById(eq(1L))).willReturn(Optional.of(cartItem));

    // when
    CartItemDto.Response response = cartItemServiceImplement
        .updateCartItem("testUser", 1L, "token", updateRequest);

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(cartItemRepository, times(1))
        .findById(eq(1L));

    assertThat(response.getQuantity()).isEqualTo(2);
  }

  @Test
  @DisplayName("장바구니 상품 수정 - 실패 (토큰에 있는 멤버 정보와 불일치)")
  void testUpdateCartItem_Fail_MemberUnMatched() {
    // given
    UpdateCartItemDto updateRequest = UpdateCartItemDto.builder()
        .quantity(2)
        .build();

    doThrow(new MemberException(ResponseCode.MEMBER_UNMATCHED))
        .when(authService).equalToMemberIdFromToken(eq("testUser"), eq("token"));

    // when
    MemberException memberException = assertThrows(MemberException.class, () -> cartItemServiceImplement
            .updateCartItem("testUser", 1L, "token", updateRequest));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    assertThat(memberException.getErrorCode()).isEqualTo(ResponseCode.MEMBER_UNMATCHED);
  }

  @Test
  @DisplayName("장바구니 상품 수정 - 실패 (존재하지 않는 장바구니 상품)")
  void testUpdateCartItem_Fail_CartItemNotFound() {
    // given
    UpdateCartItemDto updateRequest = UpdateCartItemDto.builder()
        .quantity(2)
        .build();

    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    doThrow(new CartException(ResponseCode.CART_ITEM_NOT_FOUND))
        .when(cartItemRepository).findById(eq(1L));

    // when
    CartException cartException = assertThrows(CartException.class, () -> cartItemServiceImplement
            .updateCartItem("testUser", 1L, "token", updateRequest));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(cartItemRepository, times(1))
        .findById(eq(1L));

    assertThat(cartException.getErrorCode()).isEqualTo(ResponseCode.CART_ITEM_NOT_FOUND);
  }

  @Test
  @DisplayName("장바구니 상품 수정 - 실패 (상품 재고 부족)")
  void testUpdateCartItem_Fail_ShortageProductStockQuantity() {
    // given
    UpdateCartItemDto updateRequest = UpdateCartItemDto.builder()
        .quantity(2)
        .build();

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

    Product product = Product.builder()
        .productName("testProductName1")
        .description("testProductDescription1")
        .stockQuantity(1)
        .price(BigDecimal.valueOf(10001.0))
        .status(ProductStatus.IN_STOCK)
        .rating(BigDecimal.ZERO)
        .member(new Member())
        .build();

    Cart cart = Cart.builder()
        .member(member)
        .cartItems(List.of())
        .build();

    CartItem cartItem = CartItem.builder()
        .cart(cart)
        .product(product)
        .price(product.getPrice().multiply(BigDecimal.valueOf(3)))
        .quantity(3)
        .build();

    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    given(cartItemRepository.findById(eq(1L))).willReturn(Optional.of(cartItem));

    // when
    CartException cartException = assertThrows(CartException.class, () -> cartItemServiceImplement
            .updateCartItem("testUser", 1L, "token", updateRequest));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(cartItemRepository, times(1))
        .findById(eq(1L));

    assertThat(cartException.getErrorCode()).isEqualTo(ResponseCode.CART_ITEM_EXCEED_QUANTITY);
  }

  @Test
  @DisplayName("장바구니 상품 삭제 - 성공")
  void testDeleteCartItem_Success() {
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
        .stockQuantity(1)
        .price(BigDecimal.valueOf(10001.0))
        .status(ProductStatus.IN_STOCK)
        .rating(BigDecimal.ZERO)
        .member(new Member())
        .build();

    Cart cart = Cart.builder()
        .member(member)
        .cartItems(List.of())
        .build();

    CartItem cartItem = CartItem.builder()
        .cart(cart)
        .product(product1)
        .price(product1.getPrice().multiply(BigDecimal.valueOf(3)))
        .quantity(3)
        .build();

    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    given(cartItemRepository.findById(eq(1L))).willReturn(Optional.of(cartItem));

    // when
    ResponseDto responseDto =
        cartItemServiceImplement.deleteCartItem("testUser", 1L, "token");

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(cartItemRepository, times(1))
        .findById(eq(1L));
    verify(cartItemRepository, times(1))
        .delete(eq(cartItem));

    assertThat(responseDto.getCode()).isEqualTo(ResponseCode.CART_ITEM_DELETE_SUCCESS);
  }

  @Test
  @DisplayName("장바구니 상품 삭제 - 실패 (토큰에 있는 멤버 정보와 불일치)")
  void testDeleteCartItem_Fail_MemberUnMatched() {
    // given
    doThrow(new MemberException(ResponseCode.MEMBER_UNMATCHED))
        .when(authService).equalToMemberIdFromToken(eq("testUser"), eq("token"));

    // when
    MemberException memberException = assertThrows(MemberException.class,
        () -> cartItemServiceImplement.deleteCartItem("testUser", 1L, "token"));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    assertThat(memberException.getErrorCode()).isEqualTo(ResponseCode.MEMBER_UNMATCHED);
  }

  @Test
  @DisplayName("장바구니 상품 삭제 - 실패 (존재하지 않는 장바구니 상품)")
  void testDeleteCartItem_Fail_CartItemNotFound() {
    // given
    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    doThrow(new CartException(ResponseCode.CART_NOT_FOUND))
        .when(cartItemRepository).findById(eq(1L));

    // when
    CartException cartException = assertThrows(CartException.class, () ->
        cartItemServiceImplement.deleteCartItem("testUser", 1L, "token"));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(cartItemRepository, times(1))
        .findById(eq(1L));

    assertThat(cartException.getErrorCode()).isEqualTo(ResponseCode.CART_NOT_FOUND);
  }

  @Test
  @DisplayName("장바구니 상품 전체 삭제 - 성공")
  void testDeleteAllCartItem_Success() {
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

    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    given(cartService.getCartByMemberId(eq("testUser"))).willReturn(cart);

    // when
    ResponseDto responseDto =
        cartItemServiceImplement.deleteAllCartItem("testUser", "token");

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(cartService, times(1))
        .getCartByMemberId(eq("testUser"));
    verify(cartItemRepository, times(1))
        .deleteAllByCart(eq(cart));

    assertThat(responseDto.getCode()).isEqualTo(ResponseCode.CART_ITEM_DELETE_SUCCESS);
  }

  @Test
  @DisplayName("장바구니 상품 전체 삭제 - 실패 (토큰에 있는 멤버 정보와 불일치)")
  void testDeleteAllCartItem_Fail_MemberUnMatched() {
    // given
    doThrow(new MemberException(ResponseCode.MEMBER_UNMATCHED))
        .when(authService).equalToMemberIdFromToken(eq("testUser"), eq("token"));

    // when
    MemberException memberException = assertThrows(MemberException.class,
        () -> cartItemServiceImplement.deleteAllCartItem("testUser", "token"));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    assertThat(memberException.getErrorCode()).isEqualTo(ResponseCode.MEMBER_UNMATCHED);
  }

  @Test
  @DisplayName("장바구니 상품 전체 삭제 - 실패 (존재하지 않는 멤버)")
  void testDeleteAllCartItem_Fail_MemberNotFound() {
    // given
    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    doThrow(new MemberException(ResponseCode.MEMBER_NOT_FOUND))
        .when(cartService).getCartByMemberId(eq("testUser"));

    // when
    MemberException memberException = assertThrows(MemberException.class,
        () -> cartItemServiceImplement.deleteAllCartItem("testUser", "token"));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(cartService, times(1))
        .getCartByMemberId(eq("testUser"));

    assertThat(memberException.getErrorCode()).isEqualTo(ResponseCode.MEMBER_NOT_FOUND);
  }

  @Test
  @DisplayName("장바구니 상품 전체 삭제 - 실패 (존재하지 않는 장바구니)")
  void testDeleteAllCartItem_Fail_CartNotFound() {
    // given
    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    doThrow(new CartException(ResponseCode.CART_NOT_FOUND))
        .when(cartService).getCartByMemberId(eq("testUser"));

    // when
    CartException cartException = assertThrows(CartException.class,
        () -> cartItemServiceImplement.deleteAllCartItem("testUser", "token"));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(cartService, times(1))
        .getCartByMemberId(eq("testUser"));

    assertThat(cartException.getErrorCode()).isEqualTo(ResponseCode.CART_NOT_FOUND);
  }

}