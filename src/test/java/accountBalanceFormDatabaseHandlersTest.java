import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

import Users.loggedUser;
import databaseHandlers.accountBalanceFormDatabaseHandlers;
import Connectors.ConnectionProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ConnectionProvider.class)
public class accountBalanceFormDatabaseHandlersTest {

    private Connection mockConnection;
    private PreparedStatement mockGetLoggedUserTotalBalancePS;
    private PreparedStatement mockGetLoggedUserTotalIncomeAndExpensePS;
    private PreparedStatement mockRetrieveDataForAllDefaultPS;
    private PreparedStatement mockRetrieveDataForAllIncomePS;
    private PreparedStatement mockRetrieveDataForAllExpensesPS;
    private PreparedStatement mockRetrieveDataForAllDateRangePS;
    private PreparedStatement mockRetrieveDataForIncomePS;
    private PreparedStatement mockRetrieveDataForExpensePS;
    private PreparedStatement mockRetrieveYearMonthPS;
    private ResultSet mockResultSet;
    private loggedUser loggedU;
    private accountBalanceFormDatabaseHandlers databaseHandlers;


    @Before
    public void setup() throws SQLException {
        mockConnection = mock(Connection.class);
        mockGetLoggedUserTotalBalancePS = mock(PreparedStatement.class);
        mockGetLoggedUserTotalIncomeAndExpensePS = mock(PreparedStatement.class);
        mockRetrieveDataForAllDefaultPS = mock(PreparedStatement.class);
        mockRetrieveDataForAllIncomePS = mock(PreparedStatement.class);
        mockRetrieveDataForAllExpensesPS = mock(PreparedStatement.class);
        mockRetrieveDataForAllDateRangePS = mock(PreparedStatement.class);
        mockRetrieveDataForIncomePS = mock(PreparedStatement.class);
        mockRetrieveDataForExpensePS = mock(PreparedStatement.class);
        mockRetrieveYearMonthPS = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        databaseHandlers = new accountBalanceFormDatabaseHandlers();
        loggedU = new loggedUser("1");

        mockStatic(ConnectionProvider.class);
        when(ConnectionProvider.getCon()).thenReturn(mockConnection);

        when(mockConnection.prepareStatement("select * from bank_accounts where user_id=?"))
                .thenReturn(mockGetLoggedUserTotalBalancePS);
        when(mockConnection.prepareStatement("SELECT SUM(CASE WHEN type = 'Income' THEN amount ELSE 0 END) AS total_income, " +
                "SUM(CASE WHEN type = 'Expense' THEN amount ELSE 0 END) AS total_expense " +
                "FROM transactions WHERE account_id = ?"))
                .thenReturn(mockGetLoggedUserTotalIncomeAndExpensePS);
        when(mockConnection.prepareStatement("SELECT\n" +
                "    t.amount,\n" +
                "    t.type,\n" +
                "    t.running_balance,\n" +
                "    t.category_id,\n" +
                "    c.name,\n" +
                "    t.date\n" +
                "FROM\n" +
                "    transactions t\n" +
                "LEFT JOIN\n" +
                "    categories c ON t.category_id = c.category_id\n" +
                "WHERE t.account_id = ? AND (t.type = 'Expense' OR t.category_id IS NULL)"))
                .thenReturn(mockRetrieveDataForAllDefaultPS);
        when(mockConnection.prepareStatement("SELECT\n" +
                "\tt.amount,\n" +
                "    t.type,\n" +
                "    t.running_balance,\n" +
                "    t.category_id,\n" +
                "    c.name,\n" +
                "    t.date\n" +
                "FROM\n" +
                "\ttransactions t\n" +
                "LEFT JOIN\n" +
                "\tcategories c ON t.category_id = c.category_id\n" +
                "WHERE t.account_id = ? AND (t.type='Expense' OR t.category_id IS NULL)\n" +
                "ORDER BY CASE\n" +
                "\tWHEN type = 'Income' THEN 1\n" +
                "    WHEN type = 'Expense' THEN 2\n" +
                "END"))
                .thenReturn(mockRetrieveDataForAllIncomePS);
        when(mockConnection.prepareStatement("SELECT\n" +
                "\tt.amount,\n" +
                "    t.type,\n" +
                "    t.running_balance,\n" +
                "    t.category_id,\n" +
                "    c.name,\n" +
                "    t.date\n" +
                "FROM\n" +
                "\ttransactions t\n" +
                "LEFT JOIN\n" +
                "\tcategories c ON t.category_id = c.category_id\n" +
                "WHERE t.account_id = ? AND (t.type='Expense' OR t.category_id IS NULL)\n" +
                "ORDER BY CASE\n" +
                "\tWHEN type = 'Expense' THEN 1\n" +
                "    WHEN type = 'Income' THEN 2\n" +
                "END"))
                .thenReturn(mockRetrieveDataForAllExpensesPS);
        when(mockConnection.prepareStatement("SELECT\n" +
                "\tt.amount,\n" +
                "    t.type,\n" +
                "    t.running_balance,\n" +
                "    t.category_id,\n" +
                "    c.name,\n" +
                "    t.date\n" +
                "FROM\n" +
                "\ttransactions t\n" +
                "LEFT JOIN\n" +
                "\tcategories c ON t.category_id = c.category_id\n" +
                "WHERE t.account_id = ? AND (t.type='Expense' OR t.category_id IS NULL) AND t.date BETWEEN ? AND ?"))
                .thenReturn(mockRetrieveDataForAllDateRangePS);
        when(mockConnection.prepareStatement("SELECT amount, type, running_balance, date\n" +
                "FROM transactions WHERE type = 'Income' AND account_id = ?"))
                .thenReturn(mockRetrieveDataForIncomePS);
        when(mockConnection.prepareStatement("SELECT t.amount, t.type, t.running_balance, t.date\n" +
                " FROM transactions t LEFT JOIN categories c ON t.category_id = c.category_id\n" +
                " WHERE t.account_id = ? AND t.type = 'Income' AND YEAR(t.date) = ? AND MONTH(t.date) = ?"))
                .thenReturn(mockRetrieveDataForIncomePS);
        when(mockConnection.prepareStatement("SELECT amount, type, running_balance, date " +
                "FROM transactions WHERE type = 'Income' AND account_id = ? AND date BETWEEN ? AND ?"))
                .thenReturn(mockRetrieveDataForIncomePS);
        when(mockConnection.prepareStatement("SELECT t.amount, t.type, t.running_balance, t.payment_method, t.category_id, c.name, t.description, t.date\n" +
                "FROM transactions t INNER JOIN categories c ON t.category_id = c.category_id\n" +
                "WHERE t.type = 'Expense' AND t.account_id = ?"))
                .thenReturn(mockRetrieveDataForExpensePS);
        when(mockConnection.prepareStatement("SELECT t.amount, t.type, t.running_balance, t.payment_method, t.category_id, c.name, t.description, t.date\n" +
                " FROM transactions t LEFT JOIN categories c ON t.category_id = c.category_id\n" +
                " WHERE t.account_id = ? AND t.type = 'Expense' AND YEAR(t.date) = ? AND MONTH(t.date) = ?"))
                .thenReturn(mockRetrieveDataForExpensePS);
        when(mockConnection.prepareStatement("SELECT t.amount, t.type, t.running_balance, t.payment_method, t.category_id, c.name, t.description, t.date\n" +
                "FROM transactions t INNER JOIN categories c ON t.category_id = c.category_id\n" +
                "WHERE t.type = 'Expense' AND t.account_id = ? AND t.date BETWEEN ? AND ?"))
                .thenReturn(mockRetrieveDataForExpensePS);
        when(mockConnection.prepareStatement("SELECT DISTINCT " +
                "CONCAT(YEAR(date), '-', LPAD(MONTH(date), 2, '0')) AS 'year_month' FROM transactions " +
                "ORDER BY 'year_month' DESC"))
                .thenReturn(mockRetrieveYearMonthPS);
    }

    @Test
    public void getSetLoggedUserTotalBalanceSuccess() throws SQLException {
        when(mockGetLoggedUserTotalBalancePS.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getBigDecimal("account_balance")).thenReturn(BigDecimal.valueOf(100));
        BigDecimal actualResult = databaseHandlers.getSetLoggedUserTotalBalance(loggedU);
        assertEquals(new BigDecimal(100), actualResult);
        verify(mockGetLoggedUserTotalBalancePS, times(1)).executeQuery();
        verify(mockResultSet, times(1)).next();
    }

    @Test
    public void getSetLoggedUserTotalBalanceFailure() throws SQLException {
        when(mockGetLoggedUserTotalBalancePS.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        BigDecimal actualResult = databaseHandlers.getSetLoggedUserTotalBalance(loggedU);
        assertEquals(new BigDecimal(-1), actualResult);
        verify(mockGetLoggedUserTotalBalancePS, times(1)).executeQuery();
        verify(mockResultSet, times(1)).next();
    }

    @Test
    public void getSetLoggedUserTotalIncomeAndExpenseSuccess() throws SQLException {
        List<BigDecimal> expectedResults = new ArrayList<>(
            Arrays.asList(
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(150)
            )
        );
        when(mockGetLoggedUserTotalIncomeAndExpensePS.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getBigDecimal("total_income")).thenReturn(BigDecimal.valueOf(100));
        when(mockResultSet.getBigDecimal("total_expense")).thenReturn(BigDecimal.valueOf(150));
        List<BigDecimal> actualResults = databaseHandlers.getSetLoggedUserTotalIncomeAndExpense(loggedU);
        assertEquals(expectedResults, actualResults);
        verify(mockGetLoggedUserTotalIncomeAndExpensePS, times(1)).executeQuery();
        verify(mockResultSet, times(1)).next();
    }

    @Test
    public void getSetLoggedUserTotalIncomeAndExpenseFailure() throws SQLException {
        when(mockGetLoggedUserTotalIncomeAndExpensePS.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        List<BigDecimal> actualResult = databaseHandlers.getSetLoggedUserTotalIncomeAndExpense(loggedU);
        assertEquals(new ArrayList<>(), actualResult);
        verify(mockGetLoggedUserTotalIncomeAndExpensePS, times(1)).executeQuery();
    }

    @Test
    public void retrieveDataForAllDefaultSuccess() throws SQLException {
        List<List<String>> expectedResults = new ArrayList<>(
            Arrays.asList(
                Arrays.asList("113", "Income", "1300", "null", "null", "2024-01-01"),
                Arrays.asList("95", "Expense", "1205", "2", "utilities", "2024-01-02")
            )
        );
        when(mockRetrieveDataForAllDefaultPS.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("amount")).thenReturn("113", "95");
        when(mockResultSet.getString("type")).thenReturn("Income", "Expense");
        when(mockResultSet.getString("running_balance")).thenReturn("1300", "1205");
        when(mockResultSet.getString("category_id")).thenReturn("null", "2");
        when(mockResultSet.getString("name")).thenReturn("null", "utilities");
        when(mockResultSet.getString("date")).thenReturn("2024-01-01", "2024-01-02");
        List<List<String>> actualResults = databaseHandlers.retrieveDataForAll(loggedU, "default", "", "");
        assertEquals(expectedResults, actualResults);
        verify(mockRetrieveDataForAllDefaultPS, times(1)).executeQuery();
        verify(mockResultSet, times(3)).next();
    }

    @Test
    public void retrieveDataForAllDefaultFailure() throws SQLException {
        when(mockRetrieveDataForAllDefaultPS.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        List<List<String>> actualResults = databaseHandlers.retrieveDataForAll(loggedU, "default", "", "");
        assertEquals(new ArrayList<List<String>>(), actualResults);
        verify(mockRetrieveDataForAllDefaultPS, times(1)).executeQuery();
        verify(mockResultSet, times(1)).next();
    }

    @Test
    public void retrieveDataForAllIncomeSuccess() throws SQLException {
        List<List<String>> expectedResults = new ArrayList<>(
            Arrays.asList(
                    Arrays.asList("113", "Income", "1300", "null", "null", "2024-01-01"),
                    Arrays.asList("95", "Income", "1395", "null", "null", "2024-01-02")
            )
        );
        when(mockRetrieveDataForAllIncomePS.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("amount")).thenReturn("113", "95");
        when(mockResultSet.getString("type")).thenReturn("Income", "Income");
        when(mockResultSet.getString("running_balance")).thenReturn("1300", "1395");
        when(mockResultSet.getString("category_id")).thenReturn("null", "null");
        when(mockResultSet.getString("name")).thenReturn("null", "null");
        when(mockResultSet.getString("date")).thenReturn("2024-01-01", "2024-01-02");
        List<List<String>> actualResults = databaseHandlers.retrieveDataForAll(loggedU, "Income", "", "");
        assertEquals(expectedResults, actualResults);
        verify(mockRetrieveDataForAllIncomePS, times(1)).executeQuery();
        verify(mockResultSet, times(3)).next();
    }

    @Test
    public void retrieveDataForAllIncomeFailure() throws SQLException {
        when(mockRetrieveDataForAllIncomePS.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        List<List<String>> actualResults = databaseHandlers.retrieveDataForAll(loggedU, "Income", "", "");
        assertEquals(new ArrayList<List<String>>(), actualResults);
        verify(mockRetrieveDataForAllIncomePS, times(1)).executeQuery();
        verify(mockResultSet, times(1)).next();
    }

    @Test
    public void retrieveDataForAllExpensesSuccess() throws SQLException {
        //mockRetrieveDataForAllExpensesPS
        List<List<String>> expectedResults = new ArrayList<>(
                Arrays.asList(
                        Arrays.asList("113", "Expense", "1300", "9", "tax", "2024-01-01"),
                        Arrays.asList("95", "Expense", "1205", "11", "medical", "2024-01-02")
                )
        );
        when(mockRetrieveDataForAllExpensesPS.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("amount")).thenReturn("113", "95");
        when(mockResultSet.getString("type")).thenReturn("Expense", "Expense");
        when(mockResultSet.getString("running_balance")).thenReturn("1300", "1205");
        when(mockResultSet.getString("category_id")).thenReturn("9", "11");
        when(mockResultSet.getString("name")).thenReturn("tax", "medical");
        when(mockResultSet.getString("date")).thenReturn("2024-01-01", "2024-01-02");
        List<List<String>> actualResults = databaseHandlers.retrieveDataForAll(loggedU, "Expenses", "", "");
        assertEquals(expectedResults, actualResults);
        verify(mockRetrieveDataForAllExpensesPS, times(1)).executeQuery();
        verify(mockResultSet, times(3)).next();
    }

    @Test
    public void retrieveDataForAllExpensesFailure() throws SQLException {
        when(mockRetrieveDataForAllExpensesPS.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        List<List<String>> actualResults = databaseHandlers.retrieveDataForAll(loggedU, "Expenses", "", "");
        assertEquals(new ArrayList<List<String>>(), actualResults);
        verify(mockRetrieveDataForAllExpensesPS, times(1)).executeQuery();
        verify(mockResultSet, times(1)).next();
    }

    @Test
    public void retrieveDataForAllDateRangeSuccess() throws SQLException {
//        mockRetrieveDataForAllDateRangePS
        List<List<String>> expectedResults = new ArrayList<>(
                Arrays.asList(
                        Arrays.asList("113", "Income", "1300", "null", "null", "2024-01-01"),
                        Arrays.asList("95", "Expense", "1205", "2", "utilities", "2024-01-02")
                )
        );
        when(mockRetrieveDataForAllDateRangePS.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("amount")).thenReturn("113", "95");
        when(mockResultSet.getString("type")).thenReturn("Income", "Expense");
        when(mockResultSet.getString("running_balance")).thenReturn("1300", "1205");
        when(mockResultSet.getString("category_id")).thenReturn("null", "2");
        when(mockResultSet.getString("name")).thenReturn("null", "utilities");
        when(mockResultSet.getString("date")).thenReturn("2024-01-01", "2024-01-02");
        List<List<String>> actualResults = databaseHandlers.retrieveDataForAll(loggedU, "DateRange", "2024-01-01", "2024-01-02");
        assertEquals(expectedResults, actualResults);
        verify(mockRetrieveDataForAllDateRangePS, times(1)).executeQuery();
        verify(mockResultSet, times(3)).next();
    }

    @Test
    public void retrieveDataForAllDateRangeFailure() throws SQLException {
        when(mockRetrieveDataForAllDateRangePS.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        List<List<String>> actualResults = databaseHandlers.retrieveDataForAll(loggedU, "DateRange", "2024-01-01", "2024-01-02");
        assertEquals(new ArrayList<List<String>>(), actualResults);
        verify(mockRetrieveDataForAllDateRangePS, times(1)).executeQuery();
        verify(mockResultSet, times(1)).next();
    }
}
