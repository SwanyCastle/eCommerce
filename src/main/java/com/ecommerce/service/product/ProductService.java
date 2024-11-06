package com.ecommerce.service.product;

import com.ecommerce.dto.ResponseDto;
import com.ecommerce.dto.product.ProductDto;
import com.ecommerce.dto.product.ProductDto.Response;
import com.ecommerce.dto.product.UpdateProductDto;
import com.ecommerce.entity.Product;
import com.ecommerce.type.ProductStatus;
import com.ecommerce.type.SortType;
import org.springframework.data.domain.Page;

public interface ProductService {

  ProductDto.Response createProduct(String memberId, String token, ProductDto.Request request);

  Page<Response> getProductList(
      Integer page, String search, ProductStatus status, SortType ordering
  );

  Page<ProductDto.Response> getProductListByMemberId(
      String memberId, Integer page, String search, ProductStatus status, SortType ordering
  );

  ProductDto.Response getProductDetails(Long productId);

  ProductDto.Response updateProduct(Long productId, String token, UpdateProductDto updateRequest);

  ResponseDto deleteProduct(Long productId, String token);

  Product getProductById(Long productId);

}
