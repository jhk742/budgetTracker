package ExceptionHandler.Exceptions;

import java.sql.SQLException;

public class DatabaseConnectionException extends RuntimeException {
    public DatabaseConnectionException(SQLException e) {
        super(e);
    }
}
