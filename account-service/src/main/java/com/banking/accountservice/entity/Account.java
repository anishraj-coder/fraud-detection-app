package com.banking.accountservice.entity;

import com.banking.accountservice.entity.enums.AccountStatus;
import com.banking.accountservice.entity.enums.AccountType;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
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
@Table(name = "accounts_fraud")
public class Account implements Persistable<Long> {

    @Id
    private Long id;

    @Column("account_number")
    private String accountNumber;

    @Column("user_id")
    private String userId; // Keycloak 'sub' claim

    @Column("account_holder_name")
    private String accountHolderName;

    @Column("account_type")
    private AccountType accountType;

    @Column("account_status")
    private AccountStatus accountStatus;

    @Column("email")
    private String email;

    @Column("phone")
    private String phone;

    @Column("account_balance")
    private BigDecimal accountBalance;

    @Column("daily_transaction_limit")
    private BigDecimal dailyTransactionLimit;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public boolean isNew() {
        // Essential for Spring Data R2DBC: prevents unnecessary SELECT before INSERT when ID is auto-generated
        return this.isNew || this.id == null;
    }

    public void setNewToFalse() {
        this.isNew = false;
    }
}