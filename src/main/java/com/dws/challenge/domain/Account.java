package com.dws.challenge.domain;

import com.dws.challenge.exception.AccountIdNotFoundException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Builder
public class Account {

    @NotNull
    @NotEmpty
    private final String accountId;

    @JsonIgnore
    private final Lock lock = new ReentrantLock();

    @NotNull
    @Min(value = 0, message = "Initial balance must be positive.")
    private BigDecimal balance;

    public Account(String accountId) {
        this.accountId = accountId;
        this.balance = BigDecimal.ZERO;
    }

    @JsonCreator
    public Account(@JsonProperty("accountId") String accountId,
                   @JsonProperty("balance") BigDecimal balance) {
        this.accountId = accountId;
        this.balance = balance;
    }

    public void credit(BigDecimal amount) {
        lock.lock();
        try {
            this.balance = balance.add(amount);
            System.out.println(Thread.currentThread().getName() + " deposited " + amount + ". New balance: " + this.balance);
        } finally {
            lock.unlock();
        }
    }

    public void debit(BigDecimal amount) {
        try {
            lock.lock();
            if ((!(balance.compareTo(BigDecimal.ZERO) == 0)) && balance.equals(amount) || balance.compareTo(amount) == 1) {
                this.balance = balance.subtract(amount);
            } else {
                System.out.println(Thread.currentThread().getName() + " cannot withdraw " + amount + " due to insufficient balance.");
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean transfer(Account toAccount, BigDecimal amount) {
        lock.lock();
        try {
            if ((!(balance.compareTo(BigDecimal.ZERO) == 0)) && balance.equals(amount) || balance.compareTo(amount) == 1) {
                debit(amount);
                toAccount.credit(amount);
                log.info(Thread.currentThread().getName() + " transferred " + amount + " to " + toAccount + ".");
                return true;
            } else {
                log.info(Thread.currentThread().getName() + " cannot transfer " + amount + " due to insufficient balance.");
                return false;
            }
        } finally {
            lock.unlock();
        }
    }
}
