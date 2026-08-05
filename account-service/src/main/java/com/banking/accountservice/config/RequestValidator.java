package com.banking.accountservice.config;

import jakarta.validation.ConstraintViolation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import jakarta.validation.Validator;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class RequestValidator {

    private final Validator validator;

    public <T> Mono<T> validate(T body){
        Set<ConstraintViolation<T>> errors=validator.validate(body);
        log.info("Validating the incoming request body");
        if(!errors.isEmpty()){
            log.error("The incoming request body is malformed");
            String message=errors.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(","));
            return Mono.error(new ServerWebInputException(message));
        }else{
            log.info("The incoming request body is validated");
            return Mono.just(body);
        }
    }
}
