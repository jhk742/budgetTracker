package databaseHandlers;

import Connectors.ConnectionProvider;
import ExceptionHandler.ExceptionHandler;
import Users.loggedUser;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;

public class transactionManagementFormDatabaseHandlers {

    public void insertTransaction(String option, String formattedDate, String description, BigDecimal amount, String type, loggedUser loggedU, BigDecimal runningBalance, String categoryName, String paymentMethod, String location) {
        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = null;
            if (option.equals("Expense")) {
                // first retrieve the category id
                int categoryId = getCategoryIdByName(categoryName);
                ps = con.prepareStatement("INSERT INTO transactions (date, description, amount, category_id, " +
                        "type, account_id, running_balance, payment_method, location) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
                ps.setString(1, formattedDate);
                ps.setString(2, description);
                ps.setBigDecimal(3, amount);
                ps.setInt(4, categoryId);
                ps.setString(5, type);
                ps.setInt(6, Integer.parseInt(loggedU.id));
                ps.setBigDecimal(7, runningBalance);
                ps.setString(8, paymentMethod);
                ps.setString(9, location);
                int rowsAffectedTransaction = ps.executeUpdate();
                if (rowsAffectedTransaction > 0) {
                    updateBankAccount("Expense", loggedU, amount);
                }
            }
            if (option.equals("Income")) {
                ps = con.prepareStatement("INSERT INTO transactions (date, description, amount, " +
                        "type, account_id, running_balance) " +
                        "VALUES (?, ?, ?, ?, ?, ?)");
                ps.setString(1, formattedDate);
                ps.setString(2, description);
                ps.setBigDecimal(3, amount);
                ps.setString(4, type);
                ps.setInt(5, Integer.parseInt(loggedU.id));
                ps.setBigDecimal(6, runningBalance);
                int rowsAffectedTransaction = ps.executeUpdate();
                if (rowsAffectedTransaction > 0) {
                    updateBankAccount("Income", loggedU, amount);
                }
            }
            //update to show new balance and reset all other fields to default (blanks)
            getSetLoggedUserTotalBalance(loggedU);
        } catch (SQLException er) {
            ExceptionHandler.unableToConnectToDb(er);
        }
    }

    private static void updateBankAccount(String option, loggedUser loggedU, BigDecimal amount) {
        try {
            Connection con = ConnectionProvider.getCon();
            String updateQuery = "update bank_accounts set account_balance = "
                    + (option.equals("Expense") ? "account_balance - ?" : "account_balance + ?")
                    + " WHERE user_id = ?";
            PreparedStatement psBankAccounts = con.prepareStatement(updateQuery);
            psBankAccounts.setBigDecimal(1, amount);
            psBankAccounts.setInt(2, Integer.parseInt(loggedU.id));
            int rowsAffectedBankAccount = psBankAccounts.executeUpdate();
            if (rowsAffectedBankAccount > 0) {
                JOptionPane.showMessageDialog(null, "Transaction created successfully!");
            }
        } catch (SQLException e) {
            ExceptionHandler.unableToConnectToDb(e);
        }
    }

    public void getSetLoggedUserTotalBalance(loggedUser loggedU) {
        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("select * from bank_accounts where user_id=?");
            ps.setString(1, loggedU.id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                loggedU.totalBalance = rs.getBigDecimal("account_balance");
            }
        } catch (SQLException er) {
            ExceptionHandler.unableToConnectToDb(er);
        }
    }

    private static int getCategoryIdByName(String categoryName) {
        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("select category_id from categories where name=?");
            ps.setString(1, categoryName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("category_id");
            }
        } catch (SQLException e) {
            ExceptionHandler.unableToConnectToDb(e);
        }
        return -1;
    }

    public ArrayList<String> retrieveCategories() {
        ArrayList<String> categories = new ArrayList<>();
        try {
            Connection con = ConnectionProvider.getCon();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("select name from categories");
            while (rs.next()) {
                categories.add(rs.getString("name"));
            }
        } catch (SQLException er) {
            ExceptionHandler.unableToConnectToDb(er);
        }
        return categories;
    }
}
