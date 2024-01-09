package databaseHandlers;

import Connectors.ConnectionProvider;
import ExceptionHandler.ExceptionHandler;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class addUserFormDatabaseHandlers {
    public void insertUser(String name, String email, String phone, String address, String password, String status, BigDecimal startingBalance) {
        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("insert into user (name, email, phone, address, password, status) values (?, ?, ?, ?, ?, ?)");
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, address);
            ps.setString(5, password);
            ps.setInt(6, status.equals("Active") ? 1 : 0);
            int rowsAffected = ps.executeUpdate();
            try {
                PreparedStatement createdUser = con.prepareStatement("select * from user where name=? and email=? and password=?");
                createdUser.setString(1, name);
                createdUser.setString(2, email);
                createdUser.setString(3, password);
                ResultSet rs = createdUser.executeQuery();
                if (rs.next()) {
                    int userId = rs.getInt("id");
                    try {
                        //retrieve and use id after creation
                        PreparedStatement psBalance = con.prepareStatement("insert into bank_accounts (user_id, account_balance) values (?, ?)");
                        psBalance.setInt(1, userId);
                        psBalance.setBigDecimal(2, startingBalance);
                        int rowsAffectedBankAccounts = psBalance.executeUpdate();
                        if (rowsAffected > 0 && rowsAffectedBankAccounts > 0) {
                            JOptionPane.showMessageDialog(null, "User successfully created.");
                        }
                    } catch (SQLException insertNewUser) {
                        ExceptionHandler.unableToConnectToDb(insertNewUser);
                    }
                }
            } catch (SQLException createdUser) {
                ExceptionHandler.unableToConnectToDb(createdUser);
            }
        } catch(SQLException er) {
            ExceptionHandler.unableToConnectToDb(er);
        }
    }
}
