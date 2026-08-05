package com.banking.paymentservice.routing;

import com.banking.paymentservice.DTO.CreatePaymentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
@Configuration
public class RouteHandler {

    private final PaymentHandler paymentHandler;

    @Bean
    @RouterOperations({
            @RouterOperation(path = "/api/v1/payments/create", method = RequestMethod.POST,
                    beanClass = PaymentHandler.class, beanMethod = "cretePaymentOrder",
                    operation = @Operation(
                            operationId = "createPaymentOrder",
                            summary = "Create Razorpay payment order",
                            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = CreatePaymentRequest.class)))
                    )),

            @RouterOperation(path = "/api/v1/payments/webhook", method = RequestMethod.POST,
                    beanClass = PaymentHandler.class, beanMethod = "handleWebhook",
                    operation = @Operation(
                            operationId = "handleWebhook",
                            summary = "Handle incoming webhook from payment provider",
                            parameters = { @Parameter(name = "X-Razorpay-Signature", in = ParameterIn.HEADER, required = true) }
                    ))
    })
    public RouterFunction<ServerResponse> manageRoute() {
        return RouterFunctions
                .route().path("/api/v1/payments",builder ->
                        builder.nest(accept(MediaType.APPLICATION_JSON),nestBuilder-> nestBuilder
                                        .POST("/create",paymentHandler::cretePaymentOrder))
                                .POST("/webhook",paymentHandler::handleWebhook)
                ).build();
    }
}
