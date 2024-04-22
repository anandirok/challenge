package com.dws.challenge.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;

@Data
@Builder
@Getter
public class MoneyTransferRequest {

    @NotNull(message = "account from shuld not be null!")
    @NotEmpty(message = "account from shuld not be empty!")
    private final String accountFrom;

    @NotNull(message = "account to shuld not be null!")
    @NotEmpty(message = "account to shuld not be empty!")
    private final String accountTo;

    @NotNull(message = "Transfer amount shuld not be null!")
    @Min(value = 0, message = "Initial balance must be positive.")
    private BigDecimal transferAmount;

    public MoneyTransferRequest(@NotNull @NotEmpty String accountFrom,
                                @NotNull @NotEmpty String accountTo,
                                @NotNull @Min(value = 0, message = "Transfer amount must be positive.") BigDecimal transferAmount) {
        this.accountFrom = accountFrom;
        this.accountTo = accountTo;
        this.transferAmount = transferAmount;
    }
}
