package com.revolut.core.fundstransfer.service.locate;

/**
 * DefaultServiceLocator looks for the implementation class with default logic.
 * For any service, If implementation class can not be located using default logic then a custom locator class
 * needs to written overriding ServiceLocator interface. For example, for AccountService there is a custom locator @{@link AccountServiceLocator}
 *
 * The Default logic for locating implementation class goes below:
 * Look for the class inside the base package com.revolut.core.fundstransfer.impl and that ends with <ServiceInterfaceName>Impl.
 * e.g. for FundsTransferService, the implementation class will be com.revolut.core.fundstransfer.impl.FundsTransferServiceImpl
 */
public class DefaultServiceLocator implements ServiceLocator {

    private Class serviceClassType;

    DefaultServiceLocator(Class serviceClassType) {
        this.serviceClassType = serviceClassType;
    }

    @Override
    public Object locate() throws Exception {
        String basePackageName = "com.revolut.core.fundstransfer.impl";
        String childClassName = basePackageName + "." + serviceClassType.getSimpleName() + "Impl";
        Class classObj = Class.forName(childClassName);
        return classObj.newInstance();
    }
}
