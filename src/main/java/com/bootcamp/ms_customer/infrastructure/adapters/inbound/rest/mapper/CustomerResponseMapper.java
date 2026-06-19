package com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.mapper;

import com.bootcamp.customer.dto.CustomerResponse;
import com.bootcamp.ms_customer.domain.model.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerResponseMapper {

    CustomerResponse toResponse(Customer customer);
}
