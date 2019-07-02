package com.revolut.core.fundstransfer.impl;

import com.revolut.core.fundstransfer.locks.ObjectsLockManager;
import com.revolut.core.fundstransfer.mapper.BankAccountMapper;
import com.revolut.core.fundstransfer.persist.accessor.BankAccountAccessor;
import com.revolut.core.fundstransfer.persist.accessor.DataAccessorFactory;
import com.revolut.core.fundstransfer.persist.exception.DataException;
import com.revolut.core.fundstransfer.persist.to.BankAccountTO;
import com.revolut.core.fundstransfer.transaction.manage.RevolutTransactionManager;
import com.revolut.sdk.fundstransfer.services.AccountService;
import com.revolut.sdk.fundstransfer.exception.InternalCoreException;
import com.revolut.sdk.fundstransfer.exception.ValidationException;
import com.revolut.sdk.fundstransfer.model.AccountVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AccountServiceImpl implements AccountService {

    private final DataAccessorFactory dataAccessorFactory = DataAccessorFactory.getDataAccessorFactory(DataAccessorFactory.DATA_ACCESSOR_FACTORY_H2);
    private final RevolutTransactionManager transactionManager = RevolutTransactionManager.getInstance();
    private final ObjectsLockManager lockManager = ObjectsLockManager.getInstance();

    @Override
    public List<AccountVO> getAllAccounts() throws InternalCoreException {
        try {
            return dataAccessorFactory.getBankAccountAccessor().getAllAccounts(transactionManager.getConnectionFromCurrentTransaction())
                    .stream()
                    .map(BankAccountMapper::convertFromTO)
                    .collect(Collectors.toList());
        } catch (DataException e) {
            throw new InternalCoreException(e.getMessage());
        }
    }

    @Override
    public AccountVO getAccount(Long accountId) throws InternalCoreException {
        return BankAccountMapper.convertFromTO(fetchAccount(dataAccessorFactory.getBankAccountAccessor(), accountId));
    }

    /*
        Added for testing purpose, should be ignored
     */
    @Override
    public AccountVO getAccount(Long accountId, boolean isTest) throws InternalCoreException {
        return getAccount(accountId);
    }

    @Override
    public void withdrawFromAccount(Long accountId, BigDecimal amount) throws InternalCoreException, ValidationException {

        if (Objects.isNull(amount) || amount.longValue() <= 0) {
            throw new ValidationException("Invalid withdrawal amount", "301");
        }

        BankAccountTO account = fetchAccount(dataAccessorFactory.getBankAccountAccessor(), accountId);

        if (account.getBalance().longValue() < amount.longValue()) {
            throw new ValidationException("Account doesn't have sufficient balance", "203");
        }

        int updateCount = 0;
        try {
            lockManager.lockKey(account.getBankAccountId());
            updateCount = dataAccessorFactory.getBankAccountAccessor().withdraw(
                    transactionManager.getConnectionFromCurrentTransaction(), account.getBankAccountId(), amount
            );
        } catch (DataException e) {
            throw new InternalCoreException("Withdrawal Failed:" + e.getMessage());
        } finally {
            lockManager.unlockKey(account.getBankAccountId());
        }
        if (updateCount != 1) {
            // withdrawal failed
            throw new InternalCoreException("Withdrawal Failed");
        }
    }

    @Override
    public void depositToAccount(Long accountId, BigDecimal amount) throws InternalCoreException, ValidationException {

        if (Objects.isNull(amount) || amount.longValue() <= 0) {
            throw new ValidationException("Invalid depositToAccount amount", "302");
        }

        BankAccountTO account = fetchAccount(dataAccessorFactory.getBankAccountAccessor(), accountId);

        int updateCount = 0;
        try {
            lockManager.lockKey(account.getBankAccountId());
            updateCount = dataAccessorFactory.getBankAccountAccessor().deposit(
                    transactionManager.getConnectionFromCurrentTransaction(), account.getBankAccountId(), amount
            );
        } catch (DataException e) {
            throw new InternalCoreException("Deposit Failed" + e.getMessage());
        } finally {
            lockManager.unlockKey(account.getBankAccountId());
        }
        if (updateCount != 1) {
            // deposit failed
            throw new InternalCoreException("Deposit Failed");
        }
    }

    private BankAccountTO fetchAccount(BankAccountAccessor bankAccountAccessor, Long accountId) throws InternalCoreException {
        BankAccountTO account;
        try {
            account = bankAccountAccessor.getAccount(transactionManager.getConnectionFromCurrentTransaction(), accountId);
        } catch (DataException e) {
            throw new InternalCoreException("Unable to fetch Account:" + accountId);
        }

        if (Objects.isNull(account)) {
            throw new InternalCoreException("Account doesn't Exists:" + accountId, "202");
        }

        return account;
    }
}
