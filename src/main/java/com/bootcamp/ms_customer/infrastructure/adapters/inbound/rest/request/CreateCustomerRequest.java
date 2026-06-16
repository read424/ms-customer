package com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.request;

import com.bootcamp.ms_customer.domain.model.enums.CustomerType;
import com.bootcamp.ms_customer.domain.model.enums.DocumentType;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Solicitud para crear un nuevo cliente")
public class CreateCustomerRequest {

    @NotNull(message = "El tipo de cliente es requerido")
    @JsonProperty("customerType")
    @Schema(description = "Tipo de cliente", example = "PERSONAL")
    private CustomerType customerType;

    @NotNull(message = "El tipo de documento es requerido")
    @JsonProperty("documentType")
    @Schema(description = "Tipo de documento", example = "DNI")
    private DocumentType documentType;

    @NotBlank(message = "El número de documento es requerido")
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
}
