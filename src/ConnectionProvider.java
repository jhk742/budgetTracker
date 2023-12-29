import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionProvider {

    private static final String jdbcUrl = "jdbc:mysql://localhost:3306/budgettracker?useTimezone=true&serverTimezone=Asia/Seoul";
    private static final String username = "root";
    private static final String password = "Kjhyeong0219!";

    private static Connection connection;

    public static Connection getCon() {
        try {
            connection = DriverManager.getConnection(jdbcUrl, username, password);
            return connection;
        } catch(SQLException e) {
            System.out.println(e);
            return null;
        }
    }
}
