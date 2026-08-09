package com.banking.transaction_service.routes;

import com.banking.transaction_service.DTO.CustomerTransferRequest;
import com.banking.transaction_service.DTO.TransactionRequest;
import com.banking.transaction_service.DTO.TransferResponseDTO;
import com.banking.transaction_service.config.RequestValidator;
import com.banking.transaction_service.config.SagaStreamBroadcaster;
import com.banking.transaction_service.entity.Transaction;
import com.banking.transaction_service.service.TransactionService;
import com.banking.transaction_service.service.TransactionVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.http.codec.ServerSentEvent;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransactionRouteHandler {

    private final TransactionService transactionService;
    private final RequestValidator validator;
    private final TransactionVerificationService verificationService;
    private  final SagaStreamBroadcaster sagaStreamBroadcaster;

    public Mono<ServerResponse> transferMoneyForCustomer(ServerRequest request) {
        printIncomingLog();
        return request.bodyToMono(CustomerTransferRequest.class)
                .flatMap(validator::validateBody)
                .flatMap(transactionService::transferMoneyForCustomer)
                .flatMap(transaction -> ServerResponse.status(HttpStatus.CREATED)
                        .bodyValue(this.convertToTransferResponse(transaction)))
                .onErrorResume(ex -> {
                    log.error("Failed to process customer transfer: {}", ex.getMessage());
                    return Mono.error(ex);
                });
    }

    public Mono<ServerResponse> transferMoney(ServerRequest request) {
        printIncomingLog();
        return request.bodyToMono(TransactionRequest.class)
                .flatMap(validator::validateBody)
                .flatMap(transactionService::transferMoney)
                .flatMap(transaction -> ServerResponse.status(HttpStatus.CREATED)
                        .bodyValue(this.convertToTransferResponse(transaction)))
                .onErrorResume(ex -> {
                    log.error("Failed to process admin transfer: {}", ex.getMessage());
                    return Mono.error(ex);
                });
    }

    public Mono<ServerResponse> getTransactionByReferenceNumber(ServerRequest request) {
        printIncomingLog();
        Optional<String> ref = request.queryParam("referenceNumber");
        return Mono.justOrEmpty(ref)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reference Number not found")))
                .flatMap(transactionService::getTransactionByReferenceNumber)
                .flatMap(transaction -> ServerResponse.ok().bodyValue(this.convertToTransferResponse(transaction)));
    }

    public Mono<ServerResponse> getTransactionHistoryUser(ServerRequest request){
        printIncomingLog();
        return transactionService.getTransactionHistoryUser()
                .map(this::convertToTransferResponse)
                .collectList()
                .flatMap(history -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(history))
                .onErrorResume(ResponseStatusException.class, ex -> {
                    log.warn("Could not fetch transaction history: {}", ex.getReason());
                    if (ex.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(java.util.Collections.emptyList());
                    }
                    return ServerResponse.status(ex.getStatusCode()).bodyValue(java.util.Map.of("error", ex.getReason()));
                })
                .onErrorResume(ex -> {
                    log.error("Error fetching transaction history", ex);
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValue(java.util.Map.of("error", ex.getMessage()));
                });
    }

    public Mono<ServerResponse> getTransactionHistoryAdmin(ServerRequest request) {
        printIncomingLog();
        String accountNumber = request.pathVariable("id");
        if (accountNumber.isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid account number"));
        }

        Flux<TransferResponseDTO> historyFlux = transactionService
                .getTransactionHistoryByAccountNumber(accountNumber)
                .map(this::convertToTransferResponse);

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(historyFlux, TransferResponseDTO.class);
    }

    public Mono<ServerResponse> verifyOtp(ServerRequest request) {
        printIncomingLog();
        String referenceNumber = request.pathVariable("refId");
        Optional<String> otp = request.queryParam("otp");

        return Mono.justOrEmpty(otp)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Otp")))
                .flatMap(otpValue -> verificationService.verifyOtp(referenceNumber, otpValue)
                        .flatMap(res -> ServerResponse.ok().bodyValue(res)));
    }

    public Mono<ServerResponse> streamSagaStatus(ServerRequest request) {
        printIncomingLog();
        String referenceNumber = request.pathVariable("referenceNumber");
        Flux<ServerSentEvent<TransferResponseDTO>> sseFlux = sagaStreamBroadcaster
                .getStream(referenceNumber)
                .map(tx -> ServerSentEvent.<TransferResponseDTO>builder()
                        .id(tx.referenceNumber())
                        .event("saga-step")
                        .data(tx)
                        .build());
        return ServerResponse.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(sseFlux, ServerSentEvent.class);
    }

    private void printIncomingLog() {
        log.info(">> Handling request under TransactionRouteHandler");
    }

    private TransferResponseDTO convertToTransferResponse(Transaction transaction) {
        return TransferResponseDTO.builder()
                .id(transaction.getId())
                .senderAccountNumber(transaction.getSenderAccountNumber())
                .receiverAccountNumber(transaction.getReceiverAccountNumber())
                .referenceNumber(transaction.getReferenceNumber())
                .amount(transaction.getAmount())
                .createdAt(transaction.getCreatedAt())
                .completedAt(transaction.getCompletedAt())
                .status(transaction.getStatus())
                .description(transaction.getDescription())
                .failureReason(transaction.getFailureReason())
                .type(transaction.getType())
                .build();
    }
}