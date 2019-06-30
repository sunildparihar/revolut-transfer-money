package com.revolut.sdk.fundstransfer;

import com.revolut.sdk.fundstransfer.services.AccountService;

public class AccountServiceFactory implements ServiceFactory<AccountService>{

    @Override
    public AccountService getImplementation() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class implClass = Class.forName("com.revolut.core.fundstransfer.impl.AccountServiceImpl");
        return (AccountService) implClass.newInstance();
    }
}
