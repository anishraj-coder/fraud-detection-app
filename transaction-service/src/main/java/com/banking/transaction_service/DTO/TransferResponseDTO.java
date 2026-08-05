package com.banking.transaction_service.DTO;

import com.banking.transaction_service.entity.enums.TransactionStatus;
import com.banking.transaction_service.entity.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Schema
@Builder
public record TransferResponseDTO(
         String id,

         String senderAccountNumber,

         String receiverAccountNumber,

         String referenceNumber,

         BigDecimal amount,

         LocalDateTime createdAt,

         LocalDateTime completedAt,

         TransactionStatus status,

         String description,

         String failureReason,

         TransactionType type
) {

}
