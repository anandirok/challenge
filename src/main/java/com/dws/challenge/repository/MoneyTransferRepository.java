package com.dws.challenge.repository;


import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.MoneyTransferRequest;
import com.dws.challenge.domain.MoneyTransferResponse;
import com.dws.challenge.exception.InsufficientAmountInAccountException;

import java.util.concurrent.ExecutionException;

public interface MoneyTransferRepository {
    MoneyTransferResponse fundTransfer(AccountsRepositoryInMemory target, MoneyTransferRequest moneyTransferRequest) throws InsufficientAmountInAccountException, ExecutionException, InterruptedException;

}
