package com.banking.transaction_service.routes;

import com.banking.transaction_service.DTO.CustomerTransferRequest;
import com.banking.transaction_service.DTO.TransactionRequest;
import com.banking.transaction_service.DTO.TransferResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class TransactionRouter {

    private final TransactionRouteHandler routeHandler;

    @Bean("transactionRoutingHandler")
    @RouterOperations({
            // Customer Routes
            @RouterOperation(path = "/api/v1/transactions/transfer", method = RequestMethod.POST,
                    beanClass = TransactionRouteHandler.class, beanMethod = "transferMoneyForCustomer",
                    operation = @Operation(
                            operationId = "transferMoneyForCustomer",
                            summary = "Customer transfer money",
                            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = CustomerTransferRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Transfer Initiated",
                                            content = @Content(schema = @Schema(implementation = TransferResponseDTO.class)))
                            }
                    )),

            @RouterOperation(path = "/api/v1/transactions/transaction", method = RequestMethod.GET,
                    beanClass = TransactionRouteHandler.class, beanMethod = "getTransactionByReferenceNumber",
                    operation = @Operation(
                            operationId = "getTransactionByReferenceNumber",
                            summary = "Get transaction by reference number",
                            parameters = { @Parameter(name = "referenceNumber", in = ParameterIn.QUERY, required = true) },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Transaction found",
                                            content = @Content(schema = @Schema(implementation = TransferResponseDTO.class)))
                            }
                    )),

            @RouterOperation(path = "/api/v1/transactions/transaction/history", method = RequestMethod.GET,
                    beanClass = TransactionRouteHandler.class, beanMethod = "getTransactionHistoryUser",
                    operation = @Operation(
                            operationId = "getTransactionHistoryUser",
                            summary = "Get authenticated user transaction history",
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Transaction History",
                                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = TransferResponseDTO.class))))
                            }
                    )),

            @RouterOperation(path = "/api/v1/transactions/transaction/verify/{refId}", method = RequestMethod.GET,
                    beanClass = TransactionRouteHandler.class, beanMethod = "verifyOtp",
                    operation = @Operation(
                            operationId = "verifyOtp",
                            summary = "Verify OTP for transaction",
                            parameters = {
                                    @Parameter(name = "refId", in = ParameterIn.PATH, required = true),
                                    @Parameter(name = "otp", in = ParameterIn.QUERY, required = true)
                            }
                    )),

            // Admin Routes
            @RouterOperation(path = "/api/v1/admin/transactions/transfer", method = RequestMethod.POST,
                    beanClass = TransactionRouteHandler.class, beanMethod = "transferMoney",
                    operation = @Operation(
                            operationId = "transferMoneyAdmin",
                            summary = "Admin transfer money",
                            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = TransactionRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Transfer Initiated",
                                            content = @Content(schema = @Schema(implementation = TransferResponseDTO.class)))
                            }
                    )),

            @RouterOperation(path = "/api/v1/admin/transactions/history/{id}", method = RequestMethod.GET,
                    beanClass = TransactionRouteHandler.class, beanMethod = "getTransactionHistoryAdmin",
                    operation = @Operation(
                            operationId = "getTransactionHistoryAdmin",
                            summary = "Admin: Get transaction history by account ID",
                            parameters = { @Parameter(name = "id", in = ParameterIn.PATH, required = true) },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Transaction History",
                                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = TransferResponseDTO.class))))
                            }
                    ))
    })
    public RouterFunction<ServerResponse> transactionRouter() {
        return RouterFunctions.route()
                // --- CUSTOMER DOMAIN ROUTES ---
                .path("/api/v1/transactions", builder ->
                        builder.nest(accept(MediaType.APPLICATION_JSON), nest -> nest
                                .POST("/transfer", routeHandler::transferMoneyForCustomer)
                                .GET("/transaction", routeHandler::getTransactionByReferenceNumber)
                                .GET("/transaction/history", routeHandler::getTransactionHistoryUser)
                                .GET("/transaction/verify/{refId}", routeHandler::verifyOtp)
                                .GET("/transaction/stream/{referenceNumber}", routeHandler::streamSagaStatus)
                        ))
                // --- ADMIN DOMAIN ROUTES ---
                .path("/api/v1/admin/transactions", builder ->
                        builder.nest(accept(MediaType.APPLICATION_JSON), nest -> nest
                                .POST("/transfer", routeHandler::transferMoney)
                                .GET("/history/{id}",routeHandler::getTransactionHistoryAdmin)
                        ))
                .build();
    }
}