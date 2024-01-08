package ExceptionHandler;

import jdk.nashorn.internal.scripts.JO;

import javax.swing.*;
import java.sql.SQLException;

public class ExceptionHandler {

    public static void userDoesNotExist(RuntimeException error) {
        JOptionPane.showMessageDialog(null, "Authentication failed: " + error.getMessage());
    }

    public static void allFieldsRequired(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

    public static void unableToConnectToDb(SQLException e) {
        String connectionDefaultMessage = "Database Connection Error. " + "\nReason: ";
        String queryDefaultMessage = "Database Operation Error. " + "\nReason: ";
        int errorCode = e.getErrorCode();
        //access denied
        if (errorCode == 1045) {
            JOptionPane.showMessageDialog(null, connectionDefaultMessage + e.getMessage().substring(0, e.getMessage().length() - 21) + "\nVendor Code: " +
                    errorCode);
        }
        //unknown database
        if (errorCode == 1049) {
            JOptionPane.showMessageDialog(null, connectionDefaultMessage + e.getMessage() + "\nVendor Code: " + errorCode);
        }
        //communications link failure
        if (errorCode == 0) {
            JOptionPane.showMessageDialog(null, connectionDefaultMessage + e.getMessage().substring(e.getMessage().indexOf(":") + 1) + "\nVendor Code: " + errorCode);
        }
        //unknown table
        if (errorCode == 1146) {
            JOptionPane.showMessageDialog(null, queryDefaultMessage + e.getMessage() + "\nVendor Code: " + errorCode);
        }
        //unknown column
        if (errorCode == 1054) {
            JOptionPane.showMessageDialog(null, queryDefaultMessage + e.getMessage() + "\nVendor Code: " + errorCode);
        }

    }

}
