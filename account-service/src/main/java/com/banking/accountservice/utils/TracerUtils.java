package com.banking.accountservice.utils;

import com.banking.accountservice.config.TraceLoggingFilter;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

public class TracerUtils {
    public static Mono<String> getTraceId(){
        return Mono.deferContextual(ctxView->Mono.just(extractTraceFromView(ctxView)));
    }

    private static String extractTraceFromView(ContextView ctx){
        return ctx.getOrDefault(TraceLoggingFilter.CORRELATION_HEADER,"UNKNOWN_TRACE");
    }
}
