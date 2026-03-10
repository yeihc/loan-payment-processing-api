package com.yeihc.loanpayment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication(scanBasePackages = "com.yeihc")
@EnableJpaRepositories(basePackages = "com.yeihc.infrastructure.persistence.jpa")
@EntityScan(basePackages = "com.yeihc.domain.model")

public class LoanPaymentProcessingApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoanPaymentProcessingApiApplication.class, args);
	}

}
