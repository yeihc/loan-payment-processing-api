package com.yeihc.interfaces.rest.dto;
public record TransferResponse(
        String idempotencyKey,
        String status
) {}

