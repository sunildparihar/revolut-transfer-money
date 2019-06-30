package com.revolut.core.fundstransfer.persist.conn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionHelper {

    private static final String CONNECTION_URL_KEY = "db.connection.url";
    private static final String DB_USER_KEY = "db.user";
    private static final String DB_PASSWORD_KEY = "db.password";

    private static final ConnectionConfiguration connectionConfig = new ConnectionConfiguration();

    public static Connection createNewConnection() throws SQLException {
        return DriverManager.getConnection(
                connectionConfig.getConfigValue(CONNECTION_URL_KEY),
                connectionConfig.getConfigValue(DB_USER_KEY),
                connectionConfig.getConfigValue(DB_PASSWORD_KEY)
        );
    }

}
