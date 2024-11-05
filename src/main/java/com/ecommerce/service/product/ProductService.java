package com.ecommerce.service.product;

import com.ecommerce.dto.ResponseDto;
import com.ecommerce.dto.product.ProductDto;
import com.ecommerce.dto.product.UpdateProductDto;
import com.ecommerce.entity.Product;
import java.util.List;

public interface ProductService {

  ProductDto.Response createProduct(ProductDto.Request request);

  List<ProductDto.Response> getProductList(Integer page, String search, String ordering);

  List<ProductDto.Response> getProductListByMemberId(String memberId, Integer page, String search,
      String ordering);

  ProductDto.Response getProductDetails(Long productId);

  ProductDto.Response updateProduct(Long productId, UpdateProductDto updateRequest);

  ResponseDto deleteProduct(Long productId);

  Product getProductById(Long productId);

}
