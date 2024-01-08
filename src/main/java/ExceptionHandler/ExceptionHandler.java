package ExceptionHandler;

import jdk.nashorn.internal.scripts.JO;

import javax.swing.*;
import java.sql.SQLException;

public class ExceptionHandler {

    public static void userDoesNotExist(RuntimeException error) {
        JOptionPane.showMessageDialog(null, "Authentication failed: " + error.getMessage());
    }

    public static void unableToConnectToDb(int errorCode, SQLException e) {
        String connectionDefaultMessage = "Database Connection Error. " + "\nReason: ";
        String queryDefaultMessage = "Database Operation Error. " + "\nReason: ";
        if (errorCode == 1045) {
            JOptionPane.showMessageDialog(null, connectionDefaultMessage + e.getMessage().substring(0, e.getMessage().length() - 21) + "\nVendor Code: " +
                    errorCode);
        }
        if (errorCode == 1049) {
            JOptionPane.showMessageDialog(null, connectionDefaultMessage + e.getMessage() + "\nVendor Code: " + errorCode);
        }
        if (errorCode == 0) {
            JOptionPane.showMessageDialog(null, connectionDefaultMessage + e.getMessage().substring(110, e.getMessage().length() - 1) + "\nVendor Code: " + errorCode);
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
