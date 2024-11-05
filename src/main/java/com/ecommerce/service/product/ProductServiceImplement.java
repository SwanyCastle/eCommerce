package com.ecommerce.service.product;

import com.ecommerce.dto.ResponseDto;
import com.ecommerce.dto.product.ProductDto;
import com.ecommerce.dto.product.UpdateProductDto;
import com.ecommerce.entity.Member;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ProductException;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.member.MemberService;
import com.ecommerce.type.ResponseCode;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductServiceImplement implements ProductService {

  private final MemberService memberService;
  private final ProductRepository productRepository;

  /**
   * 상품 등록
   * @param request
   * @return ProductDto.Response
   */
  @Override
  @Transactional
  public ProductDto.Response createProduct(ProductDto.Request request) {

    Member member = memberService.getMemberByMemberId(request.getMemberId());

    return ProductDto.Response.fromEntity(
        productRepository.save(
            Product.builder()
                .productName(request.getProductName())
                .description(request.getDescription())
                .stockQuantity(request.getStockQuantity())
                .price(request.getPrice())
                .status(request.getStatus())
                .rating(BigDecimal.valueOf(0))
                .member(member)
                .build()
        )
    );

  }

  /**
   * 상품 목록 조회 (검색)
   * @param page
   * @param search
   * @param ordering
   * @return List<ProductDto.Response>
   */
  @Override
  public List<ProductDto.Response> getProductList(Integer page, String search, String ordering) {
    return List.of();
  }

  /**
   * 상품 목록 조회 (판매자 전용)
   * @param memberId
   * @param page
   * @param search
   * @param ordering
   * @return List<ProductDto.Response>
   */
  @Override
  public List<ProductDto.Response> getProductListByMemberId(String memberId, Integer page,
      String search,
      String ordering) {
    return List.of();
  }

  /**
   * 상품 정보 조회
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
   * @param productId
   * @param updateRequest
   * @return ProductDto.Response
   */
  @Override
  @Transactional
  public ProductDto.Response updateProduct(Long productId, UpdateProductDto updateRequest) {

    Product product = getProductById(productId);

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
    }

    return ProductDto.Response.fromEntity(product);
  }

  /**
   * 상품 정보 삭제
   * @param productId
   * @return ResponseDto
   */
  @Override
  @Transactional
  public ResponseDto deleteProduct(Long productId) {

    Product product = getProductById(productId);
    productRepository.delete(product);

    return ResponseDto.getResponseBody(ResponseCode.PRODUCT_DELETE_SUCCESS);

  }

  /**
   * 상품 정보 조회 (상품 id)
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
