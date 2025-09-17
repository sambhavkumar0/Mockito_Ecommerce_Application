package com.cts.ecommerce.mapper;


import org.mapstruct.*;

import com.cts.ecommerce.dto.ProductRequest;
import com.cts.ecommerce.dto.ProductResponse;
import com.cts.ecommerce.model.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductResponse toDto(Product product);

    @Mapping(target = "id", ignore = true) 
    Product toEntity(ProductRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntity(ProductRequest request, @MappingTarget Product product);
}
