package databaseHandlers;

import Connectors.ConnectionProvider;
import ExceptionHandler.ExceptionHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class categoryFormDatabaseHandlers {

    public static void addCategory(JTable tableCategories, String categoryName, String categoryDescription) {
        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("insert into categories (name, description) values (?, ?)");
            ps.setString(1, categoryName);
            ps.setString(2, categoryDescription);
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(null, "Successfully created category: " + categoryName);
                populateTable(tableCategories);
            }
        } catch (SQLException ex) {
            ExceptionHandler.unableToConnectToDb(ex);
        }
    }

    public static void populateTable(JTable tableCategories) throws SQLException {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID", "Name", "Description"},0
        );
        tableCategories.setModel(model);
        try {
            Connection con = ConnectionProvider.getCon();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("select * from categories");
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString("category_id"), rs.getString("name"), rs.getString("description")});
            }
        } catch(SQLException x) {
            throw new SQLException(x);
        }
    }
}
