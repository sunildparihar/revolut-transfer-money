package com.revolut.core.fundstransfer.service.locate;

import com.revolut.sdk.fundstransfer.services.AccountService;

/**
 * Service Locator to locate the actual implementation class object based service interface class type
 * @param <T>
 */
public interface ServiceLocator<T> {

    T locate() throws Exception;

    static <T> ServiceLocator<T> getLocator(Class<T> type) {
        String className = type.getCanonicalName();
        if (AccountService.class.getCanonicalName().equals(className)) {
            return (ServiceLocator<T>) new AccountServiceLocator();
        } else {
            return new DefaultServiceLocator(type);
        }

    }
}
