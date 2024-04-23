package com.dws.challenge.repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.MoneyTransferRequest;
import com.dws.challenge.exception.AccountIdNotFoundException;
import com.dws.challenge.exception.DuplicateAccountIdException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    private final Lock lock = new ReentrantLock();


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
        try {

            Account fromAccount = getAccount(transfer.getAccountFrom());
            if (getAccount(fromAccount.getAccountId()) == null) {
                throw new AccountIdNotFoundException("Invalid fromAccount :: " + fromAccount.getAccountId());
            }
            lock.lock();
            if ((!(fromAccount.getBalance().compareTo(BigDecimal.ZERO) == 0)) && fromAccount.getBalance().equals(transfer.getTransferAmount()) || fromAccount.getBalance().compareTo(transfer.getTransferAmount()) == 1) {
                BigDecimal balanceAmount = fromAccount.getBalance().subtract(transfer.getTransferAmount());
                this.accounts.put(fromAccount.getAccountId(), Account.builder().accountId(fromAccount.getAccountId()).balance(balanceAmount).build());
                return true;
            }
        } finally {
            lock.unlock();
        }
        return false;
    }

    // Money credited to accpunt
    @Override
    public Boolean credit(final MoneyTransferRequest transfer) {
        try {
            Account toAccount = getAccount(transfer.getAccountTo());
            if (toAccount == null) {
                return false;
            }
            lock.lock();
            BigDecimal balanceAmount = toAccount.getBalance().add(transfer.getTransferAmount());
            this.accounts.put(toAccount.getAccountId(), Account.builder().accountId(toAccount.getAccountId()).balance(balanceAmount).build());
        } finally {
            lock.unlock();
        }
        return true;
    }
}
