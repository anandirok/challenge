package com.dws.challenge.exception;

import com.dws.challenge.domain.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MoneyTransferGlobalException {

    @ExceptionHandler(value = DuplicateAccountIdException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handlerException(DuplicateAccountIdException ex) {
        return ErrorResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .status(HttpStatus.BAD_REQUEST.name())
                .message(ex.getMessage()).build();
    }

    @ExceptionHandler(value = AccountIdNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handlerException(AccountIdNotFoundException ex) {
        return ErrorResponse.builder()
                .code(HttpStatus.NOT_FOUND.value())
                .status(HttpStatus.NOT_FOUND.name())
                .message(ex.getMessage()).build();
    }

    @ExceptionHandler(value = InsufficientAmountInAccountException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handlerException(InsufficientAmountInAccountException ex) {
        return ErrorResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .status(HttpStatus.BAD_REQUEST.name())
                .message(ex.getMessage()).build();
    }

    @ExceptionHandler(value = TransactionIdNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handlerException(TransactionIdNotFoundException ex) {
        return ErrorResponse.builder()
                .code(HttpStatus.NOT_FOUND.value())
                .status(HttpStatus.NOT_FOUND.name())
                .message(ex.getMessage()).build();
    }

    @ExceptionHandler(value = BadRequestExceptionClass.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handlerException(BadRequestExceptionClass ex) {
        return ErrorResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .status(HttpStatus.BAD_REQUEST.name())
                .message(ex.getMessage()).build();
    }
}
