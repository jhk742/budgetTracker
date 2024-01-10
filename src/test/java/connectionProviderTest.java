import Connectors.ConnectionProvider;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class connectionProviderTest {

    private static Connection testConnection;

    @AfterClass
    public static void setUp() {
        try {
            if (testConnection != null && !testConnection.isClosed()) {
                testConnection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetCon() throws SQLException {
        try {
            testConnection = ConnectionProvider.getCon();
            DatabaseMetaData metaData = testConnection.getMetaData();
            assertNotNull("The connection object is not null.", testConnection);
            assertNotEquals("Metadata (URL) received from the Connection object is not an empty string.", metaData.getURL());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
