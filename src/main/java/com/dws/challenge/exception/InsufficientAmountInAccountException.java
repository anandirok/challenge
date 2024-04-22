package com.dws.challenge.exception;

public class InsufficientAmountInAccountException extends RuntimeException {

  public InsufficientAmountInAccountException(String message) {
    super(message);
  }
}
