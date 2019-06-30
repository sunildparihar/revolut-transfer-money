package com.revolut.core.fundstransfer.service.locate;

public class ServiceLocator {

    /*private static final AccountService accountService = new AccountServiceImpl();
    private static final FundsTransferService fundsTransferService = new FundsTransferServiceImpl();

    private static ConcurrentHashMap<Class, String> serviceTypeToImplMap = new ConcurrentHashMap<>();

    static {

    }


    public static <T> T find(Class<T> type) {
        String className = type.getCanonicalName();
        if(AccountService.class.getCanonicalName().equals(className)) {
            return (T) accountService;
        } else if (FundsTransferService.class.getCanonicalName().equals(className)) {
            return (T) fundsTransferService;
        }
        throw new RuntimeException("Service not found");
    }*/
}
