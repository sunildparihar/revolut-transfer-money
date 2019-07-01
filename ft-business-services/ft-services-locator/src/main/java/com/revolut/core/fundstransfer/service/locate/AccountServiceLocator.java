package com.revolut.core.fundstransfer.service.locate;

import com.revolut.sdk.fundstransfer.services.AccountService;

public class AccountServiceLocator implements ServiceLocator<AccountService> {

    @Override
    public AccountService locate() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class implClass = Class.forName("com.revolut.core.fundstransfer.impl.AccountServiceImpl");
        return (AccountService) implClass.newInstance();
    }
}
