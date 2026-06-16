package com.bootcamp.ms_customer.infrastructure.adapters.outbound.persistence.entity;

import com.bootcamp.ms_customer.domain.model.enums.CustomerStatus;
import com.bootcamp.ms_customer.domain.model.enums.CustomerType;
import com.bootcamp.ms_customer.domain.model.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerEntity {

    @Id
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
