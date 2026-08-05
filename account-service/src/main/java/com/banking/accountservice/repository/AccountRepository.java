package com.banking.accountservice.repository;

import com.banking.accountservice.entity.Account;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface AccountRepository extends ReactiveCrudRepository<Account, Long> {

    // Find account by Keycloak user ID (for customer '/me' endpoints)
    Mono<Account> findByUserId(String userId);

    // Find account by account number (for transfers / admin)
    Mono<Account> findByAccountNumber(String accountNumber);

    // Check existing account on creation
    Mono<Account> findByUserIdOrAccountNumber(String userId, String accountNumber);

}