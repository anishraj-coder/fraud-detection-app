package com.banking.accountservice.DTO.request;


import com.banking.accountservice.entity.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema
public class OnboardingRequest {

    @Schema(description = "Account Holder's name",example = "Anish Raj")
    @NotBlank(message = "Account holder name is required")
    private String accountHolderName;

    @Schema(description= "Account Type",example = "SAVINGS/CURRENT")
    @NotNull(message = "Valid account type is required")
    private AccountType accountType;

    @Schema(description = "Phone number",example = "1234567890")
    @NotBlank(message = "Phone number is required")
    @Size(min = 10, max = 10, message = "Phone number must be exactly 10 digits")
    private String phone;

    @Schema(description = "Initial Deposit amount",example = "500.00")
    @NotNull(message = "Initial deposit amount is required")
    @Min(value = 500, message = "Minimum initial deposit must be at least 500")
    private BigDecimal initialDeposit;
}