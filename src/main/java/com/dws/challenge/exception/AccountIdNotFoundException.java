package com.dws.challenge.exception;

public class AccountIdNotFoundException extends RuntimeException {

  public AccountIdNotFoundException(String message) {
    super(message);
  }
}
