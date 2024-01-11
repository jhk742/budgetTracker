package databaseHandlers;
import Connectors.ConnectionProvider;
import ExceptionHandler.ExceptionHandler;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class categoryFormDatabaseHandlers {

    public static boolean addCategory(String categoryName, String categoryDescription) {
        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("insert into categories (name, description) values (?, ?)");
            ps.setString(1, categoryName);
            ps.setString(2, categoryDescription);
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                return true;
            }
        } catch (SQLException ex) {
            ExceptionHandler.unableToConnectToDb(ex);
        }
        return false;
    }

    public List<List<String>> retrieveCategories() {
        List<List<String>> retData = new ArrayList<>();
        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("select * from categories");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                List<String> rowData = new ArrayList<>();
                rowData.add(rs.getString("category_id"));
                rowData.add(rs.getString("name"));
                rowData.add(rs.getString("description"));
                retData.add(rowData);
            }
        } catch(SQLException e) {
            ExceptionHandler.unableToConnectToDb(e);
        }
        return retData;
    }
}
