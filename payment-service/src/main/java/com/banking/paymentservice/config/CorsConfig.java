package com.banking.paymentservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;

import java.util.List;

@RequiredArgsConstructor
@Configuration
public class CorsConfig {

//    @Bean
//    public CorsWebFilter corsWebConfig(){
//        CorsConfiguration config=new CorsConfiguration();
//        config.setAllowedOrigins(List.of("http://localhost:3000","http://localhost:5500","http://127.0.0.1:5500"));
//        config.setAllowedMethods(List.of("PUT","PATCH","DELETE","POST","DELETE","GET","OPTIONS"));
//        config.setAllowedHeaders(List.of("*"));
//        config.setExposedHeaders(List.of("X-Razorpay-Signature"));
//        config.setAllowCredentials(true);
//        config.setMaxAge(3600L);
//        UrlBasedCorsConfigurationSource source=new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**",config);
//        return new CorsWebFilter(source);
//    }
}
