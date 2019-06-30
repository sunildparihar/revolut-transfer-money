package com.revolut.core.fundstransfer.persist.to;

import java.math.BigDecimal;

public class BankAccountTO {

    private Long bankAccountId;

    private Long accountNumber;

    private String accountName;

    private BigDecimal balance;

    public BankAccountTO() {
    }

    public BankAccountTO(Long bankAccountId, Long accountNumber, String accountName, BigDecimal balance) {
        this.bankAccountId = bankAccountId;
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.balance = balance;
    }

    public Long getBankAccountId() {
        return bankAccountId;
    }

    public void setBankAccountId(Long bankAccountId) {
        this.bankAccountId = bankAccountId;
    }

    public Long getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(Long accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
