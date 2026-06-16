package com.bootcamp.ms_customer.domain.model;

import com.bootcamp.ms_customer.domain.model.enums.CustomerStatus;
import com.bootcamp.ms_customer.domain.model.enums.CustomerType;
import com.bootcamp.ms_customer.domain.model.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    private String customerId;
    private CustomerType customerType;
    private DocumentType documentType;
    private String documentNumber;
    private String firstName;
    private String lastName;
    private String businessName;
    private String email;
    private String phoneNumber;
    private CustomerStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
