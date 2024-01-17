package Connectors;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionProvider {

    private static final String local_jdbcUrl = "jdbc:mysql://localhost:3306/budgettracker?useTimezone=true&serverTimezone=Asia/Seoul";
    private static final String local_username = "root";
    private static final String local_password = "Kjhyeong0219!";

    private static final String AWSRDS_jdbcUrl = "jdbc:mysql://budgettracker.cfuygiu08mdn.ap-northeast-2.rds.amazonaws.com:3306/budgettracker?useTimezone=true&serverTimezone=Asia/Seoul";
    private static final String AWSRDS_jdbcUrl_username = "root";
    private static final String AWSRDS_jdbcUrl_password = "Kjhyeong0219!";

    private static Connection connection;

    public static Connection getCon() throws SQLException {
        try {
            connection = DriverManager.getConnection(AWSRDS_jdbcUrl, AWSRDS_jdbcUrl_username, AWSRDS_jdbcUrl_password);
            return connection;
        } catch(SQLException e) {
            throw e;
        }
    }
}
