package com.banking.accountservice.controller;

import com.banking.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Component
@Slf4j
@RequiredArgsConstructor
public class InternalAccountHandler {

    private final AccountService accountService;

    public Mono<ServerResponse> debitAccount(ServerRequest request) {
        String accountNumber = request.pathVariable("accountNumber");
        String idempotencyKey = request.headers().firstHeader("X-Idempotency-Key");
        BigDecimal amount = new BigDecimal(request.queryParam("amount").orElse("0"));

        log.info(">> [Internal Call] Debit request for account: {}, amount: {}, IdempotencyKey: {}",
                accountNumber, amount, idempotencyKey);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new ServerWebInputException("Debit amount must be greater than zero"));
        }

        return accountService.debitAmount(accountNumber, amount)
                .flatMap(res -> ServerResponse.ok().bodyValue(res));
    }
    public Mono<ServerResponse> getAccountByNumber(ServerRequest request) {
        log.info(">> Processing request under AdminAccountHandler: getAccountByNumber");
        String accountNumber = request.pathVariable("accountNumber");
        return accountService.getAccountByNumber(accountNumber)
                .flatMap(acc -> ServerResponse.ok().bodyValue(acc));
    }

    public Mono<ServerResponse> creditAccount(ServerRequest request) {
        String accountNumber = request.pathVariable("accountNumber");
        String idempotencyKey = request.headers().firstHeader("X-Idempotency-Key");
        BigDecimal amount = new BigDecimal(request.queryParam("amount").orElse("0"));

        log.info(">> [Internal Call] Credit request for account: {}, amount: {}, IdempotencyKey: {}",
                accountNumber, amount, idempotencyKey);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new ServerWebInputException("Credit amount must be greater than zero"));
        }

        return accountService.creditAmount(accountNumber, amount)
                .flatMap(res -> ServerResponse.ok().bodyValue(res));
    }

    public Mono<ServerResponse> blockAccount(ServerRequest request) {
        String accountNumber = request.pathVariable("accountNumber");
        log.info(">> [Internal Call] Block request for account: {}", accountNumber);
        return accountService.blockAccount(accountNumber)
                .then(ServerResponse.ok().build());
    }
}