package com.banking.accountservice.controller;

import com.banking.accountservice.DTO.request.AccountRequest;
import com.banking.accountservice.config.RequestValidator;
import com.banking.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Component
@Slf4j
@RequiredArgsConstructor
public class AdminAccountHandler {

    private final AccountService accountService;
    private final RequestValidator validator;

    public Mono<ServerResponse> createAccount(ServerRequest request) {
        log.info(">> Processing request under AdminAccountHandler: createAccount");
        String targetUserId = request.queryParam("userId")
                .orElseThrow(() -> new ServerWebInputException("Query parameter 'userId' is required"));

        return request.bodyToMono(AccountRequest.class)
                .flatMap(validator::validate)
                .flatMap(req -> accountService.createAccount(req, targetUserId))
                .flatMap(acc -> ServerResponse.status(HttpStatus.CREATED).bodyValue(acc));
    }

    public Mono<ServerResponse> getAccountByNumber(ServerRequest request) {
        log.info(">> Processing request under AdminAccountHandler: getAccountByNumber");
        String accountNumber = request.pathVariable("accountNumber");
        return accountService.getAccountByNumber(accountNumber)
                .flatMap(acc -> ServerResponse.ok().bodyValue(acc));
    }

    public Mono<ServerResponse> blockAccount(ServerRequest request) {
        log.info(">> Processing request under AdminAccountHandler: blockAccount");
        String accountNumber = request.pathVariable("accountNumber");
        return accountService.blockAccount(accountNumber)
                .then(ServerResponse.accepted().build());
    }

    public Mono<ServerResponse> unblockAccount(ServerRequest request) {
        log.info(">> Processing request under AdminAccountHandler: unblockAccount");
        String accountNumber = request.pathVariable("accountNumber");
        return accountService.unblockAccount(accountNumber)
                .then(ServerResponse.accepted().build());
    }

    public Mono<ServerResponse> debitAccount(ServerRequest request) {
        log.info(">> Processing request under AdminAccountHandler: debitAccount");
        String accountNumber = request.pathVariable("accountNumber");
        BigDecimal amount = new BigDecimal(request.queryParam("amount").orElse("0"));
        return accountService.debitAmount(accountNumber, amount)
                .flatMap(res -> ServerResponse.accepted().bodyValue(res));
    }

    public Mono<ServerResponse> creditAccount(ServerRequest request) {
        log.info(">> Processing request under AdminAccountHandler: creditAccount");
        String accountNumber = request.pathVariable("accountNumber");
        BigDecimal amount = new BigDecimal(request.queryParam("amount").orElse("0"));
        return accountService.creditAmount(accountNumber, amount)
                .flatMap(res -> ServerResponse.accepted().bodyValue(res));
    }
}