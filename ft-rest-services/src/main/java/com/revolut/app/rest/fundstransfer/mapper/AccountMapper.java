package com.revolut.app.rest.fundstransfer.mapper;

import com.revolut.app.rest.fundstransfer.model.Account;
import com.revolut.sdk.fundstransfer.model.AccountVO;

public class AccountMapper {

    public static Account convertFromVO(AccountVO accountVO) {
        Account account = new Account();
        account.setAccountId(accountVO.getAccountId());
        account.setAccountName(accountVO.getAccountName());
        account.setAccountNumber(accountVO.getAccountNumber());
        account.setBalance(accountVO.getBalance());
        return account;
    }
}
