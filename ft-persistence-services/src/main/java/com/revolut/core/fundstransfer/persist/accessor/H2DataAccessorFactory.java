package com.revolut.core.fundstransfer.persist.accessor;

import com.revolut.core.fundstransfer.persist.impl.BankAccountAccessorH2Impl;

public class H2DataAccessorFactory extends DataAccessorFactory {

	private final BankAccountAccessor bankAccountAccessor = new BankAccountAccessorH2Impl();

	public BankAccountAccessor getBankAccountAccessor() {
		return bankAccountAccessor;
	}
}
