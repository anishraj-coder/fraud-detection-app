package com.banking.accountservice.controller;

import com.banking.accountservice.DTO.request.OnboardingRequest;
import com.banking.accountservice.DTO.response.AccountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class AccountController {

    @Bean
    @RouterOperations({
            // --- CUSTOMER DOMAIN ---
            @RouterOperation(path = "/api/v1/accounts/me", method = RequestMethod.GET,
                    beanClass = AccountHandler.class, beanMethod = "getMyAccount",
                    operation = @Operation(
                            operationId = "getMyAccount", summary = "Get logged-in user account details")
            ),

            @RouterOperation(path = "/api/v1/accounts/me/balance", method = RequestMethod.GET,
                    beanClass = AccountHandler.class, beanMethod = "getMyBalance",
                    operation = @Operation(operationId = "getMyBalance", summary = "Get logged-in user balance")),

            @RouterOperation(path = "/api/v1/accounts/me/onboard", method = RequestMethod.POST,
                    beanClass = AccountHandler.class, beanMethod = "onboardAccount",
                    operation = @Operation(
                            operationId = "onboardAccount",
                            summary = "Onboard a new account",
                            requestBody = @RequestBody(
                                    content = @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = OnboardingRequest.class)
                                    )
                            ),
                            responses = @ApiResponse(
                                    responseCode = "200",
                                    description = "Account onboarded successfully",
                                    content= @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = AccountResponse.class)
                                    )
                            )
                    )
            ),

            // --- ADMIN DOMAIN ---
            @RouterOperation(path = "/api/v1/admin/accounts", method = RequestMethod.POST,
                    beanClass = AdminAccountHandler.class, beanMethod = "createAccount",
                    operation = @Operation(operationId = "createAccount", summary = "Admin: Create bank account")),

            @RouterOperation(path = "/api/v1/admin/accounts/{accountNumber}", method = RequestMethod.GET,
                    beanClass = AdminAccountHandler.class, beanMethod = "getAccountByNumber",
                    operation = @Operation(
                            operationId = "getAccountByNumber",
                            summary = "Admin: Get account by account number",
                            parameters = {
                                    @Parameter(
                                            name = "accountNumber",
                                            in = ParameterIn.PATH,
                                            required = true,
                                            description = "14-digit account number",
                                            example = "50100012345671"
                                    )
                            }
                    )),

            @RouterOperation(path = "/api/v1/admin/accounts/{accountNumber}/block", method = RequestMethod.PUT,
                    beanClass = AdminAccountHandler.class, beanMethod = "blockAccount",
                    operation = @Operation(
                            operationId = "blockAccount",
                            summary = "Admin: Block account",
                            parameters = {
                                    @Parameter(
                                            name = "accountNumber",
                                            in = ParameterIn.PATH,
                                            required = true,
                                            description = "Account number to block",
                                            example = "50100012345671"
                                    )
                            }
                    )),

            @RouterOperation(path = "/api/v1/admin/accounts/{accountNumber}/unblock", method = RequestMethod.PUT,
                    beanClass = AdminAccountHandler.class, beanMethod = "unblockAccount",
                    operation = @Operation(
                            operationId = "unblockAccount",
                            summary = "Admin: Unblock account",
                            parameters = {
                                    @Parameter(
                                            name = "accountNumber",
                                            in = ParameterIn.PATH,
                                            required = true,
                                            description = "Account number to unblock",
                                            example = "50100012345671"
                                    )
                            }
                    )),


    })
    public RouterFunction<ServerResponse> accountRoutes(
            AccountHandler customerHandler,
            AdminAccountHandler adminHandler,
            InternalAccountHandler internalHandler) {
        return route()
                // --- CUSTOMER DOMAIN ROUTES ---
                .path("/api/v1/accounts", builder -> builder
                        .nest(accept(MediaType.APPLICATION_JSON), nest -> nest
                                .GET("/me", customerHandler::getMyAccount)
                                .GET("/me/balance", customerHandler::getMyBalance)
                                .POST("/me/onboard", customerHandler::onboardAccount)
                        )
                )
                // --- ADMIN DOMAIN ROUTES ---
                .path("/api/v1/admin/accounts", builder -> builder
                        .nest(accept(MediaType.APPLICATION_JSON), nest -> nest
                                .POST("", adminHandler::createAccount)
                                .GET("/{accountNumber}", adminHandler::getAccountByNumber)
                                .PUT("/{accountNumber}/block", adminHandler::blockAccount)
                                .PUT("/{accountNumber}/unblock", adminHandler::unblockAccount)
                        )
                )
                // --- INTERNAL SERVICE-TO-SERVICE ROUTES ---
                .path("/api/v1/internal/accounts", builder -> builder
                        .nest(accept(MediaType.APPLICATION_JSON), nest -> nest
                                .GET("/{accountNumber}", internalHandler::getAccountByNumber)
                                .POST("/{accountNumber}/debit", internalHandler::debitAccount)
                                .POST("/{accountNumber}/credit", internalHandler::creditAccount)
                                .PUT("/{accountNumber}/block", internalHandler::blockAccount)
                        )
                )
                .build();
    }
}