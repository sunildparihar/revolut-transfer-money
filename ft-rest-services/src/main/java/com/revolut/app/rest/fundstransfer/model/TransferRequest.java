package com.revolut.app.rest.fundstransfer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class TransferRequest {

	@JsonProperty(required = true)
	private BigDecimal transferAmount;

	@JsonProperty(required = true)
	private long sourceAccountId;

	@JsonProperty(required = true)
	private long destinationAccountId;

	public TransferRequest() {
	}

	public TransferRequest(BigDecimal transferAmount, long sourceAccountId, long destinationAccountId) {
	    this.destinationAccountId = destinationAccountId;
	    this.sourceAccountId = sourceAccountId;
		this.transferAmount = transferAmount;
	}

    public BigDecimal getTransferAmount() {
        return transferAmount;
    }

    public void setTransferAmount(BigDecimal transferAmount) {
        this.transferAmount = transferAmount;
    }

    public long getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(long sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public long getDestinationAccountId() {
        return destinationAccountId;
    }

    public void setDestinationAccountId(long destinationAccountId) {
        this.destinationAccountId = destinationAccountId;
    }
}
