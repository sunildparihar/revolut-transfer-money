package com.revolut.core.fundstransfer.persist.exception;

public class DataException extends Exception {

	public DataException(String message) {
		super(message);
	}

	public DataException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
