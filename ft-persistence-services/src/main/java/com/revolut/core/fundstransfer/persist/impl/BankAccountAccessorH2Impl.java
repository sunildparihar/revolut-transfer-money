package com.revolut.core.fundstransfer.persist.impl;

import com.revolut.core.fundstransfer.persist.accessor.BankAccountAccessor;
import com.revolut.core.fundstransfer.persist.exception.DataException;
import com.revolut.core.fundstransfer.persist.to.BankAccountTO;
import org.apache.commons.dbutils.DbUtils;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BankAccountAccessorH2Impl implements BankAccountAccessor {

	private static Logger log = Logger.getLogger(BankAccountAccessorH2Impl.class.getName());

    private static final String SELECT_FOR_UPDATE = "SELECT * FROM BANK_ACCOUNT WHERE BANK_ACCOUNT_ID = ? FOR UPDATE";

    private static final String SELECT_ALL = "SELECT * FROM BANK_ACCOUNT";
	private static final String SELECT_BY_ID = "SELECT * FROM BANK_ACCOUNT WHERE BANK_ACCOUNT_ID = ?";
	private static final String UPDATE_BALANCE = "UPDATE BANK_ACCOUNT SET BALANCE = ? WHERE BANK_ACCOUNT_ID = ?";

	public List<BankAccountTO> getAllAccounts(Connection connection) throws DataException {
		Statement statement = null;
		ResultSet rs = null;
		List<BankAccountTO> bankAccountList = new ArrayList<>();
		try {
			statement = connection.createStatement();
			rs = statement.executeQuery(SELECT_ALL);
			while (rs.next()) {
                BankAccountTO account = createBankAccountFromResultSet(rs);
				bankAccountList.add(account);
			}
			return bankAccountList;
		} catch (SQLException e) {
            log.log(Level.SEVERE, "Error reading accounts", e);
			throw new DataException("Error reading accounts", e);
		} finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(statement);
		}
	}

    @Override
    public BankAccountTO getAccount(Connection connection, long accountId) throws DataException {
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        BankAccountTO account = null;
        try {
            preparedStatement = connection.prepareStatement(SELECT_BY_ID);
            preparedStatement.setLong(1, accountId);
            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                account = createBankAccountFromResultSet(rs);
            }
            return account;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error fetching account:", e);
            throw new DataException("Error fetching account:", e);
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(preparedStatement);
        }
    }

    @Override
    public int deposit(Connection connection, long accountId, BigDecimal amount) throws DataException {
        int updateCount = 0;
        PreparedStatement lockStatement = null;
        PreparedStatement updateStatement = null;
        ResultSet rs = null;
        BankAccountTO account = null;
        try {
            lockStatement = connection.prepareStatement(SELECT_FOR_UPDATE);
            lockStatement.setLong(1, accountId);
            log.log(Level.INFO, "###trying to lock deposit account:" + accountId);
            rs = lockStatement.executeQuery();
            if (rs.next()) {
                account = createBankAccountFromResultSet(rs);
            }

            if (Objects.isNull(account)) {
                log.log(Level.SEVERE, "deposit() failed to lock account:" + accountId);
                throw new DataException("deposit() failed to lock account : " + accountId);
            }

            BigDecimal finalBalance = account.getBalance().add(amount);

            updateStatement = connection.prepareStatement(UPDATE_BALANCE);
            updateStatement.setBigDecimal(1, finalBalance);
            updateStatement.setLong(2, accountId);
            updateCount = updateStatement.executeUpdate();
            return updateCount;
        } catch (Exception e) {
            throw new DataException("deposit() Failed", e);
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(lockStatement);
            DbUtils.closeQuietly(updateStatement);
        }
    }

    @Override
    public int withdraw(Connection connection, long accountId, BigDecimal amount) throws DataException {
        int updateCount = 0;
        PreparedStatement lockStatement = null;
        PreparedStatement updateStatement = null;
        ResultSet rs = null;
        BankAccountTO account = null;
        try {
            lockStatement = connection.prepareStatement(SELECT_FOR_UPDATE);
            lockStatement.setLong(1, accountId);
            log.log(Level.INFO, "###trying to lock withdrawal account:" + accountId);
            rs = lockStatement.executeQuery();
            if (rs.next()) {
                account = createBankAccountFromResultSet(rs);
            }

            if (Objects.isNull(account)) {
                log.log(Level.SEVERE, "withdraw() failed to lock account:" + accountId);
                throw new DataException("withdraw() failed to lock account: " + accountId);
            }

            BigDecimal finalBalance = account.getBalance().subtract(amount);
            if (finalBalance.longValue() < 0) {
                throw new DataException("No sufficient balance for account:" + accountId);
            }

            updateStatement = connection.prepareStatement(UPDATE_BALANCE);
            updateStatement.setBigDecimal(1, finalBalance);
            updateStatement.setLong(2, accountId);
            updateCount = updateStatement.executeUpdate();
            return updateCount;
        } catch (Exception e) {
            throw new DataException("withdraw() Failed", e);
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(lockStatement);
            DbUtils.closeQuietly(updateStatement);
        }
    }

    private BankAccountTO createBankAccountFromResultSet(ResultSet rs) throws SQLException {
        return new BankAccountTO(rs.getLong("BANK_ACCOUNT_ID"), rs.getLong("ACCOUNT_NUMBER"),
                rs.getString("ACCOUNT_NAME"), rs.getBigDecimal("BALANCE"));
    }

}
