package com.bootcamp.ms_customer.domain.model.dto;

import com.bootcamp.ms_customer.domain.model.enums.CustomerType;
import com.bootcamp.ms_customer.domain.model.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCustomerDto {

    private CustomerType customerType;
    private DocumentType documentType;
    private String documentNumber;
    private String firstName;
    private String lastName;
    private String businessName;
    private String email;
    private String phoneNumber;
}
