package com.dws.challenge.repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.MoneyTransactionDetails;
import com.dws.challenge.domain.MoneyTransferRequest;
import com.dws.challenge.domain.MoneyTransferResponse;
import com.dws.challenge.exception.InsufficientAmountInAccountException;

import java.util.Map;

public interface MoneyTransferRepository {
    MoneyTransferResponse fundTransfer(MoneyTransferRequest moneyTransferRequest) throws InsufficientAmountInAccountException;
    boolean debit(MoneyTransferRequest transfer);
    boolean credit(MoneyTransferRequest transfer);
    MoneyTransactionDetails getTransactionDetails(String transactionId);
}
