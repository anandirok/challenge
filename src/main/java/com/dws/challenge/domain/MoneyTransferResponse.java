package com.dws.challenge.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.ZonedDateTime;

@Data
@Builder
@Getter
public class MoneyTransferResponse {
    private String transactionId;
    private String status;
    private String message;

    public MoneyTransferResponse(String transactionId, String status, String message) {
        this.transactionId = transactionId;
        this.status = status;
        this.message = message;
    }
}
