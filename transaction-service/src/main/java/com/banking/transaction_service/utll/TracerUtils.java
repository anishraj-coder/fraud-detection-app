package com.banking.transaction_service.utll;

import com.banking.transaction_service.config.TraceLoggingFilter;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

public class TracerUtils {
    public static Mono<String> getTraceId() {
        return Mono.deferContextual(ctxView -> Mono.just(extractTraceFromView(ctxView)));
    }

    public static String extractTraceFromView(ContextView ctx) {
        return ctx.getOrDefault(TraceLoggingFilter.CORRELATION_HEADER, "UNKNOWN_TRACE");
    }
}