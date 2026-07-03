package com.bootcamp.ms_customer.infrastructure.adapters.outbound.persistence.entity;

import com.bootcamp.ms_customer.domain.model.enums.CustomerStatus;
import com.bootcamp.ms_customer.domain.model.enums.CustomerType;
import com.bootcamp.ms_customer.domain.model.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "customers")
@CompoundIndexes({
    @CompoundIndex(name = "idx_status_createdAt", def = "{ 'status' : 1, 'createdAt' : -1 }"),
    @CompoundIndex(name = "idx_customerType_status", def = "{ 'customerType' : 1, 'status' : 1 }")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerEntity {

    @Id
    private String customerId;

    @Indexed(name = "idx_customerType")
    private CustomerType customerType;

    @Indexed(name = "idx_documentType")
    private DocumentType documentType;

    @Indexed(name = "idx_documentNumber", unique = true, sparse = true)
    private String documentNumber;

    private String firstName;

    private String lastName;

    private String businessName;

    @Indexed(name = "idx_email", unique = true, sparse = true)
    private String email;

    private String phoneNumber;

    @Indexed(name = "idx_status")
    private CustomerStatus status;

    @Indexed(name = "idx_createdAt_desc")
    private LocalDateTime createdAt;

    @Indexed(name = "idx_updatedAt_desc")
    private LocalDateTime updatedAt;
}
