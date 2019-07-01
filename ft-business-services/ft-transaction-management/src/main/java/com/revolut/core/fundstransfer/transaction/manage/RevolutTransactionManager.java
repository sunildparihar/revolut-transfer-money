package com.revolut.core.fundstransfer.transaction.manage;

import com.revolut.core.fundstransfer.conn.manage.ConnectionManager;

import javax.transaction.NotSupportedException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.transaction.Transactional.TxType;

public class RevolutTransactionManager {

    private static Logger logger = Logger.getLogger(RevolutTransactionManager.class.getName());

    private static final RevolutTransactionManager transactionManager = new RevolutTransactionManager();

    private ThreadLocal<Deque<Connection>> currentThreadConnectionStack = new ThreadLocal<>();
    private ThreadLocal<Deque<Boolean>> currentThreadEndTxFlagStack = new ThreadLocal<>();

    public static RevolutTransactionManager getInstance() {
        return transactionManager;
    }

    /**
     * Starts a new Transaction if
     * If Transaction Propagation is REQUIRES_NEW OR
     * If Transaction Propagation is REQUIRED and there is no parent transaction found.
     *
     * As of now only REQUIRED & REQUIRES_NEW are supported.
     *
     * This transaction manager supports nested transactions as well.
     *
     * @param transactionPropagation
     * @return
     * @throws SQLException
     */

    public Connection startTransaction(TxType transactionPropagation) throws Exception {

        if (!isValidTransactionType(transactionPropagation)) {
            throw new NotSupportedException("Trx Type Not Supported: "+transactionPropagation.name());
        }

        Deque<Connection> connectionStack = currentThreadConnectionStack.get();
        if (Objects.isNull(connectionStack)) {
            connectionStack = new ArrayDeque<>();
            currentThreadConnectionStack.set(connectionStack);
        }

        boolean isNewConnection = false;
        Connection connection = null;
        boolean isPushedToConnStack = false;
        boolean isFailure = true;

        try {

            if(isNewTransactionNeeded(transactionPropagation, connectionStack)) {
                //get a new connection from pool
                connection = ConnectionManager.getInstance().getConnection();
                isNewConnection = true;
                connection.setAutoCommit(false);
                //push the new connection to stack
                connectionStack.push(connection);
                isPushedToConnStack = true;
            } else {
                //retrieve the existing connection from stack i.e. use the connection from parent transaction itself
                connection = connectionStack.peek();
            }

            Deque<Boolean> endTransactionFlagStack = currentThreadEndTxFlagStack.get();
            if (Objects.isNull(endTransactionFlagStack)) {
                endTransactionFlagStack = new ArrayDeque<>();
                currentThreadEndTxFlagStack.set(endTransactionFlagStack);
            }

            // a transaction needs to be closed only if was newly created by current active transaction otherwise it needs to be passThrough back to the parent transaction
            endTransactionFlagStack.push(isNewConnection);
            isFailure = false;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while starting transaction:" + e);
            throw e;
        } finally {
            if (isFailure) {
                //any error occurred, make sure stacks are not in inconsistent state to avoid any connection leak.
                if (isPushedToConnStack) {
                    connectionStack.pop();
                }
                //make sure to return the newly created connection back to the pool
                if (isNewConnection) {
                    ConnectionManager.getInstance().release(connection);
                }
            }
        }

        return connection;
    }

    /**
     * Closes the current transaction is there was one newly created otherwise is propagated back to the parent transaction
     *
     * @param isSuccessful
     * @throws SQLException
     */
    public void endTransactionIfNeeded(boolean isSuccessful) throws SQLException {

        Deque<Connection> connectionStack = currentThreadConnectionStack.get();
        Deque<Boolean> endTransactionFlagStack = currentThreadEndTxFlagStack.get();

        if(isTransactionNotStarted(connectionStack, endTransactionFlagStack)) {
            throw new RuntimeException("Error: A transaction is not started, please start one");
        }

        boolean shouldCloseTransaction = endTransactionFlagStack.pop();
        if (shouldCloseTransaction) {
            Connection currentConnection = connectionStack.pop();
            if (isSuccessful) {
                try {
                    currentConnection.commit();
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Error while committing transaction:" + e);
                    throw e;
                }
            } else {
                try {
                    currentConnection.rollback();
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Error while rollback transaction:" + e);
                    throw e;
                }
            }
            // close the transaction i.e. return the connection back to the pool
            ConnectionManager.getInstance().release(currentConnection);
        } else {
            //a new transaction was not created by current active transaction, so no action is needed. It needs to be propagated back to the parent transaction.
            //It's the responsibility of parent transaction to close the transaction with commit or rollback.
        }
    }

    public Connection getConnectionFromCurrentTransaction() {
        return currentThreadConnectionStack.get().peek();
    }

    private boolean isNewTransactionNeeded(TxType transactionPropagation, Deque<Connection> connectionStack) {
        return transactionPropagation == TxType.REQUIRES_NEW
                || connectionStack.isEmpty()
                || Objects.isNull(connectionStack.peek());
    }

    private boolean isTransactionNotStarted(Deque<Connection> connectionStack, Deque<Boolean> endTransactionFlagStack) {
        return Objects.isNull(connectionStack)
                || connectionStack.isEmpty()
                || Objects.isNull(endTransactionFlagStack)
                || endTransactionFlagStack.isEmpty();
    }

    private boolean isValidTransactionType(TxType transactionPropagation) {
        return transactionPropagation == TxType.REQUIRES_NEW
                || transactionPropagation == TxType.REQUIRED;
    }

}
