package com.banking.transaction_service.entity;

import com.banking.transaction_service.entity.enums.TransactionStatus;
import com.banking.transaction_service.entity.enums.TransactionType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "transactions_fraud")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction implements Persistable<String> {

    @Id
    @Column("id")
    private String id;

    @Column("sender_account_number")
    private String senderAccountNumber;

    @Column("receiver_account_number")
    private String receiverAccountNumber;

    @Column("reference_number")
    private String referenceNumber;

    @Column("amount")
    private BigDecimal amount;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("completed_at")
    private LocalDateTime completedAt;

    @Column("status")
    private TransactionStatus status;

    @Column("description")
    private String description;

    @Column("failure_reason")
    private String failureReason;

    @Column("type")
    private TransactionType type;

    @Transient
    @Builder.Default
    @JsonIgnore
    private boolean isNew = true;

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    @JsonIgnore
    public boolean isNew() {
        return this.isNew || this.id == null;
    }

    public void setNewToFalse() {
        this.isNew = false;
    }
}