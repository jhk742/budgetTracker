package Connectors;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.IOException;
import java.io.FileInputStream;

public class ConnectionProvider {

    private static boolean useLocal = false;

    private static String local_jdbcURL;
    private static String local_username;
    private static String local_password;

    private static Connection connection;

    public static void MySQLConfigReaderForLocal() {
        Properties properties = new Properties();
        try {
            FileInputStream input = new FileInputStream(System.getProperty("user.dir") + "/.my.cnf");
            properties.load(input);
            local_username = String.valueOf(properties.get("user"));
            local_password = String.valueOf(properties.get("password"));
            local_jdbcURL = String.valueOf(properties.get("jdbcUrl"));
            useLocal = true;
        } catch (IOException e) {
            System.out.println("Could not locate .my.cnf. Using AWSRDS.");
        }
    }

    public static Connection getCon() throws SQLException {
        MySQLConfigReaderForLocal();
        try {
            if (useLocal) {
                connection = DriverManager.getConnection(local_jdbcURL, local_username, local_password);
                System.out.println("USING on-PREMISE");
            }
            if (!useLocal) {
//                connection = DriverManager.getConnection(AWSRDS_jdbcUrl, AWSRDS_jdbcUrl_username, AWSRDS_jdbcUrl_password);
                System.out.println("USING AWS-RDS");
            }
            return connection;
        } catch(SQLException e) {
            throw e;
        }
    }
}
