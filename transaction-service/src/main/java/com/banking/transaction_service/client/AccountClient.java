package com.banking.transaction_service.client;

import com.banking.transaction_service.DTO.AccountResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountClient {

    private final WebClient webClient;

    /**
     * Calls ACCOUNT-SERVICE /api/v1/accounts/me using the current security context token.
     * Automatically retrieves the authenticated user's account details.
     */
    public Mono<AccountResponse> getMyAccount() {
        log.info("Fetching authenticated user's account details from ACCOUNT-SERVICE");

        return webClient.get()
                .uri("/accounts/me")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new ResponseStatusException(
                                        HttpStatus.NOT_FOUND, "User account not found or onboarded: " + body)))
                )
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new ResponseStatusException(
                                        HttpStatus.SERVICE_UNAVAILABLE, "Account service unavailable: " + body)))
                )
                .bodyToMono(AccountResponse.class)
                .doOnNext(acc -> log.info("Successfully resolved user account number: {}", acc.getAccountNumber()))
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No account record found for authenticated user. Please onboard an account first.")));
    }

    /**
     * Calls ACCOUNT-SERVICE /api/v1/internal/accounts/{accountNumber} to fetch details by account number.
     * Used internally for service-to-service communication.
     */
    public Mono<AccountResponse> getAccountByNumber(String accountNumber) {
        log.info("Fetching account details for account number: {} from ACCOUNT-SERVICE", accountNumber);

        return webClient.get()
                .uri("/internal/accounts/{accountNumber}", accountNumber)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new ResponseStatusException(
                                        HttpStatus.NOT_FOUND, "Account not found for number " + accountNumber + ": " + body)))
                )
                .bodyToMono(AccountResponse.class)
                .doOnNext(acc -> log.info("Successfully fetched details for account: {}", acc.getAccountNumber()));
    }

    /**
     * Calls ACCOUNT-SERVICE /api/v1/internal/accounts/{accountNumber}/block to block the account.
     * Used internally when a fraudulent transaction (invalid OTP) is detected.
     */
    public Mono<Void> blockAccount(String accountNumber) {
        log.info("Requesting ACCOUNT-SERVICE to block account number: {}", accountNumber);

        return webClient.put()
                .uri("/internal/accounts/{accountNumber}/block", accountNumber)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new ResponseStatusException(
                                        HttpStatus.INTERNAL_SERVER_ERROR, "Failed to block account " + accountNumber + ": " + body)))
                )
                .bodyToMono(Void.class)
                .doOnSuccess(unused -> log.info("Successfully requested block for account: {}", accountNumber));
    }
}