package com.bootcamp.ms_customer.infrastructure.adapters.outbound.persistence.mapper;

import com.bootcamp.ms_customer.domain.model.Customer;
import com.bootcamp.ms_customer.infrastructure.adapters.outbound.persistence.entity.CustomerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerPersistenceMapper {

    CustomerEntity toEntity(Customer customer);

    Customer toDomain(CustomerEntity entity);
}
