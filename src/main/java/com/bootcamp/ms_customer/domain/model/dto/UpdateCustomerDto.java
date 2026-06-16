package com.bootcamp.ms_customer.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCustomerDto {

    private String firstName;
    private String lastName;
    private String businessName;
    private String email;
    private String phoneNumber;
}
