package com.banking.transaction_service.config;

import io.micrometer.context.ContextRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;

@Configuration
public class MdcTracingConfig {

    @PostConstruct
    public void init() {
        // 1. Enable automatic context propagation across Reactor threads
        Hooks.enableAutomaticContextPropagation();

        // 2. Register X-Correlation-ID to automatically sync from Reactor Context -> MDC
        ContextRegistry.getInstance().registerThreadLocalAccessor(
                TraceLoggingFilter.CORRELATION_HEADER, // Context key: "X-Correlation-ID"
                () -> MDC.get(TraceLoggingFilter.CORRELATION_HEADER), // Get from MDC
                traceId -> {
                    if (traceId != null) {
                        MDC.put(TraceLoggingFilter.CORRELATION_HEADER, traceId); // Put in MDC
                    } else {
                        MDC.remove(TraceLoggingFilter.CORRELATION_HEADER);
                    }
                },
                () -> MDC.remove(TraceLoggingFilter.CORRELATION_HEADER) // Reset MDC
        );
    }
}