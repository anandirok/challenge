package com.dws.challenge.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.ZonedDateTime;

@Data
@Builder
@Getter
public class MoneyTransferResponse {
    private String message;

    public MoneyTransferResponse(String message) {
        this.message = message;
    }
}
