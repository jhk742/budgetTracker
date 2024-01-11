import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

import databaseHandlers.chartsFormDatabaseHandlers;
import Connectors.ConnectionProvider;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.jdbc.JDBCCategoryDataset;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ConnectionProvider.class, JDBCCategoryDataset.class})
public class chartsFormDatabaseHandlersTest {
    private Connection mockConnection;
    private PreparedStatement mockPieChartDataSetPS;
    private JDBCCategoryDataset mockBarChartDataSetST;
    private PreparedStatement mockLineGraphDataSetPS;
    private ResultSet mockResultSet;
    private chartsFormDatabaseHandlers databaseHandlers;

    @Before
    public void setup() throws SQLException{
        databaseHandlers = new chartsFormDatabaseHandlers();
        mockConnection = mock(Connection.class);
        mockPieChartDataSetPS = mock(PreparedStatement.class);
        mockBarChartDataSetST = mock(JDBCCategoryDataset.class);
        mockLineGraphDataSetPS = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        mockStatic(ConnectionProvider.class);
        when(ConnectionProvider.getCon()).thenReturn(mockConnection);

        when(mockConnection.prepareStatement("SELECT c.name AS category, SUM(t.amount) AS total\n" +
                "FROM transactions t\n" +
                "INNER JOIN categories c ON t.category_id = c.category_id\n" +
                "WHERE t.type = 'Expense'\n" +
                "GROUP BY c.name")).thenReturn(mockPieChartDataSetPS);



        when(mockConnection.prepareStatement("\"SELECT date, SUM(amount) AS total_amount\\n\" +\n" +
                "FROM transactions\n" +
                "WHERE type = 'Income'\n" +
                "GROUP BY date\n" +
                "ORDER BY date")).thenReturn(mockLineGraphDataSetPS);
        when(mockConnection.prepareStatement("SELECT\n" +
                " DATE(date) AS date,\n" +
                " SUM(CASE WHEN type = 'Income' THEN amount ELSE 0 END) -\n" +
                " SUM(CASE WHEN type = 'Expense' THEN amount ELSE 0 END) AS running_balance\n" +
                " FROM transactions\n" +
                " GROUP BY DATE(date)\n" +
                " ORDER BY DATE(date)")).thenReturn(mockLineGraphDataSetPS);
        when(mockConnection.prepareStatement("WITH RankedTransactions AS (\n" +
                " SELECT\n" +
                " *,\n" +
                " ROW_NUMBER() OVER (PARTITION BY date ORDER BY transaction_id DESC) AS row_num\n" +
                " FROM transactions)\n" +
                " SELECT\n" +
                " date,\n" +
                " running_balance\n" +
                " FROM RankedTransactions\n" +
                " WHERE row_num = 1")).thenReturn(mockLineGraphDataSetPS);
    }

    @Test
    public void createPieChartDataSetFailure() throws SQLException {
        when(mockPieChartDataSetPS.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        DefaultPieDataset actualResults = databaseHandlers.createPieChartDataset();
        assertEquals(new DefaultPieDataset<>(), actualResults);
        verify(mockPieChartDataSetPS, times(1)).executeQuery();
        verify(mockResultSet, times(1)).next();
    }

    @Test
    public void createPieChartDataSetSuccess() throws SQLException {
        List<List<String>> expectedResults = new ArrayList<>(
                Arrays.asList(
                        Arrays.asList("Food", "Misc"),
                        Arrays.asList(String.valueOf(347.5), String.valueOf(230.0))
                )
        );
        when(mockPieChartDataSetPS.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("category")).thenReturn("Food", "Misc");
        when(mockResultSet.getDouble("total")).thenReturn(347.5, 230.0);

        DefaultPieDataset data = databaseHandlers.createPieChartDataset();
        List<List<String>> actualResults = new ArrayList<>();
        ArrayList<String> categories = new ArrayList<>();
        ArrayList<String> totals = new ArrayList<>();
        for (Object key : data.getKeys()) {
            Comparable category = (Comparable) key;
            Number total = data.getValue(category);
            categories.add(category.toString());
            totals.add(String.valueOf(total.doubleValue()));
        }
        actualResults.add(categories);
        actualResults.add(totals);
        assertEquals(expectedResults, actualResults);
        verify(mockPieChartDataSetPS, times(1)).executeQuery();
        verify(mockResultSet, times(3)).next();
    }
}
