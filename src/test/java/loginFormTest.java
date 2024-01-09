import gui.loginForm;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class loginFormTest {

    private loginForm lForm;
//    @Before public void setup() {
//        lForm = new loginForm(null);
//    }

//    @After public void tearDown() {
//        lForm.dispose();
//    }

    // UNIT TEST
    @Test
    public void testAuthenticateUser() throws SQLException {
        //Create a mock for ConnectionProvider
//        ConnectionProvider connectionProviderMock = Mockito.mock(ConnectionProvider.class);
        //Create a mock for Connection
        Connection connectionMock = Mockito.mock(Connection.class);
        //Create a mock for PreparedStatement
        PreparedStatement psMock = Mockito.mock(PreparedStatement.class);
        //Create a mock for ResultSet
        ResultSet rsMock = Mockito.mock(ResultSet.class);

        /*Mock the behavior of ConnectionProvider.getCon() such that when someone invokes the method,
          getCon() isn't actually called, but a mock is returned.
        **/
//        when(ConnectionProvider.getCon()).thenReturn(connectionMock);
        when(connectionMock.prepareStatement(any(String.class))).thenReturn(psMock);

        Mockito.doNothing().when(psMock).setString(1, "johnDoe@test.com");
        Mockito.doNothing().when(psMock).setString(2, "doe123");
        when(psMock.executeQuery()).thenReturn(rsMock);

        //Mock the behavior of the ResultSet
        when(rsMock.next()).thenReturn(true);
        when(rsMock.getString("id")).thenReturn("1");
        when(rsMock.getString("name")).thenReturn("John Doe");
        when(rsMock.getString("address")).thenReturn("187 Doe St");
        when(rsMock.getString("email")).thenReturn("johnDoe@test.com");
        when(rsMock.getString("phone")).thenReturn("012-345-6789");
        when(rsMock.getString("password")).thenReturn("doe123");
        when(rsMock.getInt("status")).thenReturn(1);

        //call the method
//        lForm = new loginForm(null);
//        loggedUser user = lForm.authenticateUser("joenDoe@test.com", "doe123");

        //Assert the expected results
//        assertNotNull(user);
//        assertEquals("1", user.id);
//        assertEquals("John Doe", user.name);
//        assertEquals("187 Doe St", user.address);
//        assertEquals("johnDoe@test.com", user.email);
//        assertEquals("012-345-6789", user.phone);
//        assertEquals("doe123", user.password);
//        assertEquals(1, user.status);
    }
}
