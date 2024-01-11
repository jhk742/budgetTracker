import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

import Users.loggedUser;
import databaseHandlers.transactionManagementFormDatabaseHandlers;
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

@RunWith(PowerMockRunner.class)
@PrepareForTest(ConnectionProvider.class)
public class transactionFormDatabaseHandlersTest {
    private Connection mockConnection;
    private PreparedStatement mockInsertTransactionExpensePS;
    private PreparedStatement mockInsertTransactionIncomePS;
    private PreparedStatement mockUpdateBankAccountExpensePS;
    private PreparedStatement mockUpdateBankAccountIncomePS;
    private PreparedStatement mockGetSetLoggedUserTotalBalancePS;
    private PreparedStatement mockGetCategoryIdByNamePS;
    private PreparedStatement mockRetrieveCategoriesPS;
    private ResultSet mockResultSet;
    private transactionManagementFormDatabaseHandlers databaseHandlers;

    @Before
    public void setup() throws SQLException {
        mockConnection = mock(Connection.class);
        mockInsertTransactionExpensePS = mock(PreparedStatement.class);
        mockInsertTransactionIncomePS = mock(PreparedStatement.class);
        mockUpdateBankAccountExpensePS = mock(PreparedStatement.class);
        mockUpdateBankAccountIncomePS = mock(PreparedStatement.class);
        mockGetSetLoggedUserTotalBalancePS = mock(PreparedStatement.class);
        mockGetCategoryIdByNamePS = mock(PreparedStatement.class);
        mockRetrieveCategoriesPS = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        databaseHandlers = new transactionManagementFormDatabaseHandlers();

        mockStatic(ConnectionProvider.class);
        when(ConnectionProvider.getCon()).thenReturn(mockConnection);

        when(mockConnection.prepareStatement("INSERT INTO transactions (date, description, amount, category_id, " +
                "type, account_id, running_balance, payment_method, location) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"))
                .thenReturn(mockInsertTransactionExpensePS);
        when(mockConnection.prepareStatement("INSERT INTO transactions (date, description, amount, " +
                "type, account_id, running_balance) " +
                "VALUES (?, ?, ?, ?, ?, ?)"))
                .thenReturn(mockInsertTransactionIncomePS);
        when(mockConnection.prepareStatement("update bank_accounts set account_balance = "
                + "account_balance - ? WHERE user_id = ?"))
                .thenReturn(mockUpdateBankAccountExpensePS);
        when(mockConnection.prepareStatement("update bank_accounts set account_balance = "
                + "account_balance + ? WHERE user_id = ?"))
                .thenReturn(mockUpdateBankAccountIncomePS);
        when(mockConnection.prepareStatement("select * from bank_accounts where user_id=?"))
                .thenReturn(mockGetSetLoggedUserTotalBalancePS);
        when(mockConnection.prepareStatement("select category_id from categories where name=?"))
                .thenReturn(mockGetCategoryIdByNamePS);
        when(mockConnection.prepareStatement("select name from categories"))
                .thenReturn(mockRetrieveCategoriesPS);
    }

    @Test
    public void insertTransactionExpenseFailure() throws SQLException {
        when(mockInsertTransactionExpensePS.executeUpdate()).thenReturn(0);
        boolean result = databaseHandlers.insertTransaction("Expense", "", "", new BigDecimal(0), "", new loggedUser("0"), new BigDecimal(0), 0, "", "");
        assertFalse(result);
        verify(mockInsertTransactionExpensePS, times(1)).executeUpdate();
    }

    @Test
    public void insertTransactionExpenseSuccess() throws SQLException {
        when(mockInsertTransactionExpensePS.executeUpdate()).thenReturn(1);
        boolean result = databaseHandlers.insertTransaction("Expense", "", "", new BigDecimal(0), "", new loggedUser("0"), new BigDecimal(0), 0, "", "");
        assertTrue(result);
        verify(mockInsertTransactionExpensePS, times(1)).executeUpdate();
    }

    @Test
    public void insertTransactionIncomeFailure() throws SQLException {
        when(mockInsertTransactionIncomePS.executeUpdate()).thenReturn(0);
        boolean result = databaseHandlers.insertTransaction("Income", "", "", new BigDecimal(0), "", new loggedUser("0"), new BigDecimal(0), 0, "", "");
        assertFalse(result);
        verify(mockInsertTransactionIncomePS, times(1)).executeUpdate();
    }

    @Test
    public void insertTransactionIncomeSuccess() throws SQLException {
        when(mockInsertTransactionIncomePS.executeUpdate()).thenReturn(1);
        boolean result = databaseHandlers.insertTransaction("Income", "", "", new BigDecimal(0), "", new loggedUser("0"), new BigDecimal(0), 0, "", "");
        assertTrue(result);
        verify(mockInsertTransactionIncomePS, times(1)).executeUpdate();
    }

    @Test
    public void updateBankAccountFailure() throws SQLException {
        when(mockUpdateBankAccountExpensePS.executeUpdate()).thenReturn(0);
        boolean result = databaseHandlers.updateBankAccount("Expense", new loggedUser("0"), new BigDecimal(0));
        assertFalse(result);
        verify(mockUpdateBankAccountExpensePS, times(1)).executeUpdate();
    }

    @Test
    public void updateBankAccountSuccess() throws SQLException {
        when(mockUpdateBankAccountExpensePS.executeUpdate()).thenReturn(1);
        boolean result = databaseHandlers.updateBankAccount("Expense", new loggedUser("0"), new BigDecimal(0));
        assertTrue(result);
        verify(mockUpdateBankAccountExpensePS, times(1)).executeUpdate();
    }

    @Test
    public void getSetLoggedUserTotalBalanceFailure() throws SQLException {
        when(mockGetSetLoggedUserTotalBalancePS.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        boolean result = databaseHandlers.getSetLoggedUserTotalBalance(new loggedUser("0"));
        assertFalse(result);
        verify(mockGetSetLoggedUserTotalBalancePS, times(1)).executeQuery();
    }

    @Test
    public void getSetLoggedUserTotalBalanceSuccess() throws SQLException {
        when(mockGetSetLoggedUserTotalBalancePS.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        boolean result = databaseHandlers.getSetLoggedUserTotalBalance(new loggedUser("0"));
        assertTrue(result);
        verify(mockGetSetLoggedUserTotalBalancePS, times(1)).executeQuery();
    }

    @Test
    public void getCategoryIdByNameFailure() throws SQLException {
        when(mockGetCategoryIdByNamePS.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        int actualResult = databaseHandlers.getCategoryIdByName("testCategory");
        assertEquals(-1, actualResult);
        verify(mockGetCategoryIdByNamePS, times(1)).executeQuery();
    }

    @Test
    public void getCategoryIdByNameSuccess() throws SQLException {
        int expectedResult = 1;
        when(mockGetCategoryIdByNamePS.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("category_id")).thenReturn(1);
        int actualResult = databaseHandlers.getCategoryIdByName("testCategory");
        assertEquals(expectedResult, actualResult);
        verify(mockGetCategoryIdByNamePS, times(1)).executeQuery();
    }

    @Test
    public void retrieveCategoriesFailure() throws SQLException {
        when(mockRetrieveCategoriesPS.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        ArrayList<String> actualResult = databaseHandlers.retrieveCategories();
        assertEquals(new ArrayList<String>(), actualResult);
        verify(mockRetrieveCategoriesPS, times(1)).executeQuery();
    }

    @Test
    public void retrieveCategoriesSuccess() throws SQLException {
        ArrayList<String> expectedResult = new ArrayList<>(Arrays.asList("testCategoryName"));
        when(mockRetrieveCategoriesPS.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("name")).thenReturn("testCategoryName");
        ArrayList<String> actualResult = databaseHandlers.retrieveCategories();
        assertEquals(expectedResult, actualResult);
        verify(mockRetrieveCategoriesPS, times(1)).executeQuery();
    }
}


