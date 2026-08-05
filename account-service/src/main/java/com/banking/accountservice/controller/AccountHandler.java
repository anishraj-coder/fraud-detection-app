package com.banking.accountservice.controller;

import com.banking.accountservice.config.RequestValidator;
import com.banking.accountservice.DTO.request.OnboardingRequest;
import com.banking.accountservice.service.AccountService;
import com.banking.accountservice.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class AccountHandler {

    private final AccountService accountService;
    private final RequestValidator requestValidator;

    public Mono<ServerResponse> onboardAccount(ServerRequest request) {
        log.info(">> Processing onboarding request under AccountHandler");

        return Mono.zip(SecurityUtils.getCurrentUserId(), SecurityUtils.getCurrentEmail())
                .flatMap(tuple -> {
                    String userId = tuple.getT1();
                    String email = tuple.getT2();

                    return request.bodyToMono(OnboardingRequest.class)
                            .flatMap(requestValidator::validate)
                            .flatMap(req -> accountService.onboardAccount(req, userId, email));
                })
                .flatMap(acc -> ServerResponse.status(HttpStatus.CREATED).bodyValue(acc));
    }

    public Mono<ServerResponse> getMyAccount(ServerRequest request) {
        log.info(">> Processing request under AccountHandler: getMyAccount");
        return SecurityUtils.getCurrentUserId()
                .flatMap(accountService::getAccountByUserId)
                .flatMap(acc -> ServerResponse.ok().bodyValue(acc));
    }

    public Mono<ServerResponse> getMyBalance(ServerRequest request) {
        log.info(">> Processing request under AccountHandler: getMyBalance");
        return SecurityUtils.getCurrentUserId()
                .flatMap(accountService::getBalanceByUserId)
                .flatMap(balanceMap -> ServerResponse.ok().bodyValue(balanceMap));
    }
}