package com.revolut.core.fundstransfer.conn.manage;

import com.revolut.core.fundstransfer.persist.conn.ConnectionHelper;
import org.apache.commons.dbutils.DbUtils;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * This class is supposed to use a connection pool, as of now not using any conn pool for simplicity.
 */
public class ConnectionManager {

    private static final ConnectionManager connectionManager = new ConnectionManager();

    private ConnectionManager() {
    }

    public static ConnectionManager getInstance() {
        return connectionManager;
    }

    public Connection getConnection() throws SQLException {
        //for now, always creating a new fresh connection.
        return createNewConnection();

    }

    public void release(Connection connection) {
        //for now, always directly closing the connection.
        DbUtils.closeQuietly(connection);
    }

    private Connection createNewConnection() throws SQLException {
//        return null;
        return ConnectionHelper.createNewConnection();
    }
}
