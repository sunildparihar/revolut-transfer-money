package com.revolut.sdk.fundstransfer;

import com.revolut.sdk.fundstransfer.services.AccountService;

public interface ServiceFactory<T> {

    T getImplementation() throws Exception;

    static Object getDefaultImplementation(Class serviceClassType) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String basePackageName = "com.revolut.core.fundstransfer.impl";
        String childClassName = basePackageName + "." + serviceClassType.getSimpleName() + "Impl";
        Class classObj = Class.forName(childClassName);
        return classObj.newInstance();
    }

    public static <T> ServiceFactory<T> getFactory(Class<T> type) {
        String className = type.getCanonicalName();
        if(AccountService.class.getCanonicalName().equals(className)) {
            return (ServiceFactory<T>) new AccountServiceFactory();
        } else  {
            return () -> { return (T) ServiceFactory.getDefaultImplementation(type);};
        }

    }
}
