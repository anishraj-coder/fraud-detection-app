package com.banking.paymentservice.service.impl;

import com.banking.paymentservice.DTO.CreatePaymentRequest;
import com.banking.paymentservice.DTO.PaymentCompletedEvent;
import com.banking.paymentservice.DTO.PaymentOrderResponse;
import com.banking.paymentservice.config.PaymentProducerConfig;
import com.banking.paymentservice.config.RazorpayConfig;
import com.banking.paymentservice.entity.Payment;
import com.banking.paymentservice.entity.enums.PaymentStatus;
import com.banking.paymentservice.repository.PaymentRepository;
import com.banking.paymentservice.service.PaymentService;
import com.banking.paymentservice.util.UtilClass;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RazorpayClient razorpayClient;
    private final UtilClass utilClass;
    private final RazorpayConfig razorpayConfig;
    private final PaymentProducerConfig paymentProducerConfig;

    @Override
    public Mono<PaymentOrderResponse> createPayment(CreatePaymentRequest request) {
        log.info("[START] Creating payment order | AccNo: {}, Amount: {}",
                request.getAccountNumber(), request.getAmount());

        int amountInPaise = request.getAmount().multiply(BigDecimal.valueOf(100)).intValue();
        String receiptId = utilClass.generateReceiptId();

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("receipt", receiptId);
        orderRequest.put("currency", "INR");

        return Mono.fromCallable(() -> {
                    log.debug("Calling Razorpay SDK to create order with Receipt: {}", receiptId);
                    return razorpayClient.orders.create(orderRequest);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(order -> {
                    String razorpayOrderId = order.get("id");
                    log.info("Successfully created Razorpay Order ID: {}", razorpayOrderId);

                    Payment payment = Payment.builder()
                            .id(UUID.randomUUID().toString() + "-" + LocalDateTime.now())
                            .razorPaymentId(razorpayOrderId)
                            .createdAt(LocalDateTime.now())
                            .amount(request.getAmount())
                            .paymentStatus(PaymentStatus.CREATED)
                            .accountNumber(request.getAccountNumber())
                            .description(request.getDescription())
                            .updatedAt(LocalDateTime.now())
                            .currency("INR")
                            .isNew(true)
                            .build();

                    log.debug("Saving payment record to DB for Razorpay Order ID: {}", razorpayOrderId);
                    return paymentRepository.save(payment)
                            .doOnNext(saved -> log.info("Saved Payment record to DB with ID: {}", saved.getId()))
                            .map(saved -> buildResponse(saved, razorpayOrderId));
                })
                .doOnSuccess(response ->
                        log.info("[SUCCESS] Payment pipeline finished for AccNo: {}, OrderId: {}",
                                response.getAccountNumber(), response.getRazorpayOrderId()))
                .doOnError(ex ->
                        log.error("[FAILED] Error during payment order creation process: {}", ex.getMessage(), ex));
    }

    @Override
    public Mono<Void> handleWebhook(String payload, String signature) {
        return Mono.fromRunnable(() -> {
                    log.info("[WEBHOOK RECEIVED] Validating webhook signature...");
                    boolean isValid = verifySignature(payload, signature, razorpayConfig.getWebhookSecret());
                    if (!isValid) {
                        log.error("[WEBHOOK REJECTED] Invalid signature received in webhook payload!");
                        throw new IllegalArgumentException("Invalid Razorpay Webhook Signature");
                    }
                    log.info("[WEBHOOK VERIFIED] Webhook signature verified successfully.");
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then(Mono.defer(() -> {
                    JSONObject jsonObject = new JSONObject(payload);
                    String event = jsonObject.optString("event");
                    log.info("[WEBHOOK PROCESSING] Processing Event Type: {}", event);
                    JSONObject payloadEntity = jsonObject.getJSONObject("payload")
                            .getJSONObject("payment")
                            .getJSONObject("entity");

                    String razorpayOrderId = payloadEntity.getString("order_id");
                    log.info("[WEBHOOK PROCESSING] Extracted Razorpay Order ID: {}", razorpayOrderId);
                    PaymentStatus status = mapToStatus(event);

                    return paymentRepository.findByRazorPaymentId(razorpayOrderId)
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("No payment record found for Razorpay Order ID: " + razorpayOrderId)))
                            .flatMap(payment -> {
                                log.info("[DB UPDATE] Updating payment status in DB from {} to {} for OrderId: {}",
                                        payment.getPaymentStatus(), status, razorpayOrderId);
                                payment.setIsNewToFalse();
                                payment.setPaymentStatus(status);
                                if (status == PaymentStatus.FAILED) {
                                    String errorDescription = payloadEntity
                                            .optString("error_description", "Payment failed");
                                    payment.setFailureReason(errorDescription);
                                }
                                return paymentRepository.save(payment);
                            })
                            .flatMap(updated -> {
                                log.info("[WEBHOOK SUCCESS] Database payment status updated to {} for Order ID: {}",
                                        status, razorpayOrderId);
                                if (status == PaymentStatus.COMPLETED) {
                                    PaymentCompletedEvent completedEvent = PaymentCompletedEvent.builder()
                                            .accountNumber(updated.getAccountNumber())
                                            .amount(updated.getAmount())
                                            .razorpayOrderId(razorpayOrderId)
                                            .build();
                                    return paymentProducerConfig.publishPaymentCompleted(completedEvent);
                                }
                                return Mono.empty();
                            })
                            .doOnError(err -> log.error("[WEBHOOK ERROR] Failed to process webhook for Order ID: {}. Error: {}",
                                    razorpayOrderId, err.getMessage(), err))
                            .then();
                }));
    }

    private PaymentStatus mapToStatus(String event) {
        return switch (event) {
            case "payment.captured", "order.paid" -> PaymentStatus.COMPLETED;
            case "payment.failed" -> PaymentStatus.FAILED;
            case "payment.authorized" -> PaymentStatus.PENDING;
            case "refund.processed" -> PaymentStatus.REFUNDED;
            default -> {
                log.warn("Unhandled Webhook event type: {}. Defaulting to PENDING", event);
                yield PaymentStatus.PENDING;
            }
        };
    }

    private PaymentOrderResponse buildResponse(Payment payment, String razorpayOrderId) {
        return PaymentOrderResponse.builder()
                .accountNumber(payment.getAccountNumber())
                .amount(payment.getAmount())
                .status(payment.getPaymentStatus().name())
                .currency(payment.getCurrency())
                .razorpayOrderId(razorpayOrderId)
                .razorpayKeyId(razorpayConfig.razorpayId)
                .build();
    }

    private boolean verifySignature(String payload, String signature, String secret) {
        try {
            return Utils.verifyWebhookSignature(payload, signature, secret);
        } catch (Exception e) {
            log.error("Error occurred while validating Razorpay signature: {}", e.getMessage());
            return false;
        }
    }
}