package com.banking.paymentservice.routing;

import com.banking.paymentservice.DTO.CreatePaymentRequest;
import com.banking.paymentservice.config.RequestValidator;
import com.banking.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentHandler {
    private final RequestValidator validator;
    private final PaymentService paymentService;

    public Mono<ServerResponse> cretePaymentOrder(ServerRequest request){
        log.info(">>Intercepting the request for payment creation ");
        return request.bodyToMono(CreatePaymentRequest.class)
                .flatMap(validator::validate)
                .flatMap(paymentService::createPayment)
                .flatMap(res->ServerResponse.status(HttpStatus.CREATED).bodyValue(res))
                .doOnError(ex->log.error("Error creating payment order: {}", ex.getMessage()));
    }

    public Mono<ServerResponse> handleWebhook(ServerRequest request){
        log.info("[WEBHOOK ENTRY] Incoming Webhook HTTP request received.");
        String signature=request.headers().firstHeader("X-Razorpay-Signature");
        if (signature == null || signature.isBlank()) {
            log.error("[WEBHOOK REJECTED] Missing X-Razorpay-Signature header!");
            return ServerResponse.status(HttpStatus.BAD_REQUEST)
                    .bodyValue("Missing X-Razorpay-Signature header");
        }

        // Read raw request body payload string
        return request.bodyToMono(String.class)
                .flatMap(payload -> paymentService.handleWebhook(payload, signature))
                .then(ServerResponse.ok().bodyValue("Webhook processed successfully"))
                .onErrorResume(IllegalArgumentException.class, ex -> {
                    log.error("[WEBHOOK ERROR] Invalid Signature/Request: {}", ex.getMessage());
                    return ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue(ex.getMessage());
                })
                .onErrorResume(ex -> {
                    log.error("[WEBHOOK ERROR] Internal server error processing webhook: {}", ex.getMessage(), ex);
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .bodyValue("Internal Server Error processing webhook");
                });
    }
}
