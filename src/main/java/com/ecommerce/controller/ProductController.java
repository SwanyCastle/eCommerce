package com.ecommerce.controller;

import com.ecommerce.dto.ResponseDto;
import com.ecommerce.dto.product.ProductDto;
import com.ecommerce.dto.product.UpdateProductDto;
import com.ecommerce.service.product.ProductService;
import com.ecommerce.type.ProductStatus;
import com.ecommerce.type.SortType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  @PreAuthorize("hasRole('ROLE_SELLER')")
  @PostMapping("/seller/{memberId}")
  public ProductDto.Response createProduct(
      @PathVariable String memberId,
      @RequestHeader("Authorization") String token,
      @RequestBody @Valid ProductDto.Request request
  ) {

    return productService.createProduct(memberId, token, request);

  }

  @GetMapping
  public Page<ProductDto.Response> getProductList(
      @RequestParam(required = false, defaultValue = "1") Integer page,
      @RequestParam(required = false, defaultValue = "") String search,
      @RequestParam(required = false, defaultValue = "NONE") ProductStatus status,
      @RequestParam(required = false, defaultValue = "LATEST") SortType sortType
  ) {

    return productService.getProductList(page, search, status, sortType);

  }

  @GetMapping("/seller/{memberId}")
  public Page<ProductDto.Response> getProductListByMemberId(
      @PathVariable String memberId,
      @RequestParam(required = false, defaultValue = "1") Integer page,
      @RequestParam(required = false, defaultValue = "") String search,
      @RequestParam(required = false, defaultValue = "NONE") ProductStatus status,
      @RequestParam(required = false, defaultValue = "LATEST") SortType sortType
  ) {

    return productService.getProductListByMemberId(memberId, page, search, status, sortType);

  }

  @GetMapping("/{productId}")
  public ProductDto.Response getProductDetails(@PathVariable Long productId) {

    return productService.getProductDetails(productId);

  }

  @PreAuthorize("hasRole('ROLE_SELLER')")
  @PutMapping("/{productId}")
  public ProductDto.Response updateProduct(
      @PathVariable Long productId,
      @RequestHeader("Authorization") String token,
      @RequestBody @Valid UpdateProductDto updateRequest
  ) {

    return productService.updateProduct(productId, token, updateRequest);

  }

  @PreAuthorize("hasRole('ROLE_SELLER')")
  @DeleteMapping("/{productId}")
  public ResponseDto deleteProduct(
      @PathVariable Long productId,
      @RequestHeader("Authorization") String token
  ) {

    return productService.deleteProduct(productId, token);

  }

}
