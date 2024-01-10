package databaseHandlers;
import Connectors.ConnectionProvider;
import ExceptionHandler.Exceptions.DatabaseConnectionException;
import Users.loggedUser;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class loginFormDatabaseHandlers {

    public loggedUser authenticateUser(String email, String password) {
        loggedUser user = new loggedUser();
        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("select * from user where email=? and password=?");
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                user.id = rs.getString("id");
                user.name = rs.getString("name");
                user.address = rs.getString("address");
                user.email = rs.getString("email");
                user.phone = rs.getString("phone");
                user.password = rs.getString("password");
                user.status = rs.getInt("status");
            } else {
                user.status = -1;
            }
            return user;
        } catch(SQLException e) {
            throw new DatabaseConnectionException(e);
        }
    }

    public boolean reactivateUser(String email, String password) {
        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("update user set status=1 where email=? and password=?");
            ps.setString(1, email);
            ps.setString(2, password);
            int rowsAffected =  ps.executeUpdate();
            if (rowsAffected > 0) {
                return true;
            }
        } catch(SQLException e) {
            throw new DatabaseConnectionException(e);
        }
        return false;
    }
}
