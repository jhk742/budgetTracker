package databaseHandlers;
import Connectors.ConnectionProvider;
import ExceptionHandler.ExceptionHandler;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class usersFormDatabaseHandlers {

    public boolean updateUser(String password, ArrayList<String> userFields) {
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
            ps.setString(7, password);
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                return true;
            }
        } catch (SQLException er) {
            ExceptionHandler.unableToConnectToDb(er);
        }
        return false;
    }

    public boolean deleteUser(String password, ArrayList<String> userFields) {
        //WILL DELETE FROM user & back_accounts table, but not from transaction (this table is a ledger: data retention)
        Connection con = null;
        try {
            con = ConnectionProvider.getCon();
            //need to delete from user and bank_accounts (so start a transaction)
            con.setAutoCommit(false);

            //delete bank_accounts
            PreparedStatement deleteBankAccounts = con.prepareStatement("delete from bank_accounts where user_id = ?");
            deleteBankAccounts.setInt(1, Integer.parseInt(userFields.get(0)));
            int rowsAffectedBankAccounts = deleteBankAccounts.executeUpdate();

            //delete the user from the user table
            PreparedStatement ps = con.prepareStatement("delete from user where id=? and name=? and email=? and password=?");
            ps.setInt(1, Integer.parseInt(userFields.get(0)));
            ps.setString(2, userFields.get(1));
            ps.setString(3, userFields.get(2));
            ps.setString(4, password);
            int rowsAffectedUser = ps.executeUpdate();

            //if both deletions were successful, commit the transaction
            con.commit();
            if (rowsAffectedUser > 0 && rowsAffectedBankAccounts > 0) {
                return true;
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
        return false;
    }

    public String getUserPassword(int id, String name, String email, String phone) {
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
        return ret;
    }

    public List<List<String>> retrieveUsers() {
        List<List<String>> retData = new ArrayList<>();
        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("select * from user");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                List<String> rowData = new ArrayList<>();
                rowData.add(rs.getString("id"));
                rowData.add(rs.getString("name"));
                rowData.add(rs.getString("email"));
                rowData.add(rs.getString("phone"));
                rowData.add(rs.getString("address"));
                rowData.add(rs.getString("status"));
                retData.add(rowData);
            }
        } catch(SQLException e) {
            ExceptionHandler.unableToConnectToDb(e);
        }
        return retData;
    }
}
