package com.dws.challenge.exception;

public class TransactionIdNotFoundException extends RuntimeException {

  public TransactionIdNotFoundException(String message) {
    super(message);
  }
}
