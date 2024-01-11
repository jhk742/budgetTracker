import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

import databaseHandlers.categoryFormDatabaseHandlers;
import Connectors.ConnectionProvider;
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
public class accountBalanceFormDatabaseHandlersTest {

    private Connection mockConnection;
    private PreparedStatement mockGetLoggedUserTotalBalancePS;
    private PreparedStatement mockGetLoggedUserTotalIncomeAndExpensePS;
    private PreparedStatement mockRetrieveDataForAllPS;
    private PreparedStatement mockRetrieveDataForIncomePS;
    private PreparedStatement mockRetrieveDataForExpensePS;
    private PreparedStatement mockRetrieveYearMonthPS;

    @Before
    public void setup() throws SQLException {
        mockConnection = mock(Connection.class);
        mockGetLoggedUserTotalBalancePS = mock(PreparedStatement.class);
        mockGetLoggedUserTotalIncomeAndExpensePS = mock(PreparedStatement.class);
        mockRetrieveDataForAllPS = mock(PreparedStatement.class);
        mockRetrieveDataForIncomePS = mock(PreparedStatement.class);
        mockRetrieveDataForExpensePS = mock(PreparedStatement.class);
        mockRetrieveYearMonthPS = mock(PreparedStatement.class);

        mockStatic(ConnectionProvider.class);
        when(ConnectionProvider.getCon()).thenReturn(mockConnection);

        when(mockConnection.prepareStatement("select * from bank_accounts where user_id=?"))
                .thenReturn(mockGetLoggedUserTotalBalancePS);
        when(mockConnection.prepareStatement("SELECT SUM(CASE WHEN type = 'Income' THEN amount ELSE 0 END) AS total_income, " +
                "SUM(CASE WHEN type = 'Expense' THEN amount ELSE 0 END) AS total_expense " +
                "FROM transactions WHERE account_id = ?"))
                .thenReturn(mockGetLoggedUserTotalIncomeAndExpensePS);
        when(mockConnection.prepareStatement("SELECT t.amount, t.type, t.running_balance, t.category_id, c.name, t.date\n" +
                "FROM transactions t LEFT JOIN categories c ON t.category_id = c.category_id\n" +
                "WHERE t.account_id = ? AND (t.type = 'Expense' OR t.category_id IS NULL)"))
                .thenReturn(mockRetrieveDataForAllPS);
        when(mockConnection.prepareStatement("SELECT t.amount, t.type, t.running_balance, t.category_id, c.name, t.date\n" +
                "FROM transactions t LEFT JOIN categories c ON t.category_id = c.category_id\n" +
                "WHERE t.account_id = ? AND (t.type='Expense' OR t.category_id IS NULL)\n" +
                "ORDER BY CASE\n" +
                "    WHEN type = 'Income' THEN 1\n" +
                "    WHEN type = 'Expense' THEN 2\n" +
                "END"))
                .thenReturn(mockRetrieveDataForAllPS);
        when(mockConnection.prepareStatement("SELECT t.amount, t.type, t.running_balance, t.category_id, c.name, t.date\n" +
                "FROM transactions t LEFT JOIN categories c ON t.category_id = c.category_id\n" +
                "WHERE t.account_id = ? AND (t.type='Expense' OR t.category_id IS NULL)\n" +
                "ORDER BY CASE\n" +
                "   WHEN type = 'Expense' THEN 1\n" +
                "   WHEN type = 'Income' THEN 2\n" +
                "END"))
                .thenReturn(mockRetrieveDataForAllPS);
        when(mockConnection.prepareStatement("SELECT t.amount, t.type, t.running_balance, t.category_id, c.name, t.date\n" +
                "FROM transactions t LEFT JOIN categories c ON t.category_id = c.category_id\n" +
                "WHERE t.account_id = ? AND (t.type='Expense' OR t.category_id IS NULL) AND t.date BETWEEN ? AND ?"))
                .thenReturn(mockRetrieveDataForAllPS);
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
}
