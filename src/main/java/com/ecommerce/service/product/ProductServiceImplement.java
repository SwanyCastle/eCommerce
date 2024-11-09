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
import com.ecommerce.type.SortType;
import java.math.BigDecimal;
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

  private static final int PRODUCT_PAGE_SIZE = 5;

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
                .status(request.getStatus())
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
   * @param sortType
   * @return List<ProductDto.Response>
   */
  @Override
  @Transactional(readOnly = true)
  public Page<ProductDto.Response> getProductList(
      Integer page, String search, ProductStatus status, SortType sortType
  ) {

    Sort sort = setSortType(sortType);

    Pageable pageable = PageRequest.of(page - 1, PRODUCT_PAGE_SIZE, sort);

    if (status != ProductStatus.NONE) {
      return productRepository.findByProductNameContainingAndStatus(search, status, pageable)
          .map(ProductDto.Response::fromEntity);
    }

    return productRepository.findByProductNameContaining(search, pageable)
        .map(ProductDto.Response::fromEntity);

  }

  /**
   * 판매자 상품 목록 조회 (특정 판매자의 상품 목록)
   *
   * @param memberId
   * @param page
   * @param search
   * @param sortType
   * @return List<ProductDto.Response>
   */
  @Override
  @Transactional(readOnly = true)
  public Page<ProductDto.Response> getProductListByMemberId(
      String memberId, Integer page, String search, ProductStatus status, SortType sortType
  ) {

    Sort sort = setSortType(sortType);

    Pageable pageable = PageRequest.of(page - 1, PRODUCT_PAGE_SIZE, sort);

    Member member = memberService.getMemberByMemberId(memberId);

    if (status != ProductStatus.NONE) {
      return productRepository
          .findByMemberAndProductNameContainingAndStatus(member, search, status, pageable)
          .map(ProductDto.Response::fromEntity);
    }

    return productRepository.findByMemberAndProductNameContaining(member, search, pageable)
        .map(ProductDto.Response::fromEntity);

  }

  /**
   * 상품 정렬 기준 설정
   *
   * @param sortType
   * @return Sort
   */
  private Sort setSortType(SortType sortType) {

    switch (sortType) {
      case LOW_PRICE -> {
        return Sort.by(Direction.ASC, "price");
      }
      case HIGH_PRICE -> {
        return Sort.by(Direction.DESC, "price");
      }
      case LOW_RATING -> {
        return Sort.by(Direction.ASC, "rating");
      }
      case HIGH_RATING -> {
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

    product.setProductName(updateRequest.getProductName());
    product.setDescription(updateRequest.getDescription());
    product.setStockQuantity(updateRequest.getStockQuantity());
    product.setPrice(updateRequest.getPrice());

    if (updateRequest.getStatus() != ProductStatus.DISABLE) {
      product.setStatus(setStatusByStockQuantity(product));
    } else {
      product.setStatus(updateRequest.getStatus());
    }

    return ProductDto.Response.fromEntity(product);

  }

  /**
   * 재고 상태에 따른 상품 상태 설정
   *
   * @param product
   * @return ProductStatus
   */
  private ProductStatus setStatusByStockQuantity(Product product) {

    if (product.getStockQuantity() == 0) {
      return ProductStatus.NO_STOCK;
    } else {
      return ProductStatus.IN_STOCK;
    }

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
