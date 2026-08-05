package com.banking.transaction_service.config;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class RequestValidator {
    private final Validator validator;

    public <T> Mono<T> validateBody(T body){
        Set<ConstraintViolation<T>> errors=validator.validate(body);
        log.info("Validating the request of incoming request");
        if(!errors.isEmpty()){
            log.error("The incoming request is invalid");
            String errorMessage=errors.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(","));
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,errorMessage));
        }else{
            log.info("The request body is completely valid");
            return Mono.just(body);
        }
    }
}
