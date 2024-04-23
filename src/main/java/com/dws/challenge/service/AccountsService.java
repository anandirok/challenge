package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.MoneyTransferRequest;
import com.dws.challenge.domain.MoneyTransferResponse;
import com.dws.challenge.exception.AccountIdNotFoundException;
import com.dws.challenge.exception.BadRequestExceptionClass;
import com.dws.challenge.exception.InsufficientAmountInAccountException;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;

@Slf4j
@Service
public class AccountsService {

    @Getter
    private final AccountsRepository accountsRepository;

    @Getter
    private final NotificationService notificationService;

    private final String SUCCESS_MSG = "Money Transfer Successfully!!!";
    private final String FAILED_MSG = "Money Transfer failed";


    @Autowired
    public AccountsService(AccountsRepository accountsRepository, NotificationService notificationService) {
        this.accountsRepository = accountsRepository;
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
            throw new BadRequestExceptionClass("Transfer amount should be greater than balance!");
        }
        if (moneyTransferRequest.getAccountFrom().trim().equals(moneyTransferRequest.getAccountTo().trim())) {
            throw new BadRequestExceptionClass("Please check, To and From Account are the same!");
        }
        log.info("End basic validanotificationToAccountHoldertion before calling trasfer metghod");


        log.info("Start calling Money transfer method after validation");
        Account fromAccountDetails = getAccount(moneyTransferRequest.getAccountFrom());
        Account toAccountDetails = getAccount(moneyTransferRequest.getAccountTo());
        MoneyTransferResponse moneyTransferResponse =
                transfer(fromAccountDetails, toAccountDetails, moneyTransferRequest);
        return moneyTransferResponse;
    }

    public MoneyTransferResponse transfer(Account fromAccountDetails, Account toAccountDetails, MoneyTransferRequest moneyTransferRequest) throws InsufficientAmountInAccountException, ExecutionException, InterruptedException {
        validateAccountDetails(moneyTransferRequest, fromAccountDetails, toAccountDetails);
        Lock fromAccountLock = fromAccountDetails.getLock();
        Lock toAccountLock = toAccountDetails.getLock();
        try {
            fromAccountLock.lock();
            toAccountLock.lock();
            // Perform the transfer
            if (fromAccountDetails.transfer(toAccountDetails, moneyTransferRequest.getTransferAmount())) {
                accountsRepository.updateAccountDetails(toAccountDetails);
                accountsRepository.updateAccountDetails(fromAccountDetails);
                notificationToAccountHolder(true, moneyTransferRequest, fromAccountDetails, toAccountDetails);
                log.info("Transfer successful: $" + moneyTransferRequest.getTransferAmount() + " transferred from Account " + fromAccountDetails.getAccountId() + " to Account " + toAccountDetails.getAccountId());
                return MoneyTransferResponse.builder().message(SUCCESS_MSG).build();
            } else {
                notificationToAccountHolder(false, moneyTransferRequest, fromAccountDetails, toAccountDetails);
                log.info("Transfer failed: Insufficient funds in Account " + fromAccountDetails.getAccountId());
                return MoneyTransferResponse.builder().message(FAILED_MSG).build();
            }
        } finally {
            toAccountLock.unlock();
            fromAccountLock.unlock();
        }

    }

    private void validateAccountDetails(final MoneyTransferRequest moneyTransferRequest, Account fromAccountDetails, Account toAccountDetails) {
        if (fromAccountDetails == null) {
            throw new AccountIdNotFoundException("Invalid fromAccount :: " + moneyTransferRequest.getAccountFrom());
        }
        if (toAccountDetails == null) {
            throw new AccountIdNotFoundException("Invalid toAccount :: " + moneyTransferRequest.getAccountTo());
        }

        // balance greater than zero and no negative value consider
        if ((fromAccountDetails.getBalance().compareTo(BigDecimal.ZERO) == 0)) {
            throw new InsufficientAmountInAccountException("Insufficient amount in from account");
        }
        // balance greater than transfer amount
        if (!fromAccountDetails.getBalance().equals(moneyTransferRequest.getTransferAmount())) {
            if (!(fromAccountDetails.getBalance().compareTo(moneyTransferRequest.getTransferAmount()) == 1)) {
                throw new InsufficientAmountInAccountException("Insufficient amount in from account balance");
            }
        }
    }

    private void notificationToAccountHolder(final boolean status, final MoneyTransferRequest moneyTransferRequest, Account accountfrom, Account accountTo) {
        if (status) {
            successNotification(moneyTransferRequest, accountfrom, accountTo);
        } else {
            // failed notification only to send the  from account(sender account)
            failedNotification(moneyTransferRequest, accountfrom);
        }
    }

    private void successNotification(MoneyTransferRequest moneyTransferRequest, Account accountfrom, Account accountTo) {
        // Sending Success Email Message to accountFrom
        notificationService.notifyAboutTransfer(accountfrom, "Successfully Transfer the Money");

        // Sending Success Email Message to accountTo
        notificationService.notifyAboutTransfer(accountTo, "Your account is "
                + accountTo.getAccountId() + " Amount ::" + moneyTransferRequest.getTransferAmount() +
                " is credited from this account :: " + accountfrom.getAccountId());
    }

    private void failedNotification(MoneyTransferRequest moneyTransferRequest, Account accountfrom) {
        notificationService.notifyAboutTransfer(accountfrom, "Transaction failed ");

    }

}
