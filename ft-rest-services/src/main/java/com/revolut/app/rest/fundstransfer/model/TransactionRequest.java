package com.revolut.app.rest.fundstransfer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class TransactionRequest {

    @JsonProperty(required = true)
    private long accountId;

    @JsonProperty(required = true)
    private BigDecimal transactionAmount;

    public TransactionRequest() {
    }

    public TransactionRequest(Long accountId, BigDecimal transactionAmount) {
        this.accountId = accountId;
        this.transactionAmount = transactionAmount;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }
}
