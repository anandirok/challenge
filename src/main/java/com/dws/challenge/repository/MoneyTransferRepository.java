package com.dws.challenge.repository;


import com.dws.challenge.domain.MoneyTransferRequest;
import com.dws.challenge.domain.MoneyTransferResponse;
import com.dws.challenge.exception.InsufficientAmountInAccountException;

import java.util.concurrent.ExecutionException;

public interface MoneyTransferRepository {
    MoneyTransferResponse fundTransfer(final MoneyTransferRequest moneyTransferRequest) throws InsufficientAmountInAccountException, ExecutionException, InterruptedException;
    Boolean debit(final MoneyTransferRequest transfer);
    Boolean credit(final MoneyTransferRequest transfer);
}
