package com.revolut.core.fundstransfer.persist.accessor;

public abstract class DataAccessorFactory {

	public static final String DATA_ACCESSOR_FACTORY_H2 = "h2";

	public abstract BankAccountAccessor getBankAccountAccessor();

	public static DataAccessorFactory getDataAccessorFactory(String factoryType) {
	    //as of now only H2DataAccessorFactory is available
		return new H2DataAccessorFactory();
	}
}