package com.revolut.fundstransfer.tools;

import com.revolut.core.fundstransfer.persist.conn.ConnectionHelper;
import org.apache.commons.dbutils.DbUtils;
import org.h2.tools.RunScript;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Schema generator to create an in memory H2 DB
 */
public class H2SchemaGenerator {

    private static Logger log = Logger.getLogger(H2SchemaGenerator.class.getName());

    public static void generate() {
        log.log(Level.INFO,"Preparing demo h2 db...");
        Connection connection = null;
        try(InputStream testSqlStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("prepareh2.sql");) {
            connection = ConnectionHelper.createNewConnection();
            RunScript.execute(connection, new InputStreamReader(testSqlStream));
        } catch (SQLException | IOException e) {
            log.log(Level.SEVERE, "Error while creating demo h2 db:", e);
            throw new RuntimeException(e);
        } finally {
            DbUtils.closeQuietly(connection);
        }
    }
}
