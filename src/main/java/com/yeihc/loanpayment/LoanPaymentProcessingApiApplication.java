package com.yeihc.loanpayment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.yeihc")
public class LoanPaymentProcessingApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoanPaymentProcessingApiApplication.class, args);
	}

}
