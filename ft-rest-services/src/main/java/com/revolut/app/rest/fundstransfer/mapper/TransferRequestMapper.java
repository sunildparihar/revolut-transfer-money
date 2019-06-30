package com.revolut.app.rest.fundstransfer.mapper;

import com.revolut.app.rest.fundstransfer.model.TransferRequest;
import com.revolut.sdk.fundstransfer.model.TransferRequestVO;

public class TransferRequestMapper {

    public static TransferRequestVO convertIntoVO(TransferRequest transferRequest){
        TransferRequestVO transferRequestVO = new TransferRequestVO();
        transferRequestVO.setSourceAccountId(transferRequest.getSourceAccountId());
        transferRequestVO.setDestinationAccountId(transferRequest.getDestinationAccountId());
        transferRequestVO.setTransferAmount(transferRequest.getTransferAmount());
        return transferRequestVO;
    }
}
