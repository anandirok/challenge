package com.dws.challenge.web;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.MoneyTransactionDetails;
import com.dws.challenge.domain.MoneyTransferRequest;
import com.dws.challenge.domain.MoneyTransferResponse;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.service.AccountsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

    private final AccountsService accountsService;

    @Autowired
    public AccountsController(AccountsService accountsService) {
        this.accountsService = accountsService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createAccount(@Valid @RequestBody Account account) {
        log.info("Creating account {}", account);

        try {
            this.accountsService.createAccount(account);
        } catch (DuplicateAccountIdException daie) {
            throw daie;
            //return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping(path = "/{accountId}")
    public Account getAccount(@PathVariable String accountId) {
        log.info("Retrieving account for id {}", accountId);
        return this.accountsService.getAccount(accountId);
    }


    // Money transfer rest endpoint
    // Method Name : amountTransfer
    // Request Param : MoneyTransferRequest
    // Reponse Body : MoneyTransferResponse
    @PostMapping(path = "/amount-transfer", consumes = MediaType.APPLICATION_JSON_VALUE)
    public MoneyTransferResponse amountTransfer(@Valid @RequestBody MoneyTransferRequest moneyTransferRequest) {
        log.info("Start the amount transfer from sender account: {} to reciver account {} ::: transfer amount", moneyTransferRequest.getAccountFrom()
                , moneyTransferRequest.getAccountTo(), moneyTransferRequest.getTransferAmount());
        return accountsService.fundTransfer(moneyTransferRequest);
    }

    @GetMapping(path = "/transaction/{transactionId}")
    public MoneyTransactionDetails getTransactionDetails(@PathVariable String transactionId) {
        log.info("Retrieving transaction details for transaction id {}", transactionId);
        return this.accountsService.getTransactionDetails(transactionId);
    }

}
