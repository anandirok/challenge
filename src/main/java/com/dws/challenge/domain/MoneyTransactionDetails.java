package com.dws.challenge.domain;

import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MoneyTransactionDetails {

    private String transactionId;
    private BigDecimal transferAmount;
    private String accountFrom;
    private String accountTo;
    private ZonedDateTime transactionDate;
    private String success;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MoneyTransactionDetails that = (MoneyTransactionDetails) o;
        return transactionId.equals(that.transactionId) &&
                transferAmount.equals(that.transferAmount) &&
                accountFrom.equals(that.accountFrom) &&
                accountTo.equals(that.accountTo) &&
                transactionDate.equals(that.transactionDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId, transferAmount, accountFrom, accountTo, transactionDate);
    }
}
