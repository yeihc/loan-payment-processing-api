package com.yeihc.interfaces.rest;

import com.yeihc.application.command.TransferFundsCommand;
import com.yeihc.application.usecase.TransferFundsUseCase;
import com.yeihc.domain.model.Money;
import com.yeihc.interfaces.rest.dto.TransferRequest;
import com.yeihc.interfaces.rest.dto.TransferResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferFundsUseCase transferFundsUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public TransferResponse transfer(@RequestBody TransferRequest request) {

        transferFundsUseCase.execute(
                new TransferFundsCommand(
                        request.sourceAccountId(),
                        request.targetAccountId(),
                        Money.of(request.amount()),
                        request.idempotencyKey()
                )
        );

        // Respuesta útil para demo (sin consultar DB)
        return new TransferResponse(
                request.idempotencyKey(),
                "ACCEPTED"
        );
    }
}