package com.banking.paymentservice.entity;

import com.banking.paymentservice.entity.enums.PaymentStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="payments_fraud")
public class Payment implements Persistable<String> {

    @Id
    @Column("id")
    private String id;

    @Column("razor_payment_id")
    private String razorPaymentId;

    @Column("account_number")
    private String accountNumber;

    @Column("amount")
    private BigDecimal amount;

    @Column("currency")
    private String currency;

    @Column("payment_status")
    private PaymentStatus paymentStatus;

    @Column("description")
    private String description;

    @Column("failure_reason")
    private String failureReason;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public boolean isNew() {
        return this.isNew || this.id == null;
    }

    public void setIsNewToFalse() {
        this.isNew = false;
    }
}