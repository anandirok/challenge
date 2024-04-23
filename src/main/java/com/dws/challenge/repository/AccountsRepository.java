package com.dws.challenge.repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.MoneyTransferRequest;
import com.dws.challenge.exception.DuplicateAccountIdException;

public interface AccountsRepository {

  void createAccount(Account account) throws DuplicateAccountIdException;

  Account getAccount(String accountId);

  Boolean debit(final MoneyTransferRequest transfer);
  Boolean credit(final MoneyTransferRequest transfer);

  void clearAccounts();

}
