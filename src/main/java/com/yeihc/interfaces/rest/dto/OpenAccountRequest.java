package com.yeihc.interfaces.rest.dto;


import java.math.BigDecimal;
import java.util.UUID;

public record OpenAccountRequest(
        UUID customerId,
        BigDecimal initialDeposit
) {}

