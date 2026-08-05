package com.banking.transaction_service.config;

import com.banking.transaction_service.DTO.TransferResponseDTO;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SagaStreamBroadcaster {

    // Holds reactive event sinks keyed by transaction reference number
    private final Map<String, Sinks.Many<TransferResponseDTO>> sinks = new ConcurrentHashMap<>();

    public Flux<TransferResponseDTO> getStream(String referenceNumber) {
        Sinks.Many<TransferResponseDTO> sink = sinks.computeIfAbsent(
                referenceNumber,
                k -> Sinks.many().multicast().onBackpressureBuffer()
        );
        return sink.asFlux();
    }

    public void emitUpdate(TransferResponseDTO tx) {
        Sinks.Many<TransferResponseDTO> sink = sinks.get(tx.referenceNumber());
        if (sink != null) {
            sink.tryEmitNext(tx);

            // Auto-close SSE stream when SAGA reaches terminal state
            if ("COMPLETED".equals(tx.status()) || "FAILED_REFUNDED".equals(tx.status())) {
                sink.tryEmitComplete();
                sinks.remove(tx.referenceNumber());
            }
        }
    }
}
