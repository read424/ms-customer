package com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.mapper;

import com.bootcamp.ms_customer.domain.model.dto.UpdateCustomerDto;
import com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.request.UpdateCustomerRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UpdateCustomerMapper {

    UpdateCustomerDto toDomainDto(UpdateCustomerRequest request);
}
