package com.ecommerce.service.review;

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
import com.ecommerce.dto.review.ReviewDto;
import com.ecommerce.dto.review.ReviewDto.Response;
import com.ecommerce.dto.review.UpdateReviewDto;
import com.ecommerce.entity.Member;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.Review;
import com.ecommerce.exception.MemberException;
import com.ecommerce.exception.ProductException;
import com.ecommerce.exception.ReviewException;
import com.ecommerce.repository.review.ReviewRepository;
import com.ecommerce.service.auth.AuthService;
import com.ecommerce.service.member.MemberService;
import com.ecommerce.service.product.ProductService;
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
class ReviewServiceImplementTest {

  private static final int TEST_REVIEW_PAGE_SIZE = 3;

  @Mock
  private ReviewRepository reviewRepository;

  @Mock
  private AuthService authService;

  @Mock
  private MemberService memberService;

  @Mock
  private ProductService productService;

  @InjectMocks
  private ReviewServiceImplement reviewServiceImplement;

  @Test
  @DisplayName("리뷰 등록 - 성공")
  void testCreateReview_Success() {
    // given
    ReviewDto.Request request = ReviewDto.Request.builder()
        .productId(1L)
        .content("testReviewContent")
        .rating(BigDecimal.valueOf(4))
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
        .productName("testProductName")
        .description("testProductDescription")
        .stockQuantity(3)
        .price(BigDecimal.valueOf(10000.0))
        .status(ProductStatus.IN_STOCK)
        .rating(BigDecimal.ZERO)
        .member(member)
        .build();

    Review review = Review.builder()
        .member(member)
        .product(product)
        .content(request.getContent())
        .rating(request.getRating())
        .build();

    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    given(memberService.getMemberByMemberId(eq("testUser")))
        .willReturn(member);
    given(productService.getProductById(eq(1L)))
        .willReturn(product);
    given(reviewRepository.existsByMemberAndProduct(eq(member), eq(product)))
        .willReturn(false);
    given(reviewRepository.save(any(Review.class)))
        .willReturn(review);
    given(reviewRepository.findAverageRatingByProductId(eq(1L)))
        .willReturn(BigDecimal.valueOf(4.125));

    ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);

    // when
    ReviewDto.Response savedReview = reviewServiceImplement
        .createReview("testUser", "token", request);

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(memberService, times(1))
        .getMemberByMemberId(eq("testUser"));
    verify(productService, times(1))
        .getProductById(eq(1L));
    verify(reviewRepository, times(1))
        .existsByMemberAndProduct(eq(member), eq(product));
    verify(reviewRepository, times(1))
        .save(reviewCaptor.capture());
    verify(reviewRepository, times(1))
        .findAverageRatingByProductId(eq(1L));

    assertThat(reviewCaptor.getValue()).isNotNull();
    assertThat(reviewCaptor.getValue().getContent()).isEqualTo("testReviewContent");
    assertThat(reviewCaptor.getValue().getRating()).isEqualTo(BigDecimal.valueOf(4));
    assertThat(reviewCaptor.getValue().getProduct().getRating())
        .isEqualTo(BigDecimal.valueOf(4.125));
    assertThat(product.getRating()).isEqualTo(BigDecimal.valueOf(4.125));
    assertThat(savedReview.getMemberId()).isEqualTo("testUser");
    assertThat(savedReview.getProductName()).isEqualTo("testProductName");
  }

  @Test
  @DisplayName("리뷰 등록 - 실패 (토큰에 있는 멤버 정보와 불일치)")
  void testCreateReview_Fail_MemberUnMatched() {
    // given
    ReviewDto.Request request = ReviewDto.Request.builder()
        .productId(1L)
        .content("testReviewContent")
        .rating(BigDecimal.valueOf(4))
        .build();

    doThrow(new MemberException(ResponseCode.MEMBER_UNMATCHED))
        .when(authService).equalToMemberIdFromToken(eq("testUser"), eq("token"));

    // when
    MemberException memberException = assertThrows(MemberException.class,
        () -> reviewServiceImplement
            .createReview("testUser", "token", request));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    assertThat(memberException.getErrorCode()).isEqualTo(ResponseCode.MEMBER_UNMATCHED);
  }

  @Test
  @DisplayName("리뷰 등록 - 실패 (존재하지 않는 멤버)")
  void testCreateReview_Fail_MemberNotFound() {
    // given
    ReviewDto.Request request = ReviewDto.Request.builder()
        .productId(1L)
        .content("testReviewContent")
        .rating(BigDecimal.valueOf(4))
        .build();

    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    doThrow(new MemberException(ResponseCode.MEMBER_NOT_FOUND))
        .when(memberService).getMemberByMemberId(eq("testUser"));

    // when
    MemberException memberException = assertThrows(MemberException.class,
        () -> reviewServiceImplement
            .createReview("testUser", "token", request));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(memberService, times(1))
        .getMemberByMemberId(eq("testUser"));

    assertThat(memberException.getErrorCode()).isEqualTo(ResponseCode.MEMBER_NOT_FOUND);
  }

  @Test
  @DisplayName("리뷰 등록 - 실패 (존재하지 않는 상품)")
  void testCreateReview_Fail_ProductNotFound() {
    // given
    ReviewDto.Request request = ReviewDto.Request.builder()
        .productId(1L)
        .content("testReviewContent")
        .rating(BigDecimal.valueOf(4))
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

    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    given(memberService.getMemberByMemberId(eq("testUser")))
        .willReturn(member);

    doThrow(new ProductException(ResponseCode.PRODUCT_NOT_FOUND))
        .when(productService).getProductById(eq(1L));

    // when
    ProductException productException = assertThrows(ProductException.class,
        () -> reviewServiceImplement
            .createReview("testUser", "token", request));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(memberService, times(1))
        .getMemberByMemberId(eq("testUser"));
    verify(productService, times(1))
        .getProductById(eq(1L));

    assertThat(productException.getErrorCode()).isEqualTo(ResponseCode.PRODUCT_NOT_FOUND);
  }

  @Test
  @DisplayName("리뷰 등록 - 실패 (이미 존재하는 리뷰)")
  void testCreateReview_Fail_ExistsReview() {
    // given
    ReviewDto.Request request = ReviewDto.Request.builder()
        .productId(1L)
        .content("testReviewContent")
        .rating(BigDecimal.valueOf(4))
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
        .productName("testProductName")
        .description("testProductDescription")
        .stockQuantity(3)
        .price(BigDecimal.valueOf(10000.0))
        .status(ProductStatus.IN_STOCK)
        .rating(BigDecimal.ZERO)
        .member(member)
        .build();

    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    given(memberService.getMemberByMemberId(eq("testUser")))
        .willReturn(member);
    given(productService.getProductById(eq(1L)))
        .willReturn(product);
    given(reviewRepository.existsByMemberAndProduct(eq(member), eq(product)))
        .willReturn(true);

    // when
    ReviewException reviewException = assertThrows(ReviewException.class,
        () -> reviewServiceImplement
            .createReview("testUser", "token", request));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(memberService, times(1))
        .getMemberByMemberId(eq("testUser"));
    verify(productService, times(1))
        .getProductById(eq(1L));
    verify(reviewRepository, times(1))
        .existsByMemberAndProduct(eq(member), eq(product));

    assertThat(reviewException.getErrorCode()).isEqualTo(ResponseCode.REVIEW_ALREADY_EXISTS);
  }

  @Test
  @DisplayName("리뷰 정보 조회 - 성공")
  void testGetReviewDetail_Success() {
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
        .price(BigDecimal.valueOf(10000.0))
        .status(ProductStatus.IN_STOCK)
        .rating(BigDecimal.ZERO)
        .member(member)
        .build();

    Review review = Review.builder()
        .member(member)
        .product(product)
        .content("testReviewContent")
        .rating(BigDecimal.valueOf(4))
        .build();

    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq("otherTestUser"), eq("token"));

    given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

    // when
    ReviewDto.Response reviewDetail = reviewServiceImplement
        .getReviewDetail(1L, "otherTestUser", "token");

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("otherTestUser"), eq("token"));
    verify(reviewRepository, times(1))
        .findById(eq(1L));

    assertThat(reviewDetail.getMemberId()).isEqualTo("testUser");
    assertThat(reviewDetail.getProductName()).isEqualTo("testProductName");
    assertThat(reviewDetail.getContent()).isEqualTo("testReviewContent");
    assertThat(reviewDetail.getRating()).isEqualTo(BigDecimal.valueOf(4));
  }

  @Test
  @DisplayName("리뷰 정보 조회 - 실패 (토큰에 있는 멤버 정보와 불일치)")
  void testGetReviewDetail_Fail_MemberUnMatched() {
    // given
    doThrow(new MemberException(ResponseCode.MEMBER_UNMATCHED))
        .when(authService).equalToMemberIdFromToken(eq("otherTestUser"), eq("token"));

    // when
    MemberException memberException = assertThrows(MemberException.class,
        () -> reviewServiceImplement
            .getReviewDetail(1L, "otherTestUser", "token"));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("otherTestUser"), eq("token"));

    assertThat(memberException.getErrorCode()).isEqualTo(ResponseCode.MEMBER_UNMATCHED);
  }

  @Test
  @DisplayName("리뷰 정보 조회 - 실패 (존재하지 않는 리뷰)")
  void testGetReviewDetail_Fail_ReviewNotFound() {
    // given
    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq("otherTestUser"), eq("token"));

    given(reviewRepository.findById(1L)).willReturn(Optional.empty());

    // when
    ReviewException reviewException = assertThrows(ReviewException.class,
        () -> reviewServiceImplement
            .getReviewDetail(1L, "otherTestUser", "token"));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("otherTestUser"), eq("token"));
    verify(reviewRepository, times(1))
        .findById(eq(1L));

    assertThat(reviewException.getErrorCode()).isEqualTo(ResponseCode.REVIEW_NOT_FOUND);
  }

  @Test
  @DisplayName("리뷰 정보 수정 - 성공")
  void testUpdateReview_Success() {
    // given
    UpdateReviewDto updateRequest = UpdateReviewDto.builder()
        .content("updateTestReviewContent")
        .rating(BigDecimal.valueOf(3))
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
        .productName("testProductName")
        .description("testProductDescription")
        .stockQuantity(3)
        .price(BigDecimal.valueOf(10000.0))
        .status(ProductStatus.IN_STOCK)
        .rating(BigDecimal.ZERO)
        .member(member)
        .build();

    Review review = Review.builder()
        .member(member)
        .product(product)
        .content("testReviewContent")
        .rating(BigDecimal.valueOf(4))
        .build();

    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    given(reviewRepository.findById(eq(1L)))
        .willReturn(Optional.of(review));
    given(reviewRepository.findAverageRatingByProductId(eq(review.getProduct().getId())))
        .willReturn(BigDecimal.valueOf(3.833));

    // when
    ReviewDto.Response updateReview = reviewServiceImplement
        .updateReview(1L, "testUser", "token", updateRequest);

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(reviewRepository, times(1))
        .findById(eq(1L));
    verify(reviewRepository, times(1))
        .findAverageRatingByProductId(eq(review.getProduct().getId()));

    assertThat(updateReview.getContent()).isEqualTo("updateTestReviewContent");
    assertThat(updateReview.getRating()).isEqualTo(BigDecimal.valueOf(3));
    assertThat(updateReview.getMemberId()).isEqualTo("testUser");
    assertThat(updateReview.getProductName()).isEqualTo("testProductName");
    assertThat(product.getRating()).isEqualTo(BigDecimal.valueOf(3.833));
  }

  @Test
  @DisplayName("리뷰 정보 수정 - 실패 (토큰에 있는 멤버 정보와 불일치)")
  void testUpdateReview_Fail_MemberUnMatched() {
    // given
    UpdateReviewDto updateRequest = UpdateReviewDto.builder()
        .content("updateTestReviewContent")
        .rating(BigDecimal.valueOf(3))
        .build();

    doThrow(new MemberException(ResponseCode.MEMBER_UNMATCHED))
        .when(authService).equalToMemberIdFromToken(eq("testUser"), eq("token"));

    // when
    MemberException memberException = assertThrows(MemberException.class,
        () -> reviewServiceImplement
            .updateReview(1L, "testUser", "token", updateRequest));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    assertThat(memberException.getErrorCode()).isEqualTo(ResponseCode.MEMBER_UNMATCHED);
  }

  @Test
  @DisplayName("리뷰 정보 수정 - 실패 (존재하지 않는 리뷰)")
  void testUpdateReview_Fail_ReviewNotFound() {
    // given
    UpdateReviewDto updateRequest = UpdateReviewDto.builder()
        .content("updateTestReviewContent")
        .rating(BigDecimal.valueOf(3))
        .build();

    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    given(reviewRepository.findById(eq(1L)))
        .willReturn(Optional.empty());

    // when
    ReviewException reviewException = assertThrows(ReviewException.class,
        () -> reviewServiceImplement
            .updateReview(1L, "testUser", "token", updateRequest));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(reviewRepository, times(1))
        .findById(eq(1L));

    assertThat(reviewException.getErrorCode()).isEqualTo(ResponseCode.REVIEW_NOT_FOUND);
  }

  @Test
  @DisplayName("리뷰 정보 수정 - 실패 (요청한 멤버와 작성자 불일치)")
  void testUpdateReview_Fail_AuthorUnMatched() {
    // given
    UpdateReviewDto updateRequest = UpdateReviewDto.builder()
        .content("updateTestReviewContent")
        .rating(BigDecimal.valueOf(3))
        .build();

    Member member = Member.builder()
        .memberId("otherTestUser")
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
        .price(BigDecimal.valueOf(10000.0))
        .status(ProductStatus.IN_STOCK)
        .rating(BigDecimal.ZERO)
        .member(member)
        .build();

    Review review = Review.builder()
        .member(member)
        .product(product)
        .content("testReviewContent")
        .rating(BigDecimal.valueOf(4))
        .build();

    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    given(reviewRepository.findById(eq(1L)))
        .willReturn(Optional.of(review));

    // when
    ReviewException reviewException = assertThrows(ReviewException.class,
        () -> reviewServiceImplement
            .updateReview(1L, "testUser", "token", updateRequest));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(reviewRepository, times(1))
        .findById(eq(1L));

    assertThat(reviewException.getErrorCode()).isEqualTo(ResponseCode.REVIEW_UNMATCHED_MEMBER);
  }

  @Test
  @DisplayName("리뷰 정보 삭제 - 성공")
  void testDeleteReview_Success() {
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

    Product product = Product.builder()
        .productName("testProductName")
        .description("testProductDescription")
        .stockQuantity(3)
        .price(BigDecimal.valueOf(10000.0))
        .status(ProductStatus.IN_STOCK)
        .rating(BigDecimal.ZERO)
        .member(member)
        .build();

    Review review = Review.builder()
        .member(member)
        .product(product)
        .content("testReviewContent")
        .rating(BigDecimal.valueOf(4))
        .build();

    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    given(reviewRepository.findById(eq(1L)))
        .willReturn(Optional.of(review));
    given(reviewRepository.findAverageRatingByProductId(eq(review.getProduct().getId())))
        .willReturn(BigDecimal.valueOf(3.5));

    // when
    ResponseDto responseDto = reviewServiceImplement
        .deleteReview(1L, "testUser", "token");

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(reviewRepository, times(1))
        .findById(eq(1L));
    verify(reviewRepository, times(1))
        .findAverageRatingByProductId(eq(review.getProduct().getId()));
    verify(reviewRepository, times(1))
        .delete(eq(review));

    assertThat(product.getRating()).isEqualTo(BigDecimal.valueOf(3.5));
    assertThat(responseDto.getCode()).isEqualTo(ResponseCode.REVIEW_DELETE_SUCCESS);
  }

  @Test
  @DisplayName("리뷰 정보 삭제 - 실패 (토큰에 있는 멤버 정보와 불일치)")
  void testDeleteReview_Fail_MemberUnMatched() {
    // given
    doThrow(new MemberException(ResponseCode.MEMBER_UNMATCHED))
        .when(authService).equalToMemberIdFromToken(eq("testUser"), eq("token"));

    // when
    MemberException memberException = assertThrows(MemberException.class,
        () -> reviewServiceImplement
            .deleteReview(1L, "testUser", "token"));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    assertThat(memberException.getErrorCode()).isEqualTo(ResponseCode.MEMBER_UNMATCHED);
  }

  @Test
  @DisplayName("리뷰 정보 삭제 - 실패 (존재하지 않는 리뷰)")
  void testDeleteReview_Fail_ReviewNotFound() {
    // given
    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    given(reviewRepository.findById(eq(1L)))
        .willReturn(Optional.empty());

    // when
    ReviewException reviewException = assertThrows(ReviewException.class,
        () -> reviewServiceImplement
            .deleteReview(1L, "testUser", "token"));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(reviewRepository, times(1))
        .findById(eq(1L));

    assertThat(reviewException.getErrorCode()).isEqualTo(ResponseCode.REVIEW_NOT_FOUND);
  }

  @Test
  @DisplayName("리뷰 정보 삭제 - 실패 (요청한 멤버와 작성자 불일치)")
  void testDeleteReview_Fail_AuthorUnMatched() {
    // given
    Member member = Member.builder()
        .memberId("otherTestUser")
        .memberName("test")
        .email("test@email.com")
        .password("encodedPassword")
        .phoneNumber("01011112222")
        .address("test시 test구 test로 111")
        .role(Role.CUSTOMER)
        .loginType(LoginType.APP)
        .build();

    Product product = Product.builder()
        .productName("testProductName")
        .description("testProductDescription")
        .stockQuantity(3)
        .price(BigDecimal.valueOf(10000.0))
        .status(ProductStatus.IN_STOCK)
        .rating(BigDecimal.ZERO)
        .member(member)
        .build();

    Review review = Review.builder()
        .member(member)
        .product(product)
        .content("testReviewContent")
        .rating(BigDecimal.valueOf(4))
        .build();

    willDoNothing().given(authService)
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));

    given(reviewRepository.findById(eq(1L)))
        .willReturn(Optional.of(review));

    // when
    ReviewException reviewException = assertThrows(ReviewException.class,
        () -> reviewServiceImplement
            .deleteReview(1L, "testUser", "token"));

    // then
    verify(authService, times(1))
        .equalToMemberIdFromToken(eq("testUser"), eq("token"));
    verify(reviewRepository, times(1))
        .findById(eq(1L));

    assertThat(reviewException.getErrorCode()).isEqualTo(ResponseCode.REVIEW_UNMATCHED_MEMBER);
  }

  @Test
  @DisplayName("특정 상품에 대한 리뷰 목록 조회 - 성공")
  void testGetReviewsByProduct_Success() {
    // given
    Sort sort = Sort.by(Direction.DESC, "createdAt");
    Pageable pageable = PageRequest.of(0, TEST_REVIEW_PAGE_SIZE, sort);

    Member member = Member.builder()
        .memberId("otherTestUser")
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
        .price(BigDecimal.valueOf(10000.0))
        .status(ProductStatus.IN_STOCK)
        .rating(BigDecimal.ZERO)
        .member(member)
        .build();

    List<Review> mockReviews = List.of(
        Review.builder()
            .member(new Member())
            .product(product)
            .content("testReviewContent1")
            .rating(BigDecimal.valueOf(4))
            .build(),
        Review.builder()
            .member(new Member())
            .product(product)
            .content("testReviewContent2")
            .rating(BigDecimal.valueOf(3))
            .build(),
        Review.builder()
            .member(new Member())
            .product(product)
            .content("testReviewContent3")
            .rating(BigDecimal.valueOf(4.5))
            .build(),
        Review.builder()
            .member(new Member())
            .product(product)
            .content("testReviewContent4")
            .rating(BigDecimal.valueOf(3.5))
            .build(),
        Review.builder()
            .member(new Member())
            .product(product)
            .content("testReviewContent5")
            .rating(BigDecimal.valueOf(5))
            .build()
        );

    Page<Review> reviews = new PageImpl<>(mockReviews);

    given(productService.getProductById(eq(1L)))
        .willReturn(product);
    given(reviewRepository.findByProduct(product, pageable))
        .willReturn(reviews);

    // when
    Page<Response> reviewsByProduct = reviewServiceImplement
        .getReviewsByProduct(1L, 1, SortType.LATEST);

    // then
    verify(productService, times(1))
        .getProductById(eq(1L));
    verify(reviewRepository, times(1))
        .findByProduct(eq(product), eq(pageable));

    assertThat(reviewsByProduct).isNotNull();
    assertThat(reviewsByProduct.getSize()).isEqualTo(5);
  }

  @Test
  @DisplayName("특정 상품에 대한 리뷰 목록 조회 - 성공 (해당 결과 값 없음)")
  void testGetReviewsByProduct_Success_NoResult() {
    // given
    Sort sort = Sort.by(Direction.DESC, "createdAt");
    Pageable pageable = PageRequest.of(0, TEST_REVIEW_PAGE_SIZE, sort);

    Member member = Member.builder()
        .memberId("otherTestUser")
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
        .price(BigDecimal.valueOf(10000.0))
        .status(ProductStatus.IN_STOCK)
        .rating(BigDecimal.ZERO)
        .member(member)
        .build();

    List<Review> mockReviews = List.of();

    Page<Review> reviews = new PageImpl<>(mockReviews);

    given(productService.getProductById(eq(1L)))
        .willReturn(product);
    given(reviewRepository.findByProduct(product, pageable))
        .willReturn(reviews);

    // when
    Page<Response> reviewsByProduct = reviewServiceImplement
        .getReviewsByProduct(1L, 1, SortType.LATEST);

    // then
    verify(productService, times(1))
        .getProductById(eq(1L));
    verify(reviewRepository, times(1))
        .findByProduct(eq(product), eq(pageable));

    assertThat(reviewsByProduct).isNotNull();
    assertThat(reviewsByProduct.getSize()).isEqualTo(0);
  }

  @Test
  @DisplayName("특정 회원이 작성한 리뷰 목록 조회 - 성공")
  void testGetReviewsByMember_Success() {
    // given
    Sort sort = Sort.by(Direction.DESC, "createdAt");
    Pageable pageable = PageRequest.of(0, TEST_REVIEW_PAGE_SIZE, sort);

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

    List<Review> mockReviews = List.of(
        Review.builder()
            .member(member)
            .product(new Product())
            .content("testReviewContent1")
            .rating(BigDecimal.valueOf(4))
            .build(),
        Review.builder()
            .member(member)
            .product(new Product())
            .content("testReviewContent2")
            .rating(BigDecimal.valueOf(3))
            .build(),
        Review.builder()
            .member(member)
            .product(new Product())
            .content("testReviewContent3")
            .rating(BigDecimal.valueOf(4.5))
            .build(),
        Review.builder()
            .member(member)
            .product(new Product())
            .content("testReviewContent4")
            .rating(BigDecimal.valueOf(3.5))
            .build(),
        Review.builder()
            .member(member)
            .product(new Product())
            .content("testReviewContent5")
            .rating(BigDecimal.valueOf(5))
            .build()
    );

    Page<Review> reviews = new PageImpl<>(mockReviews);

    given(memberService.getMemberByMemberId(eq("testUser")))
        .willReturn(member);
    given(reviewRepository.findByMember(member, pageable))
        .willReturn(reviews);

    // when
    Page<ReviewDto.Response> reviewsByProduct = reviewServiceImplement
        .getReviewsByMember("testUser", 1, SortType.LATEST);

    // then
    verify(memberService, times(1))
        .getMemberByMemberId(eq("testUser"));
    verify(reviewRepository, times(1))
        .findByMember(eq(member), eq(pageable));

    assertThat(reviewsByProduct).isNotNull();
    assertThat(reviewsByProduct.getSize()).isEqualTo(5);
  }

  @Test
  @DisplayName("특정 회원이 작성한 리뷰 목록 조회 - 성공 (해당 결과 값 없음)")
  void testGetReviewsByMember_Success_NoResult() {
    // given
    Sort sort = Sort.by(Direction.DESC, "createdAt");
    Pageable pageable = PageRequest.of(0, TEST_REVIEW_PAGE_SIZE, sort);

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

    List<Review> mockReviews = List.of();

    Page<Review> reviews = new PageImpl<>(mockReviews);

    given(memberService.getMemberByMemberId(eq("testUser")))
        .willReturn(member);
    given(reviewRepository.findByMember(member, pageable))
        .willReturn(reviews);

    // when
    Page<ReviewDto.Response> reviewsByProduct = reviewServiceImplement
        .getReviewsByMember("testUser", 1, SortType.LATEST);

    // then
    verify(memberService, times(1))
        .getMemberByMemberId(eq("testUser"));
    verify(reviewRepository, times(1))
        .findByMember(eq(member), eq(pageable));

    assertThat(reviewsByProduct).isNotNull();
    assertThat(reviewsByProduct.getSize()).isEqualTo(0);
  }

}