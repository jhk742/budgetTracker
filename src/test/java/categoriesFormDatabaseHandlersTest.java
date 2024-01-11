import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

import databaseHandlers.categoryFormDatabaseHandlers;
import Connectors.ConnectionProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Result;
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
public class categoriesFormDatabaseHandlersTest {

    private Connection mockConnection;
    private PreparedStatement mockAddCategoryPS;
    private PreparedStatement mockRetrieveCategoriesPS;
    private ResultSet mockResultSet;
    private categoryFormDatabaseHandlers databaseHandlers;

    @Before
    public void setup() throws SQLException {
        mockConnection = mock(Connection.class);
        mockAddCategoryPS = mock(PreparedStatement.class);
        mockRetrieveCategoriesPS = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        databaseHandlers = new categoryFormDatabaseHandlers();

        mockStatic(ConnectionProvider.class);
        when(ConnectionProvider.getCon()).thenReturn(mockConnection);

        when(mockConnection.prepareStatement("insert into categories (name, description) values (?, ?)"))
                .thenReturn(mockAddCategoryPS);
        when(mockConnection.prepareStatement("select * from categories"))
                .thenReturn(mockRetrieveCategoriesPS);
    }

    @Test
    public void addCategoryFailure() throws SQLException {
        when(mockAddCategoryPS.executeUpdate()).thenReturn(0);
        boolean result = databaseHandlers.addCategory("", "");
        assertFalse(result);
        verify(mockAddCategoryPS, times(1)).executeUpdate();
    }

    @Test
    public void addCategorySuccess() throws SQLException {
        when(mockAddCategoryPS.executeUpdate()).thenReturn(1);
        boolean result = databaseHandlers.addCategory("", "");
        assertTrue(result);
        verify(mockAddCategoryPS, times(1)).executeUpdate();
    }

    @Test
    public void retrieveCategoriesFailure() throws SQLException {
        when(mockRetrieveCategoriesPS.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        List<List<String>> actualResults = databaseHandlers.retrieveCategories();
        assertEquals(new ArrayList<List<String>>(), actualResults);
        verify(mockRetrieveCategoriesPS, times(1)).executeQuery();
        verify(mockResultSet, times(1)).next();
    }

    @Test
    public void retrieveCategoriesSuccess() throws SQLException {
        when(mockRetrieveCategoriesPS.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("category_id")).thenReturn("1", "2");
        when(mockResultSet.getString("name")).thenReturn("Grocery", "Utilities");
        when(mockResultSet.getString("description")).thenReturn("food, beverages, etc.", "electricity, heating, gas bills, etc.");

        List<List<String>> expectedResults = new ArrayList<>(
                Arrays.asList(
                        Arrays.asList("1", "Grocery", "food, beverages, etc."),
                        Arrays.asList("2", "Utilities", "electricity, heating, gas bills, etc.")
                )
        );

        List<List<String>> actualResults = databaseHandlers.retrieveCategories();
        assertEquals(expectedResults, actualResults);
        verify(mockRetrieveCategoriesPS, times(1)).executeQuery();
        verify(mockResultSet, times(3)).next();
    }
}
