package com.revolut.core.fundstransfer.gateway;

import com.revolut.core.fundstransfer.transaction.manage.RevolutTransactionManager;
import com.revolut.sdk.fundstransfer.exception.ServiceException;

import javax.transaction.Transactional;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransactionsUtil {

    private static Logger logger = Logger.getLogger(TransactionsUtil.class.getName());

    private static final RevolutTransactionManager transactionManager = RevolutTransactionManager.getInstance();
    private static ThreadLocal<Boolean> isTransactionStartedThreadLocal = new ThreadLocal<>();

    public static void startTransaction(Method methodToInvoke, Class serviceClassType) throws ServiceException {
        // start a transaction based on propagation level
        Transactional.TxType txType = findTransactionPropagationLevel(methodToInvoke, serviceClassType);
        try {
            transactionManager.startTransaction(txType);
            isTransactionStartedThreadLocal.set(true);
        } catch (Exception e) {
            isTransactionStartedThreadLocal.set(false);
            logger.log(Level.SEVERE, "Error while starting transaction:" + e.getMessage());
            throw new ServiceException(e.getMessage());
        }
    }

    public static void endTransaction(boolean isSuccessful) throws ServiceException {
        try {
            Boolean wasTxStarted = isTransactionStartedThreadLocal.get();
            if (wasTxStarted != null && wasTxStarted) {
                transactionManager.endTransactionIfNeeded(isSuccessful);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error while ending transaction:" + e.getMessage());
            throw new ServiceException(e.getMessage());
        } finally {
            isTransactionStartedThreadLocal.remove();
        }
    }

    private static Transactional.TxType findTransactionPropagationLevel(Method methodToInvoke, Class serviceClassType) {

        Transactional trx = methodToInvoke.getDeclaredAnnotation(Transactional.class);
        if (trx == null) {
            trx = (Transactional) serviceClassType.getDeclaredAnnotation(Transactional.class);
        }
        Transactional.TxType txType = Transactional.TxType.REQUIRED;
        if (trx != null) {
            txType = trx.value();
        }

        return txType;
    }
}
