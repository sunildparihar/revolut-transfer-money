package com.revolut.sdk.fundstransfer.exception;

public class ServiceException extends Exception {

    private static final long serialVersionUID = 1401841084014L;

    private String reasonCode;

    public ServiceException(String msg) {
        super(msg);
    }

    public ServiceException(String msg, String reasonCode) {
        super(msg);
        this.reasonCode = reasonCode;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }
}
