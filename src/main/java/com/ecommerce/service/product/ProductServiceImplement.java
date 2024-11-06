package com.ecommerce.service.product;

import com.ecommerce.dto.ResponseDto;
import com.ecommerce.dto.product.ProductDto;
import com.ecommerce.dto.product.UpdateProductDto;
import com.ecommerce.entity.Member;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ProductException;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.auth.AuthService;
import com.ecommerce.service.member.MemberService;
import com.ecommerce.type.ProductStatus;
import com.ecommerce.type.ResponseCode;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductServiceImplement implements ProductService {

  private static final int PAGE_SIZE = 5;

  private final AuthService authService;
  private final MemberService memberService;
  private final ProductRepository productRepository;

  /**
   * 상품 등록
   *
   * @param request
   * @return ProductDto.Response
   */
  @Override
  @Transactional
  public ProductDto.Response createProduct(String memberId, String token,
      ProductDto.Request request) {

    authService.equalToMemberIdFromToken(memberId, token);

    Member member = memberService.getMemberByMemberId(memberId);

    return ProductDto.Response.fromEntity(
        productRepository.save(
            Product.builder()
                .productName(request.getProductName())
                .description(request.getDescription())
                .stockQuantity(request.getStockQuantity())
                .price(BigDecimal.valueOf(request.getPrice().doubleValue()))
                .status(ProductStatus.IN_STOCK)
                .rating(BigDecimal.valueOf(0))
                .member(member)
                .build()
        )
    );

  }

  /**
   * 전체 상품 목록 조회 (전체 상품 목록)
   *
   * @param page
   * @param search
   * @param ordering
   * @return List<ProductDto.Response>
   */
  @Override
  @Transactional(readOnly = true)
  public List<ProductDto.Response> getProductList(
      Integer page, String search, String status, String ordering
  ) {

    Sort sortType = setSortType(ordering);

    ProductStatus productStatus = status.isEmpty() ? null : ProductStatus.valueOf(status);

    Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE, sortType);

    Page<Product> products = getProducts(search, productStatus, pageable);

    return products.getContent().stream()
        .map(ProductDto.Response::fromEntity)
        .toList();

  }

  /**
   * 전체 상품 목록 조회 (상품 상태 확인)
   *
   * @param search
   * @param productStatus
   * @param pageable
   * @return Page<Product>
   */
  private Page<Product> getProducts(String search, ProductStatus productStatus, Pageable pageable) {

    if (productStatus != null) {
      return productRepository.findByProductNameContainingAndStatus(search, productStatus,
          pageable);
    }

    return productRepository.findByProductNameContaining(search, pageable);

  }

  /**
   * 판매자 상품 목록 조회 (특정 판매자의 상품 목록)
   *
   * @param memberId
   * @param page
   * @param search
   * @param ordering
   * @return List<ProductDto.Response>
   */
  @Override
  @Transactional(readOnly = true)
  public List<ProductDto.Response> getProductListByMemberId(
      String memberId, Integer page, String search, String status, String ordering
  ) {
    Sort sortType = setSortType(ordering);

    ProductStatus productStatus = status.isEmpty() ? null : ProductStatus.valueOf(status);

    Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE, sortType);

    Page<Product> products = getProductsByMemberId(memberId, search, productStatus, pageable);

    return products.getContent().stream()
        .map(ProductDto.Response::fromEntity)
        .toList();
  }

  /**
   * 판매자 상품 목록 조회 (상품 상태 확인)
   *
   * @param memberId
   * @param search
   * @param productStatus
   * @param pageable
   * @return Page<Product>
   */
  private Page<Product> getProductsByMemberId(
      String memberId, String search, ProductStatus productStatus, Pageable pageable
  ) {

    Member member = memberService.getMemberByMemberId(memberId);

    if (productStatus != null) {
      return productRepository.findByMemberAndProductNameContainingAndStatus(member, search,
          productStatus,
          pageable);
    }

    return productRepository.findByMemberAndProductNameContaining(member, search, pageable);

  }

  /**
   * 정렬 타입 세팅
   *
   * @param ordering
   * @return Sort
   */
  private Sort setSortType(String ordering) {
    switch (ordering) {

      case "priceLow" -> {
        return Sort.by(Direction.ASC, "price");
      }

      case "priceHigh" -> {
        return Sort.by(Direction.DESC, "price");
      }

      case "ratingLow" -> {
        return Sort.by(Direction.ASC, "rating");
      }

      case "ratingHigh" -> {
        return Sort.by(Direction.DESC, "rating");
      }

      default -> {
        return Sort.by(Direction.DESC, "createdAt");
      }

    }
  }

  /**
   * 상품 정보 조회
   *
   * @param productId
   * @return ProductDto.Response
   */
  @Override
  @Transactional(readOnly = true)
  public ProductDto.Response getProductDetails(Long productId) {

    return ProductDto.Response.fromEntity(getProductById(productId));

  }

  /**
   * 상품 정보 수정
   *
   * @param productId
   * @param updateRequest
   * @return ProductDto.Response
   */
  @Override
  @Transactional
  public ProductDto.Response updateProduct(
      Long productId, String token, UpdateProductDto updateRequest
  ) {

    Product product = getProductById(productId);

    authService.equalToMemberIdFromToken(product.getMember().getMemberId(), token);

    if (updateRequest.getProductName() != null && !updateRequest.getProductName().isEmpty()) {
      product.setProductName(updateRequest.getProductName());
    }

    if (updateRequest.getDescription() != null && !updateRequest.getDescription().isEmpty()) {
      product.setDescription(updateRequest.getDescription());
    }

    if (updateRequest.getStockQuantity() != null) {
      product.setStockQuantity(updateRequest.getStockQuantity());
    }

    if (updateRequest.getPrice() != null) {
      product.setPrice(updateRequest.getPrice());
    }

    if (updateRequest.getStatus() != null) {
      product.setStatus(updateRequest.getStatus());
    } else {
      if (product.getStockQuantity() == 0) {
        product.setStatus(ProductStatus.NO_STOCK);
      } else {
        product.setStatus(ProductStatus.IN_STOCK);
      }
    }

    return ProductDto.Response.fromEntity(product);

  }

  /**
   * 상품 정보 삭제
   *
   * @param productId
   * @return ResponseDto
   */
  @Override
  @Transactional
  public ResponseDto deleteProduct(Long productId, String token) {

    Product product = getProductById(productId);

    authService.equalToMemberIdFromToken(product.getMember().getMemberId(), token);

    productRepository.delete(product);

    return ResponseDto.getResponseBody(ResponseCode.PRODUCT_DELETE_SUCCESS);

  }

  /**
   * 상품 정보 조회 (상품 id)
   *
   * @param productId
   * @return Product
   */
  @Override
  @Transactional(readOnly = true)
  public Product getProductById(Long productId) {

    return productRepository.findById(productId)
        .orElseThrow(() -> new ProductException(ResponseCode.PRODUCT_NOT_FOUND));

  }
}
