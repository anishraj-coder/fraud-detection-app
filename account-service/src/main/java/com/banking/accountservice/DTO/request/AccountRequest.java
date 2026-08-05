package com.banking.accountservice.DTO.request;


import com.banking.accountservice.entity.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Account creation request schema")
public class AccountRequest {

    @Schema(description = "Account number")
    @NotBlank(message = "Account number is required")
    private String accountNumber;
    @Schema(description= "Account Holder's name")
    @NotBlank(message = "Account holder name is required")
    private String accountHolderName;

    @Schema(description= "Account type SAVINGS/CURRENT")
    @NotNull(message = "Need a valid account holder type")
    private AccountType accountType;

    @NotBlank(message = "Need an email ")
    @Email(message = "Enter a valid email")
    @Schema(description = "Email id of customer",example = "example@example.com")
    private String email;
    @NotBlank(message = "Need an phone number")
    @Schema(description= "Phone number",example = "1234567890")
    @Length(max = 10,min = 10,message = "The phone number must be 10 digits")
    private String phone;
    @NotNull(message = "need some initial deposit value")
    @Positive(message = "need a positive value")
    @Schema(description = "Initial deposit amount",example = "500.00")
    private BigDecimal initialDeposit;
}
