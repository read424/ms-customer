package com.bootcamp.ms_customer.application.mapper;

import com.bootcamp.ms_customer.domain.model.Customer;
import com.bootcamp.ms_customer.domain.model.dto.CreateCustomerDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        imports = {java.util.UUID.class})
public interface CustomerDomainMapper {

    @Mapping(target = "customerId", expression = "java(UUID.randomUUID().toString())")
    @Mapping(target = "status", ignore = true)
    Customer toCustomer(CreateCustomerDto createDto);
}
