package com.yeihc.interfaces.rest;


import com.yeihc.application.command.OpenAccountCommand;
import com.yeihc.application.usecase.OpenAccountUseCase;
import com.yeihc.domain.model.Money;
import com.yeihc.interfaces.rest.dto.OpenAccountRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor

public class AccountController {

    private final OpenAccountUseCase openAccountUseCase;

    @PostMapping
    public UUID openAccount(@RequestBody OpenAccountRequest request) {
        return openAccountUseCase.execute(
                new OpenAccountCommand(
                        request.customerId(),
                        Money.of(request.initialDeposit())
                )
        );
    }

}
