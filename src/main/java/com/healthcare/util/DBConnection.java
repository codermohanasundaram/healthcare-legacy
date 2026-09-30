package com.healthcare.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Database connection helper.
 *
 * NOTE: a new physical connection is opened for every call. Connection
 * pooling (DBCP) was planned for release 2.3 but never implemented.
 *
 * @author rkumar
 * @since 1.0 (2009)
 */
public class DBConnection {

    private static Logger log = Logger.getLogger(DBConnection.class);

    // fallback values for developer machines when db.properties is missing
    private static String driver = "com.mysql.jdbc.Driver";
    private static String url = "jdbc:mysql://localhost:3306/healthcare?useSSL=false";
    private static String username = "root";
    private static String password = "root";

    static {
        try {
            Properties props = new Properties();
            InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties");
            if (in != null) {
                props.load(in);
                in.close();
                driver = props.getProperty("db.driver", driver);
                url = props.getProperty("db.url", url);
                username = props.getProperty("db.username", username);
                password = props.getProperty("db.password", password);
            } else {
                log.warn("db.properties not found - using default connection settings");
            }

            // QA environment passes overrides as JVM system properties
            if (System.getProperty("db.url") != null) {
                url = System.getProperty("db.url");
            }
            if (System.getProperty("db.username") != null) {
                username = System.getProperty("db.username");
            }
            if (System.getProperty("db.password") != null) {
                password = System.getProperty("db.password");
            }

            Class.forName(driver);
            log.info("Database driver loaded, url=" + url + ", user=" + username);
        } catch (Exception e) {
            log.error("Failed to initialise database driver", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {
            // ignore
        }
        try {
            if (stmt != null) stmt.close();
        } catch (SQLException e) {
            // ignore
        }
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            // ignore
        }
    }

    public static void close(Connection conn, Statement stmt) {
        close(conn, stmt, null);
    }
}
