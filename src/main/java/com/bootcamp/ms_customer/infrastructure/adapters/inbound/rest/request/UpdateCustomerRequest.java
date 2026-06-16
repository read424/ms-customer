package com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Solicitud para actualizar un cliente")
public class UpdateCustomerRequest {

    @Nullable
    @JsonProperty("firstName")
    @Schema(description = "Nombre", example = "Juan Pablo")
    private String firstName;

    @Nullable
    @JsonProperty("lastName")
    @Schema(description = "Apellido", example = "Pérez García")
    private String lastName;

    @Nullable
    @JsonProperty("businessName")
    @Schema(description = "Razón social", example = "Empresa S.A.C.")
    private String businessName;

    @Nullable
    @JsonProperty("email")
    @Schema(description = "Correo electrónico", example = "juan.pablo@example.com")
    private String email;

    @Nullable
    @JsonProperty("phoneNumber")
    @Schema(description = "Número de teléfono", example = "987654320")
    private String phoneNumber;
}
