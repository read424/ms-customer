package com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.response;

import com.bootcamp.ms_customer.domain.model.enums.CustomerStatus;
import com.bootcamp.ms_customer.domain.model.enums.CustomerType;
import com.bootcamp.ms_customer.domain.model.enums.DocumentType;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Respuesta con datos del cliente")
public class CustomerResponse {

    @JsonProperty("customerId")
    @Schema(description = "ID del cliente", example = "123e4567-e89b-12d3-a456-426614174000")
    private String customerId;

    @JsonProperty("customerType")
    @Schema(description = "Tipo de cliente", example = "PERSONAL")
    private CustomerType customerType;

    @JsonProperty("documentType")
    @Schema(description = "Tipo de documento", example = "DNI")
    private DocumentType documentType;

    @JsonProperty("documentNumber")
    @Schema(description = "Número de documento", example = "12345678")
    private String documentNumber;

    @JsonProperty("firstName")
    @Schema(description = "Nombre", example = "Juan")
    private String firstName;

    @JsonProperty("lastName")
    @Schema(description = "Apellido", example = "Pérez")
    private String lastName;

    @JsonProperty("businessName")
    @Schema(description = "Razón social", example = "Empresa S.A.")
    private String businessName;

    @JsonProperty("email")
    @Schema(description = "Correo electrónico", example = "juan@example.com")
    private String email;

    @JsonProperty("phoneNumber")
    @Schema(description = "Número de teléfono", example = "987654321")
    private String phoneNumber;

    @JsonProperty("status")
    @Schema(description = "Estado del cliente", example = "ACTIVE")
    private CustomerStatus status;

    @JsonProperty("createdAt")
    @Schema(description = "Fecha de creación", example = "2026-06-15T23:39:37")
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    @Schema(description = "Fecha de actualización", example = "2026-06-15T23:39:37")
    private LocalDateTime updatedAt;
}
