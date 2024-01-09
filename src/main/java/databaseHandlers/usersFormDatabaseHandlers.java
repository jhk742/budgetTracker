package databaseHandlers;

import Connectors.ConnectionProvider;
import ExceptionHandler.ExceptionHandler;
import Users.User;
import Users.loggedUser;
import gui.usersForm;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.ArrayList;

import static gui.usersForm.getUserFields;

public class usersFormDatabaseHandlers {

    public void updateUser(User user, loggedUser loggedU, usersForm uForm) {
        ArrayList<String> userFields = getUserFields(uForm);
        //send in original values that match the values stored within the table pre-update
        String passWord = getUserPassword(Integer.parseInt(user.id), user.name, user.email, user.phone);
        System.out.println("USER: " + user.id + " " + user.name + " " + user.email + " " + user.phone + " " + passWord);

        try {
            //update with new values provided in the textFields
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("update user set name=?, email=?, phone=?, address=?, status=? where id=? and password=?");
            ps.setString(1, userFields.get(1));
            ps.setString(2, userFields.get(2));
            ps.setString(3, userFields.get(3));
            ps.setString(4, userFields.get(4));
            ps.setInt(5, userFields.get(5).equals("Active") ? 1 : 2);
            ps.setString(6, userFields.get(0));
            ps.setString(7, passWord);
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(null, "The user information has been successfully updated.");
                uForm.setVisible(false);
                usersForm uf = new usersForm(null, loggedU);
                uf.setVisible(true);
            }
        } catch (SQLException er) {
            ExceptionHandler.unableToConnectToDb(er);
        }
    }

    public void deleteUser(User user, loggedUser loggedU, usersForm uForm) {
        ArrayList<String> userFields = getUserFields(uForm);
        String passWord = getUserPassword(Integer.parseInt(user.id), user.name, user.email, user.phone);
        Connection con = null;
        try {
            con = ConnectionProvider.getCon();
            //need to delete from user and bank_accounts (so start a transaction)
            con.setAutoCommit(false);

            //delete bank_accounts
            PreparedStatement deleteBackAccounts = con.prepareStatement("delete from bank_accounts where user_id = ?");
            deleteBackAccounts.setInt(1, Integer.parseInt(userFields.get(0)));
            deleteBackAccounts.executeUpdate();

            //delete the user from the user table
            PreparedStatement ps = con.prepareStatement("delete from user where id=? and name=? and email=? and password=?");
            ps.setInt(1, Integer.parseInt(userFields.get(0)));
            ps.setString(2, userFields.get(1));
            ps.setString(3, userFields.get(2));
            ps.setString(4, passWord);
            int rowsAffected = ps.executeUpdate();

            //if both deletions were successful, commit the transaction
            con.commit();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, "User successfully deleted.");
                uForm.setVisible(false);
                usersForm uf = new usersForm(null, loggedU);
                uf.setVisible(true);
            }
        } catch(SQLException er) {
            // rollback the transaction if exception is thrown
            try {
                if (con != null) {
                    con.rollback();;
                }
            } catch (SQLException rollback) {
                rollback.printStackTrace();
            }
            ExceptionHandler.unableToConnectToDb(er);
        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getUserPassword(int id, String name, String email, String phone) {
        String ret = "";
        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("select * from user where id=? and name=? and email=? and phone=?");
            ps.setInt(1, id);
            ps.setString(2, name);
            ps.setString(3, email);
            ps.setString(4, phone);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ret = rs.getString("password");
            }
            return ret;
        } catch (SQLException e) {
            ExceptionHandler.unableToConnectToDb(e);
        }
        return null;
    }

    public void populateTable(JTable tableUsers) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID","Name","Email","Phone","Address","Status"},0
        );
        tableUsers.setModel(model);
        try {
            Connection con = ConnectionProvider.getCon();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("select * from user");
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString("id"), rs.getString("name"), rs.getString("email"), rs.getString("phone"), rs.getString("address"), rs.getString("status").equals("1") ? "Active" : "Inactive"});
            }
        } catch(SQLException e) {
            ExceptionHandler.unableToConnectToDb(e);
        }
    }
}
