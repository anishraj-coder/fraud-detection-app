package com.banking.paymentservice.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    public <T> Mono<T> validate(T body){
        log.info(">>>Validating incoming request body");
        Set<ConstraintViolation<T>> errors=validator.validate(body);

        if(!errors.isEmpty()){
            String errorMessage=errors.stream()
                    .map(ConstraintViolation::getMessage).collect(Collectors.joining(","));
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,errorMessage));
        }else{
            return Mono.just(body);
        }
    }
}
