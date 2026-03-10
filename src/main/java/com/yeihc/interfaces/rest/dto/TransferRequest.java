package com.yeihc.interfaces.rest.dto;


import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
        UUID sourceAccountId,
        UUID targetAccountId,
        BigDecimal amount,
        String idempotencyKey
) {}

