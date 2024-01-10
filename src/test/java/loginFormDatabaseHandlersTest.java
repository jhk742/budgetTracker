import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.*;

import databaseHandlers.loginFormDatabaseHandlers;
import Connectors.ConnectionProvider;
import Users.loggedUser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ConnectionProvider.class)
public class loginFormDatabaseHandlersTest {
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    private loginFormDatabaseHandlers databaseHandlers;

    @Before
    public void setUp() throws SQLException {
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        databaseHandlers = new loginFormDatabaseHandlers();

        mockStatic(ConnectionProvider.class);
        when(ConnectionProvider.getCon()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(any(String.class))).thenReturn(mockPreparedStatement);
    }

    /**
     * Assumes that when the PreparedStatement is executed, it successfully retrieves the values for a specified user from the user table and
     * assigns each to its corresponding field variable of the object, loggedUser.
     */
    @Test
    public void testAuthenticateUserSuccess() throws SQLException {
        //setup
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("id")).thenReturn("1");
        when(mockResultSet.getString("name")).thenReturn("John Doe");
        when(mockResultSet.getString("address")).thenReturn("187 Doe St.");
        when(mockResultSet.getString("email")).thenReturn("johndoe@test.com");
        when(mockResultSet.getString("phone")).thenReturn("012-345-6789");
        when(mockResultSet.getString("password")).thenReturn("johnDoe");
        when(mockResultSet.getInt("status")).thenReturn(1);

        //"execution"
        loggedUser user = databaseHandlers.authenticateUser("johndoe@test.com", "johnDoe");

        //assertion
        assertEquals("1", user.id);
        assertEquals("John Doe", user.name);
        assertEquals("187 Doe St.", user.address);
        assertEquals("johndoe@test.com", user.email);
        assertEquals("012-345-6789", user.phone);
        assertEquals("johnDoe", user.password);
        assertEquals(1, user.status);
    }

    /**
     * If the database doesn't retrieve any user information, the loggedUser's status is set to -1.
     * On the contrary, all remaining fields for which this test asserts as Null would be assigned values retrieved from the db.
     */
    @Test
    public void testAuthenticateUserFailure() throws SQLException {
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        loggedUser user = databaseHandlers.authenticateUser("nonexistent@example.com", "wrongpassword");
        assertEquals(-1, user.status);
        assertNull(user.id);
        assertNull(user.name);
        assertNull(user.address);
        assertNull(user.email);
        assertNull(user.phone);
        assertNull(user.password);
    }

    /**
     * This assumes that the status column of the user table for the specified user has been updated to 1 from 0,
     * thus returning 1 as the number of rows affected and consequently causing the method to return a boolean value of true.
     */
    @Test
    public void testReactivateUserSuccess() throws SQLException {
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // Assume one row affected
        boolean result = databaseHandlers.reactivateUser("test@example.com", "password");
        assertTrue(result);
    }

    /**
     When the PreparedStatement within reactivateUser does not affect any rows within the database's table,
     the method will return false.
     */
    @Test
    public void testReactivateUserFailure() throws SQLException {
        when(mockPreparedStatement.executeUpdate()).thenReturn(0); // Assume no rows affected
        boolean result = databaseHandlers.reactivateUser("nonexistent@example.com", "wrongpassword");
        assertFalse(result);
    }
}
