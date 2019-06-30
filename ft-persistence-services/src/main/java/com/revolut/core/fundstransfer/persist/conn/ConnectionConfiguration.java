package com.revolut.core.fundstransfer.persist.conn;

import org.apache.commons.dbutils.DbUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionConfiguration {

    private static Logger log = Logger.getLogger(ConnectionConfiguration.class.getName());

    private Properties dbProperties = new Properties();

    public ConnectionConfiguration() {
        reloadConfig();
    }

    public void reloadConfig() {
        String fileName = "db.properties";
        try {
            InputStream inputStream = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(fileName);
            dbProperties.load(inputStream);
        } catch (IOException e) {
            log.log(Level.SEVERE, "error while loading db config:", e);
        }

        DbUtils.loadDriver(dbProperties.getProperty("db.driver"));
    }

    public String getConfigValue(String key) {
        return dbProperties.getProperty(key);
    }
}
