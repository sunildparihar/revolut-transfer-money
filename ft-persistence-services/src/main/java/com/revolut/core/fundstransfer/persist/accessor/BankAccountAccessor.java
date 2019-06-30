package com.revolut.core.fundstransfer.persist.accessor;

import com.revolut.core.fundstransfer.persist.exception.DataException;
import com.revolut.core.fundstransfer.persist.to.BankAccountTO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

public interface BankAccountAccessor {
    List<BankAccountTO> getAllAccounts(Connection connection) throws DataException;
    BankAccountTO getAccount(Connection connection, long accountId) throws DataException;
    int deposit(Connection connection, long accountId, BigDecimal amount) throws DataException;
    int withdraw(Connection connection, long accountId, BigDecimal amount) throws DataException;
}
