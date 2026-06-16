package com.bootcamp.ms_customer.domain.service;

import com.bootcamp.ms_customer.domain.model.Customer;
import com.bootcamp.ms_customer.domain.model.dto.CreateCustomerDto;
import com.bootcamp.ms_customer.domain.model.dto.UpdateCustomerDto;
import com.bootcamp.ms_customer.domain.model.enums.CustomerStatus;
import com.bootcamp.ms_customer.domain.model.exception.InvalidCustomerDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerDomainService {

    public Customer createCustomer(CreateCustomerDto createDto) {
        validateCreateCustomerData(createDto);

        return Customer.builder()
                .customerId(UUID.randomUUID().toString())
                .customerType(createDto.getCustomerType())
                .documentType(createDto.getDocumentType())
                .documentNumber(createDto.getDocumentNumber())
                .firstName(createDto.getFirstName())
                .lastName(createDto.getLastName())
                .businessName(createDto.getBusinessName())
                .email(createDto.getEmail())
                .phoneNumber(createDto.getPhoneNumber())
                .status(CustomerStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public void updateCustomer(Customer customer, UpdateCustomerDto updateDto) {
        if (updateDto.getFirstName() != null) {
            customer.setFirstName(updateDto.getFirstName());
        }
        if (updateDto.getLastName() != null) {
            customer.setLastName(updateDto.getLastName());
        }
        if (updateDto.getBusinessName() != null) {
            customer.setBusinessName(updateDto.getBusinessName());
        }
        if (updateDto.getEmail() != null) {
            customer.setEmail(updateDto.getEmail());
        }
        if (updateDto.getPhoneNumber() != null) {
            customer.setPhoneNumber(updateDto.getPhoneNumber());
        }
        customer.setUpdatedAt(LocalDateTime.now());
    }

    private void validateCreateCustomerData(CreateCustomerDto createDto) {
        if (createDto.getDocumentNumber() == null || createDto.getDocumentNumber().isBlank()) {
            throw new InvalidCustomerDataException("El número de documento es requerido");
        }
        if (createDto.getCustomerType() == null) {
            throw new InvalidCustomerDataException("El tipo de cliente es requerido");
        }
        if (createDto.getDocumentType() == null) {
            throw new InvalidCustomerDataException("El tipo de documento es requerido");
        }
    }
}
