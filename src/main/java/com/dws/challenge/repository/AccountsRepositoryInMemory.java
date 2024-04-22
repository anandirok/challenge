package com.dws.challenge.repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.MoneyTransferRequest;
import com.dws.challenge.domain.MoneyTransferResponse;
import com.dws.challenge.exception.AccountIdNotFoundException;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InsufficientAmountInAccountException;
import com.dws.challenge.service.NotificationService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

@Slf4j
@Repository
public class AccountsRepositoryInMemory implements AccountsRepository, MoneyTransferRepository {

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    private Semaphore lock = new Semaphore(5);

    private final String SUCCESS = "Money Transfer Successfully!!!";

    @Getter
    private final NotificationService notificationService;

    public AccountsRepositoryInMemory(NotificationService notificationService) {
        this.notificationService = notificationService;
    }


    @Override
    public void createAccount(Account account) throws DuplicateAccountIdException {
        Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
        if (previousAccount != null) {
            throw new DuplicateAccountIdException(
                    "Account id " + account.getAccountId() + " already exists!");
        }
    }

    @Override
    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }

    @Override
    public void clearAccounts() {
        accounts.clear();
    }

    // Money deducted from accpunt
    @Override
    public Boolean debit(final MoneyTransferRequest transfer) {
        Account fromAccount = getAccount(transfer.getAccountFrom());
        if (getAccount(fromAccount.getAccountId()) == null) {
            throw new AccountIdNotFoundException("Invalid fromAccount :: " + fromAccount.getAccountId());
        }
        if ((fromAccount.getBalance().compareTo(BigDecimal.ZERO) == 0)) {
            return false;
        }
        try {
            lock.acquire();
            if (fromAccount.getBalance().equals(transfer.getTransferAmount()) || fromAccount.getBalance().compareTo(transfer.getTransferAmount()) == 1) {
                BigDecimal balanceAmount = fromAccount.getBalance().subtract(transfer.getTransferAmount());
                this.accounts.put(fromAccount.getAccountId(), Account.builder().accountId(fromAccount.getAccountId()).balance(balanceAmount).build());
                return true;
            }
        } catch (Exception ex) {
            ex.getMessage();
        } finally {
            lock.release();
        }
        return false;
    }

    // Money credited to accpunt
    @Override
    public Boolean credit(final MoneyTransferRequest transfer) {
        Account toAccount = getAccount(transfer.getAccountTo());
        if (toAccount == null) {
            return false;
        }
        try {
            lock.acquire();
            BigDecimal balanceAmount = toAccount.getBalance().add(transfer.getTransferAmount());
            this.accounts.put(toAccount.getAccountId(), Account.builder().accountId(toAccount.getAccountId()).balance(balanceAmount).build());
        } catch (Exception ex) {
            ex.getMessage();
        } finally {
            lock.release();
        }
        return true;
    }

    @Override
    public MoneyTransferResponse fundTransfer(AccountsRepositoryInMemory mome, final MoneyTransferRequest moneyTransferRequest) throws InsufficientAmountInAccountException, ExecutionException, InterruptedException {
        Account fromAccountDetails = getAccount(moneyTransferRequest.getAccountFrom());
        validateAccountDetails(moneyTransferRequest, fromAccountDetails);
        boolean task1 = debit(moneyTransferRequest);
        boolean task2 = mome.credit(moneyTransferRequest);
        notificationToAccountHolder(task2, moneyTransferRequest);
        return MoneyTransferResponse.builder().message(SUCCESS).build();
    }

    private void validateAccountDetails(final MoneyTransferRequest moneyTransferRequest, Account fromAccountDetails) {
        if (getAccount(moneyTransferRequest.getAccountFrom()) == null) {
            throw new AccountIdNotFoundException("Invalid fromAccount :: " + moneyTransferRequest.getAccountFrom());
        }
        if (getAccount(moneyTransferRequest.getAccountTo()) == null) {
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

    private void notificationToAccountHolder(final boolean status, final MoneyTransferRequest moneyTransferRequest) {
        Account accountfrom = getAccount(moneyTransferRequest.getAccountFrom());
        Account accountTo = getAccount(moneyTransferRequest.getAccountTo());
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
