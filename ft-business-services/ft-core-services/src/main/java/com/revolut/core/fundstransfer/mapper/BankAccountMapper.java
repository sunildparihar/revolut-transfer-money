package com.revolut.core.fundstransfer.mapper;

import com.revolut.core.fundstransfer.persist.to.BankAccountTO;
import com.revolut.sdk.fundstransfer.model.AccountVO;

public class BankAccountMapper {

    public static AccountVO convertFromTO(BankAccountTO bankAccountTO){
        AccountVO account = new AccountVO();
        account.setAccountId(bankAccountTO.getBankAccountId());
        account.setAccountName(bankAccountTO.getAccountName());
        account.setAccountNumber(bankAccountTO.getAccountNumber());
        account.setBalance(bankAccountTO.getBalance());
        return account;
    }

}
