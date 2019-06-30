package com.revolut.core.fundstransfer.gateway;

import com.revolut.sdk.fundstransfer.ServiceFactory;
import com.revolut.sdk.fundstransfer.exception.ServiceException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServicesGateway {

    private static Logger logger = Logger.getLogger(ServicesGateway.class.getName());
    private static final ServicesGateway servicesGateway = new ServicesGateway();

    private ServicesGateway() {
    }

    public static ServicesGateway getServicesGateway() {
        return servicesGateway;
    }

    /**
     * @param serviceClassType class type of the service to be invoked
     * @param methodName       name of the method to be executed of the service.
     * @param arguments        method arguments to be passed in the same order as declared in the service method
     * @return The result of service method invocation
     * @throws ServiceException
     */
    public Object passThrough(Class serviceClassType, String methodName, Object... arguments) throws ServiceException {

        Object result;
        boolean isOperationSuccessful = false;
        try {
            // first find the matching method to be invoked using reflection, once found methods are cached to avoid calling reflection second time
            Method matchingMethod = ReflectionUtils.findMatchingMethodUsingReflection(serviceClassType, methodName, arguments);

            //do common things before invoking service method i.e. tx management, logging, security checks
            doCommonBeforeService(matchingMethod, serviceClassType);

            //now it's time to invoke the actual service implementation
            result = invokeService(matchingMethod, serviceClassType, arguments);

            isOperationSuccessful = true;

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        } finally {
            //do common things after invoking service method i.e. tx management, logging, any resource release
            doCommonAfterService(isOperationSuccessful);
        }

        return result;
    }

    private Object invokeService(Method method, Class serviceClassType, Object... arguments) throws ServiceException {

        try {
            //locate the service using service factory and invoke the actual method implementation
            Object implementationObj = ServiceFactory.getFactory(serviceClassType).getImplementation();
            return method.invoke(implementationObj, arguments);

        } catch (InvocationTargetException e) {
            Throwable tw = e.getTargetException();
            logger.log(Level.SEVERE, "Error while invoking service:" + tw.getMessage());
            if (tw instanceof ServiceException) {
                throw (ServiceException) tw;
            }
            throw new ServiceException(tw.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while invoking service:" + e.getMessage());
            throw new ServiceException(e.getMessage());
        }
    }

    /*
        As of now only transaction management is performed
     */
    private void doCommonBeforeService(Method methodToInvoke, Class serviceClassType) throws ServiceException {
        TransactionsUtil.startTransaction(methodToInvoke, serviceClassType);
    }

    /*
        As of now only transaction management is performed
     */
    private void doCommonAfterService(boolean isOperationSuccessful) throws ServiceException {
        TransactionsUtil.endTransaction(isOperationSuccessful);
    }

}
