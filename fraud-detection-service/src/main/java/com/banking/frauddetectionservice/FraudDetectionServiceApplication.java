package com.banking.frauddetectionservice;

import com.banking.frauddetectionservice.config.FraudDetectionServiceRuntimeHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.context.annotation.ImportRuntimeHints;

@SpringBootApplication
@ImportRuntimeHints(FraudDetectionServiceRuntimeHints.class)
@LoadBalancerClient(name = "ACCOUNT-SERVICE")
public class FraudDetectionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FraudDetectionServiceApplication.class, args);
	}

}
