package com.dws.challenge.repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.MoneyTransactionDetails;
import com.dws.challenge.domain.MoneyTransferRequest;
import com.dws.challenge.domain.MoneyTransferResponse;
import com.dws.challenge.exception.AccountIdNotFoundException;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InsufficientAmountInAccountException;
import com.dws.challenge.exception.TransactionIdNotFoundException;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.LongStream;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository, MoneyTransferRepository {

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    private final Map<String, MoneyTransactionDetails> transactionDetails = new ConcurrentHashMap<>();

    private final String SUCCESS = "SUCCESS";
    private final String FAILED = "FAILED";
    private final String UTC = "UTC";
    int n=1;


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
    public  synchronized boolean debit(MoneyTransferRequest transfer) {
        Account fromAccount = this.accounts.get(transfer.getAccountFrom());
        if (getAccount(fromAccount.getAccountId()) == null) {
            throw new AccountIdNotFoundException("Invalid fromAccount :: " + fromAccount.getAccountId());
        }
        if ((fromAccount.getBalance().compareTo(BigDecimal.ZERO) == 0)) {
            return false;
        }
        if(fromAccount.getBalance().equals(transfer.getTransferAmount()) || fromAccount.getBalance().compareTo(transfer.getTransferAmount()) == 1){
            BigDecimal balanceAmount = fromAccount.getBalance().subtract(transfer.getTransferAmount());
            this.accounts.put(fromAccount.getAccountId(), Account.builder().accountId(fromAccount.getAccountId()).balance(balanceAmount).build());
            return true;
        }
        return false;
    }

    // Money credited to accpunt
    @Override
    public synchronized boolean credit(MoneyTransferRequest transfer) {
        Account toAccount = this.accounts.get(transfer.getAccountTo());
        if (toAccount == null) {
            return false;
        }
        BigDecimal balanceAmount = toAccount.getBalance().add(transfer.getTransferAmount());
        this.accounts.put(toAccount.getAccountId(), Account.builder().accountId(toAccount.getAccountId()).balance(balanceAmount).build());
        return true;
    }

    @Override
    public MoneyTransactionDetails getTransactionDetails(String transactionId) {
        MoneyTransactionDetails request = transactionDetails.get(transactionId);
        if (request == null) {
            throw new TransactionIdNotFoundException("This transactionId is a invalid! " + transactionId);
        }
        return request;
    }

    @Override
    public MoneyTransferResponse fundTransfer(MoneyTransferRequest moneyTransferRequest) throws InsufficientAmountInAccountException {
        MoneyTransferResponse transction = null;
        Account fromAccountDetails = getAccount(moneyTransferRequest.getAccountFrom());
        validateAccountDetails(moneyTransferRequest, fromAccountDetails);
        synchronized (this) {
            Random rm = new Random();
            String status = FAILED;
            String message = "";
            ZonedDateTime timezone = ZonedDateTime.now(ZoneId.of(UTC));
            //int n = rm.nextInt(10000 + 1);
            // Money deducted from accpunt synchronized
            if (debit(moneyTransferRequest)) {

                // Money credited to accpunt synchronized
                boolean isSuccess = credit(moneyTransferRequest);
                if (isSuccess) {
                    status = SUCCESS;
                    message = "Successfully transfer the fund!";
                } else {
                    // rollback the transaction
                    status = FAILED;
                    message = "Transaction failed!";
                    credit(MoneyTransferRequest.builder()
                            .accountTo(moneyTransferRequest.getAccountFrom())
                            .transferAmount(moneyTransferRequest.getTransferAmount()).build());

                }
            } else {
                status = FAILED;
                message = "Transaction failed!";
            }

            transction = MoneyTransferResponse.builder()
                    .status(status)
                    .transactionId(String.valueOf(n))
                    .message(message)
                    .build();

            transactionDetails.put(String.valueOf(n), MoneyTransactionDetails.builder()
                    .transactionId(String.valueOf(n))
                    .accountFrom(moneyTransferRequest.getAccountFrom())
                    .accountTo(moneyTransferRequest.getAccountTo())
                    .success(status)
                    .transactionDate(timezone).build());
            n++;
        }
        return transction;
    }

    private void validateAccountDetails(MoneyTransferRequest moneyTransferRequest, Account fromAccountDetails) {
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
        if(!fromAccountDetails.getBalance().equals(moneyTransferRequest.getTransferAmount())){
            if (!(fromAccountDetails.getBalance().compareTo(moneyTransferRequest.getTransferAmount()) == 1)) {
                throw new InsufficientAmountInAccountException("Insufficient amount in from account balance");
            }
        }
    }


}
