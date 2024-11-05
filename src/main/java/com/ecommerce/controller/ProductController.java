package com.ecommerce.controller;

import com.ecommerce.dto.ResponseDto;
import com.ecommerce.dto.product.ProductDto;
import com.ecommerce.dto.product.UpdateProductDto;
import com.ecommerce.service.product.ProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  @PreAuthorize("hasRole('ROLE_SELLER')")
  @PostMapping
  public ProductDto.Response createProduct(@RequestBody ProductDto.Request request) {

    return productService.createProduct(request);

  }

  @GetMapping
  public List<ProductDto.Response> getProductList(
      @RequestParam(required = false, defaultValue = "1") Integer page,
      @RequestParam(required = false, defaultValue = "") String search,
      @RequestParam(required = false, defaultValue = "latest") String ordering
  ) {

    return productService.getProductList(page, search, ordering);

  }

  @PreAuthorize("hasRole('ROLE_SELLER')")
  @GetMapping("/seller/{memberId}")
  public List<ProductDto.Response> getProductListByMemberId(
      @PathVariable String memberId,
      @RequestParam(required = false, defaultValue = "1") Integer page,
      @RequestParam(required = false, defaultValue = "") String search,
      @RequestParam(required = false, defaultValue = "latest") String ordering
  ) {

    return productService.getProductListByMemberId(memberId, page, search, ordering);

  }

  @GetMapping("/{productId}")
  public ProductDto.Response getProductDetails(@PathVariable Long productId) {

    return productService.getProductDetails(productId);

  }

  @PreAuthorize("hasRole('ROLE_SELLER')")
  @PatchMapping("/{productId}")
  public ProductDto.Response updateProduct(
      @PathVariable Long productId,
      @RequestBody UpdateProductDto updateRequest
  ) {

    return productService.updateProduct(productId, updateRequest);

  }

  @PreAuthorize("hasRole('ROLE_SELLER')")
  @DeleteMapping("/{productId}")
  public ResponseDto deleteProduct(@PathVariable Long productId) {

    return productService.deleteProduct(productId);

  }

}
