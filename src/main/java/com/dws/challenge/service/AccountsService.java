package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.MoneyTransactionDetails;
import com.dws.challenge.domain.MoneyTransferRequest;
import com.dws.challenge.domain.MoneyTransferResponse;
import com.dws.challenge.exception.BadRequestExceptionClass;
import com.dws.challenge.repository.AccountsRepository;
import com.dws.challenge.repository.MoneyTransferRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class AccountsService {

    @Getter
    private final AccountsRepository accountsRepository;
    @Getter
    private final MoneyTransferRepository moneyTransferRepository;

    @Getter
    private final NotificationService notificationService;
    private final String SUCCESS = "SUCCESS";


    @Autowired
    public AccountsService(AccountsRepository accountsRepository, MoneyTransferRepository moneyTransferRepository, NotificationService notificationService) {
        this.accountsRepository = accountsRepository;
        this.moneyTransferRepository = moneyTransferRepository;
        this.notificationService = notificationService;
    }

    public void createAccount(Account account) {
        this.accountsRepository.createAccount(account);
    }

    public Account getAccount(String accountId) {
        return this.accountsRepository.getAccount(accountId);
    }

    // Here we are doing to check Validation and call transaction method
    public MoneyTransferResponse fundTransfer(MoneyTransferRequest moneyTransferRequest) {
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
        log.info("End basic validation before calling trasfer metghod");

        //
        log.info("Start calling Money transfer method after validation");
        MoneyTransferResponse moneyTransferResponse = this.moneyTransferRepository.fundTransfer(moneyTransferRequest);
        Account accountfrom = getAccount(moneyTransferRequest.getAccountFrom());
        Account accountTo = getAccount(moneyTransferRequest.getAccountTo());
        log.info("End calling Money transfer method.");

        log.info("Sending notification to sender and reciever");
        notificationToAccountHolder(moneyTransferResponse, accountfrom, accountTo, moneyTransferRequest.getTransferAmount());
        log.info("Transaction completed successfully");
        return moneyTransferResponse;
    }

    public MoneyTransactionDetails getTransactionDetails(String transactionId) {
        MoneyTransactionDetails moneyTransactionDetails = this.moneyTransferRepository.getTransactionDetails(transactionId);
        return moneyTransactionDetails;
    }

    private void notificationToAccountHolder(MoneyTransferResponse moneyTransferResponse, Account accountfrom, Account accountTo, BigDecimal transferAmt) {
        if (moneyTransferResponse.getStatus() == SUCCESS) {
            successNotification(moneyTransferResponse, accountfrom, accountTo, transferAmt);
        } else {
            // failed notification only to send the  from account(sender account)
            failedNotification(moneyTransferResponse, accountfrom);
        }
    }

    private void successNotification(MoneyTransferResponse moneyTransferResponse, Account accountfrom, Account accountTo, BigDecimal transferAmt) {
        // Sending Success Email Message to accountFrom
        notificationService.notifyAboutTransfer(accountfrom, "Successfully done transaction for Transaction id: " + moneyTransferResponse.getTransactionId());

        // Sending Success Email Message to accountTo
        notificationService.notifyAboutTransfer(accountTo, "Your account is "
                + accountTo.getAccountId() + " Amount ::" + transferAmt +
                "is credited from this account :: " + accountfrom.getAccountId());
    }

    private void failedNotification(MoneyTransferResponse moneyTransferResponse, Account accountfrom) {
        notificationService.notifyAboutTransfer(accountfrom, "Transaction failed for transaction id :: " + moneyTransferResponse.getTransactionId());

    }
}
