package com.revolut.sdk.fundstransfer.exception;

public class ValidationException extends ServiceException {

    private static final long serialVersionUID = 4204824820482L;

    public ValidationException(String msg) {
        super(msg);
    }

    public ValidationException(String msg, String reasonCode) {
        super(msg, reasonCode);
    }
}
