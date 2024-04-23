package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.MoneyTransferRequest;
import com.dws.challenge.domain.MoneyTransferResponse;
import com.dws.challenge.exception.AccountIdNotFoundException;
import com.dws.challenge.exception.BadRequestExceptionClass;
import com.dws.challenge.exception.InsufficientAmountInAccountException;
import com.dws.challenge.repository.AccountsRepository;
import com.dws.challenge.repository.AccountsRepositoryInMemory;
import com.dws.challenge.repository.MoneyTransferRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class AccountsService {

    @Getter
    private final AccountsRepository accountsRepository;
    @Getter
    private final MoneyTransferRepository moneyTransferRepository;


    private final AccountsRepositoryInMemory accountsRepositoryInMemory;

    @Getter
    private final NotificationService notificationService;
    private final String SUCCESS = "SUCCESS";


    @Autowired
    public AccountsService(AccountsRepository accountsRepository, MoneyTransferRepository moneyTransferRepository, AccountsRepositoryInMemory accountsRepositoryInMemory, NotificationService notificationService) {
        this.accountsRepository = accountsRepository;
        this.moneyTransferRepository = moneyTransferRepository;
        this.accountsRepositoryInMemory = accountsRepositoryInMemory;
        this.notificationService = notificationService;
    }

    public void createAccount(Account account) {
        this.accountsRepository.createAccount(account);
    }

    public Account getAccount(String accountId) {
        return this.accountsRepository.getAccount(accountId);
    }

    // Here we are doing to check Validation and call transaction method
    public MoneyTransferResponse fundTransfer(MoneyTransferRequest moneyTransferRequest) throws ExecutionException, InterruptedException {
        log.info("Start basic validation before calling trasfer metghod");
        if (moneyTransferRequest.getAccountFrom() == null || moneyTransferRequest.getAccountFrom() == "") {
            throw new BadRequestExceptionClass("Account from should not be empty/null!");
        }
        if (moneyTransferRequest.getAccountTo() == null || moneyTransferRequest.getAccountTo() == "") {
            throw new BadRequestExceptionClass("Account to should not be null.empty!");
        }
        if (moneyTransferRequest.getTransferAmount() != null && moneyTransferRequest.getTransferAmount().compareTo(BigDecimal.ZERO) == 0) {
            throw new BadRequestExceptionClass("Transfer amount should be greater than zero!");
        } else if (moneyTransferRequest.getTransferAmount().compareTo(BigDecimal.ZERO) == 1) {
            log.info("Balance id greater than transfer amount");
        } else {
            throw new BadRequestExceptionClass("Transfer amount should be greater than zero!");
        }
        if (moneyTransferRequest.getAccountFrom().trim().equals(moneyTransferRequest.getAccountTo().trim())) {
            throw new BadRequestExceptionClass("Please check, To and From Account are the same!");
        }
        log.info("End basic validanotificationToAccountHoldertion before calling trasfer metghod");


        log.info("Start calling Money transfer method after validation");
        MoneyTransferResponse moneyTransferResponse =
                this.moneyTransferRepository.fundTransfer(accountsRepositoryInMemory, moneyTransferRequest);
        return moneyTransferResponse;
    }



}
