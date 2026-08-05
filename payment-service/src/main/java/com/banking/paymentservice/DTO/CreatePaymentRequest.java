package com.banking.paymentservice.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema
public class CreatePaymentRequest {

    @Schema(description = "Account number")
    @NotBlank(message = "Account number cant be blank")
    private String accountNumber;

    @Schema(description = "Amount to create order",example = "50000.00")
    @NotNull(message = "The amount field cant be null")
    BigDecimal amount;

    @Schema(description = "The description for the payment request",example = "Initial deposit")
    @Size(max = 310,min=0,message = "The Description can't be more than 30 characters")
    private String description;
}
