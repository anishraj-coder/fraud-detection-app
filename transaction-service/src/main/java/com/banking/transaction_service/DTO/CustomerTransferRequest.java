package com.banking.transaction_service.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerTransferRequest {

    @NotBlank(message = "Receiver account number is required")
    private String receiverAccountNumber;

    @NotNull(message = "Transfer amount is required")
    @Positive(message = "Transfer amount must be positive")
    private BigDecimal amount;

    @Length(max = 100, message = "Description must be under 100 characters")
    private String description;
}