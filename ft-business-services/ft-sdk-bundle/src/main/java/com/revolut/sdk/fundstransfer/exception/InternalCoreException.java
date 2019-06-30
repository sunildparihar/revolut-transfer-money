package com.revolut.sdk.fundstransfer.exception;

public class InternalCoreException extends ServiceException {

    private static final long serialVersionUID = 404644231113131314L;

    public InternalCoreException(String msg) {
        super(msg);
    }

    public InternalCoreException(String msg, String reasonCode) {
        super(msg, reasonCode);
    }
}
