package com.revolut.sdk.fundstransfer.services;

import com.revolut.sdk.fundstransfer.exception.InternalCoreException;
import com.revolut.sdk.fundstransfer.exception.ValidationException;
import com.revolut.sdk.fundstransfer.model.TransferRequestVO;

import javax.transaction.Transactional;

import static javax.transaction.Transactional.TxType;

@Transactional(TxType.REQUIRES_NEW)
public interface FundsTransferService {

    /**
     * Transfer funds between two accounts.
     * @param transferRequest
     * @return
     * @throws com.revolut.sdk.fundstransfer.exception.InternalCoreException
     */
    void transferFunds(TransferRequestVO transferRequest) throws InternalCoreException, ValidationException;
}
