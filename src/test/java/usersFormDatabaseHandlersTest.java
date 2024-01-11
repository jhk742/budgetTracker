import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

import Users.User;
import databaseHandlers.usersFormDatabaseHandlers;
import Connectors.ConnectionProvider;
import Users.loggedUser;
import gui.usersForm;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ConnectionProvider.class)
public class usersFormDatabaseHandlersTest {
    private Connection mockConnection;
    private PreparedStatement mockUpdateUserPS;
    private PreparedStatement mockDeleteBankAccountPS;
    private PreparedStatement mockDeleteUserPS;
    private PreparedStatement mockUserPasswordPS;
    private PreparedStatement mockRetrieveUsersPS;
    private ResultSet mockResultSet;
    private usersFormDatabaseHandlers databaseHandlers;
    private ArrayList<String> mockUserFields;

    @Before
    public void setUp() throws SQLException {
        mockConnection = mock(Connection.class);
        mockUpdateUserPS = mock(PreparedStatement.class);
        mockDeleteBankAccountPS = mock(PreparedStatement.class);
        mockDeleteUserPS = mock(PreparedStatement.class);
        mockUserPasswordPS = mock(PreparedStatement.class);
        mockRetrieveUsersPS = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        databaseHandlers = new usersFormDatabaseHandlers();
        mockUserFields = new ArrayList<>(Arrays.asList(
                "1",          // id
                "John",       // name
                "john@example.com",  // email
                "1234567890", // phone
                "123 Main St", // address
                "Active"      // status
        ));

        mockStatic(ConnectionProvider.class);
        when(ConnectionProvider.getCon()).thenReturn(mockConnection);

        // Use specific mocks for specific statements (delete user)
        when(mockConnection.prepareStatement("update user set name=?, email=?, phone=?, address=?, status=? where id=? and password=?"))
                .thenReturn(mockUpdateUserPS);
        when(mockConnection.prepareStatement("delete from bank_accounts where user_id = ?"))
                .thenReturn(mockDeleteBankAccountPS);
        when(mockConnection.prepareStatement("delete from user where id=? and name=? and email=? and password=?"))
                .thenReturn(mockDeleteUserPS);
        when(mockConnection.prepareStatement("select * from user where id=? and name=? and email=? and phone=?"))
                .thenReturn(mockUserPasswordPS);
        when(mockConnection.prepareStatement("select * from user"))
                .thenReturn(mockRetrieveUsersPS);
    }

    /**
     * the executeUpdate method of the preparedStatement is designed to return
     * 0, conveying that no rows had been affected = false;
     */
    @Test
    public void testUpdateUserFailure() throws SQLException {
        when(mockUpdateUserPS.executeUpdate()).thenReturn(0);
        boolean result = databaseHandlers.updateUser("", mockUserFields);
        assertFalse(result);
        verify(mockUpdateUserPS, times(1)).executeUpdate();
    }

    @Test
    public void testUpdateUserSuccess() throws SQLException {
        when(mockUpdateUserPS.executeUpdate()).thenReturn(1);
        boolean result = databaseHandlers.updateUser("", mockUserFields);
        assertTrue(result);
        verify(mockUpdateUserPS, times(1)).executeUpdate();
    }

    @Test
    public void testDeleteUserFailure() throws SQLException {
        when(mockDeleteBankAccountPS.executeUpdate()).thenReturn(0);
        when(mockDeleteUserPS.executeUpdate()).thenReturn(0);
        boolean result = databaseHandlers.deleteUser("test", mockUserFields);
        assertFalse(result);
        verify(mockDeleteBankAccountPS, times(1)).executeUpdate();
        verify(mockDeleteUserPS, times(1)).executeUpdate();
    }

    @Test
    public void testDeleteUserSuccess() throws SQLException {
        when(mockDeleteBankAccountPS.executeUpdate()).thenReturn(1);
        when(mockDeleteUserPS.executeUpdate()).thenReturn(1);
        boolean result = databaseHandlers.deleteUser("test", mockUserFields);
        assertTrue(result);
        verify(mockDeleteBankAccountPS, times(1)).executeUpdate();
        verify(mockDeleteUserPS, times(1)).executeUpdate();
    }

    @Test
    public void testGetUserPasswordFailure() throws SQLException {
        when(mockUserPasswordPS.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        String result = databaseHandlers.getUserPassword(0, "", "", "");
        assertEquals("", result);
        verify(mockUserPasswordPS, times(1)).executeQuery();
    }

    @Test
    public void testGetUserPasswordSuccess() throws SQLException {
        String expectedPassword = "testPassword";
        when(mockUserPasswordPS.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("password")).thenReturn(expectedPassword);
        String result = databaseHandlers.getUserPassword(0,"", "", "");
        assertEquals("testPassword", result);
        verify(mockUserPasswordPS, times(1)).executeQuery();
    }

    @Test
    public void testRetrieveUsersSuccess() throws SQLException {
        when(mockRetrieveUsersPS.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, true, false);
        when(mockResultSet.getString("id")).thenReturn("1", "2", "3");
        when(mockResultSet.getString("name")).thenReturn("John", "Jane", "Jack");
        when(mockResultSet.getString("email")).thenReturn("john@example.com", "jane@example.com", "jack@example.com");
        when(mockResultSet.getString("phone")).thenReturn("0000000", "1111111", "2222222");
        when(mockResultSet.getString("address")).thenReturn("123 Numbers St", "ABC Letters St", "Seoul, KR");
        when(mockResultSet.getString("status")).thenReturn("Active", "Inactive", "Active");

        // Expected result
        List<List<String>> expectedResult = new ArrayList<>(
                Arrays.asList(
                    Arrays.asList("1", "John", "john@example.com", "0000000", "123 Numbers St", "Active"),
                    Arrays.asList("2", "Jane", "jane@example.com", "1111111", "ABC Letters St", "Inactive"),
                    Arrays.asList("3", "Jack", "jack@example.com", "2222222", "Seoul, KR", "Active")
                )
        );
        // Actual result
        List<List<String>> actualResult = databaseHandlers.retrieveUsers();
        assertEquals(expectedResult, actualResult);
        verify(mockRetrieveUsersPS, times(1)).executeQuery();
        verify(mockResultSet, times(4)).next(); // three rows plus one for the initial state
    }

    @Test
    public void testRetrieveUsersFailure() throws SQLException {
        when(mockRetrieveUsersPS.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<List<String>> actualResult = databaseHandlers.retrieveUsers();
        assertEquals(new ArrayList<List<String>>(), actualResult);
        verify(mockRetrieveUsersPS, times(1)).executeQuery();
    }
}
