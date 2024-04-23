package com.dws.challenge.repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.MoneyTransferRequest;
import com.dws.challenge.domain.MoneyTransferResponse;
import com.dws.challenge.exception.AccountIdNotFoundException;
import com.dws.challenge.exception.InsufficientAmountInAccountException;
import com.dws.challenge.service.NotificationService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Repository
public class MoneyTransferSystem implements MoneyTransferRepository {

    private final Lock lock1 = new ReentrantLock();
    private final Lock lock2 = new ReentrantLock();

    private final String SUCCESS = "Money Transfer Successfully!!!";
    private final String FAILED = "Money Transfer failed";

    @Getter
    private final AccountsRepositoryInMemory accountsRepositoryInMemory;

    @Getter
    private final NotificationService notificationService;

    public MoneyTransferSystem(AccountsRepositoryInMemory accountsRepositoryInMemory, NotificationService notificationService) {
        this.accountsRepositoryInMemory = accountsRepositoryInMemory;
        this.notificationService = notificationService;
    }

    @Override
    public MoneyTransferResponse fundTransfer(AccountsRepositoryInMemory target, MoneyTransferRequest moneyTransferRequest) throws InsufficientAmountInAccountException, ExecutionException, InterruptedException {

        Account fromAccountDetails = target.getAccount(moneyTransferRequest.getAccountFrom());
        Account toAccountDetails = target.getAccount(moneyTransferRequest.getAccountTo());
        validateAccountDetails(moneyTransferRequest, fromAccountDetails, toAccountDetails);
        String accountFrom = fromAccountDetails.getAccountId();
        String accountTo = toAccountDetails.getAccountId();

        Lock firstLock = accountFrom.compareTo(accountTo) < 0 ? lock1 : lock2;
        Lock secondLock = accountFrom.compareTo(accountTo) < 0 ? lock2 : lock1;
        try {
            firstLock.lock();
            secondLock.lock();
            // Perform the transfer
            if (accountsRepositoryInMemory.debit(moneyTransferRequest)) {
                boolean task2 = accountsRepositoryInMemory.credit(moneyTransferRequest);
                notificationToAccountHolder(task2, moneyTransferRequest, fromAccountDetails, toAccountDetails);
                log.info("Transfer successful: $" + moneyTransferRequest.getTransferAmount() + " transferred from Account " + accountFrom + " to Account " + accountTo);
                return MoneyTransferResponse.builder().message(SUCCESS).build();
            } else {
                notificationToAccountHolder(false, moneyTransferRequest, fromAccountDetails, toAccountDetails);
                log.info("Transfer failed: Insufficient funds in Account " + accountFrom);
                return MoneyTransferResponse.builder().message(FAILED).build();
            }
        } finally {
            secondLock.unlock();
            firstLock.unlock();
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
