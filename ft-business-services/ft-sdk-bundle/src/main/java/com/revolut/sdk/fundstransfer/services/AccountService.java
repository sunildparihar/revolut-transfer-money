package com.revolut.sdk.fundstransfer.services;

import com.revolut.sdk.fundstransfer.exception.InternalCoreException;
import com.revolut.sdk.fundstransfer.exception.ValidationException;
import com.revolut.sdk.fundstransfer.model.AccountVO;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;

import static javax.transaction.Transactional.TxType;

public interface AccountService {

    /**
     * Find all accounts
     *
     * @return
     * @throws InternalCoreException
     */
    @Transactional(TxType.REQUIRED)
    List<AccountVO> getAllAccounts() throws InternalCoreException;

    /**
     * Find by account id
     *
     * @param accountId
     * @return
     * @throws InternalCoreException
     */
    @Transactional(TxType.REQUIRED)
    AccountVO getAccount(Long accountId) throws InternalCoreException;

    /**
     * Added for testing of @ com.revolut.core.fundstransfer.gateway.ServicesGateway} for overloaded methods, should be ignored.
     * Find by account id
     *
     * @param accountId
     * @return
     * @throws InternalCoreException
     */
    @Transactional(TxType.REQUIRED)
    AccountVO getAccount(Long accountId, boolean isTest) throws InternalCoreException;

    /**
     * Withdraw Funds
     *
     * @param accountId
     * @param amount
     * @return
     * @throws InternalCoreException
     */
    @Transactional(TxType.REQUIRES_NEW)
    void withdrawFromAccount(Long accountId, BigDecimal amount) throws InternalCoreException, ValidationException;

    /**
     * Deposit Funds
     *
     * @param accountId
     * @param amount
     * @return
     * @throws InternalCoreException
     */
    @Transactional(TxType.REQUIRES_NEW)
    void depositToAccount(Long accountId, BigDecimal amount) throws InternalCoreException, ValidationException;
}
