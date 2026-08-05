package com.banking.transaction_service.DTO;


import com.banking.transaction_service.entity.enums.TransactionStatus;
import com.banking.transaction_service.entity.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Customer Money Transfer Request Payload")
public class TransactionRequest {
    @Schema(description = "Sender account number", example = "ACC1000234")
    @NotBlank(message = "Need the sender account number")
    private String senderAccountNumber;
    @Schema(description = "Receiver account number", example = "ACC9000876")
    @NotBlank(message = "Need the receiver account number")
    private String receiverAccountNumber;
    @Schema(description = "Transfer amount", example = "250.00")
    @NotNull(message = "Need the amount to transfer")
    @Positive(message = "The amount must be positive")
    private BigDecimal amount;
    @Schema(description = "Description for transaction", example = "Rent")
    @Length(max = 100,message = "The description must be under 100 characters")
    private String description;
}
