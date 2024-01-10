package databaseHandlers;
import Connectors.ConnectionProvider;
import ExceptionHandler.ExceptionHandler;
import Users.loggedUser;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class accountBalanceFormDatabaseHandlers {

    public void getLoggedUserTotalBalance(loggedUser loggedU) {
        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("select * from bank_accounts where user_id=?");
            ps.setString(1, loggedU.id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                loggedU.totalBalance = rs.getBigDecimal("account_balance");
            }
        } catch (SQLException e) {
            ExceptionHandler.unableToConnectToDb(e);
        }
    }

    public void getLoggedUserTotalIncomeAndExpense(loggedUser loggedU) {
        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("SELECT SUM(CASE WHEN type = 'Income' THEN amount ELSE 0 END) AS total_income, " +
                    "SUM(CASE WHEN type = 'Expense' THEN amount ELSE 0 END) AS total_expense " +
                    "FROM transactions WHERE account_id = ?");
            ps.setString(1, loggedU.id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                loggedU.totalIncome = rs.getBigDecimal("total_income");
                loggedU.totalExpense = rs.getBigDecimal("total_expense");
            }
        } catch (SQLException e) {
            ExceptionHandler.unableToConnectToDb(e);
        }
    }

    public List<List<String>> retrieveDataForAll(loggedUser loggedU, String filterOption, String startDate, String endDate) {
        List<List<String>> retData = new ArrayList<>();
        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = null;
            if (filterOption.equals("default")) {
                ps = con.prepareStatement("SELECT\n" +
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
                        "WHERE t.account_id = ? AND (t.type = 'Expense' OR t.category_id IS NULL)");
                ps.setString(1, loggedU.id);
            }
            if (filterOption.equals("Income")) {
                ps = con.prepareStatement("SELECT\n" +
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
                        "END");
                ps.setString(1, loggedU.id);
            }
            if (filterOption.equals("Expenses")) {
                ps = con.prepareStatement("SELECT\n" +
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
                        "END");
                ps.setString(1, loggedU.id);
            }
            if (filterOption.equals("DateRange")) {
                ps = con.prepareStatement("SELECT\n" +
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
                        "WHERE t.account_id = ? AND (t.type='Expense' OR t.category_id IS NULL) AND t.date BETWEEN ? AND ?");
                ps.setString(1, loggedU.id);
                ps.setString(2, startDate);
                ps.setString(3, endDate);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                List<String> rowData = new ArrayList<>();
                rowData.add(rs.getString("amount"));
                rowData.add(rs.getString("type"));
                rowData.add(rs.getString("running_balance"));
                rowData.add(rs.getString("category_id"));
                rowData.add(rs.getString("name"));
                rowData.add(rs.getString("date"));
                retData.add(rowData);
            }
        } catch (SQLException e) {
            ExceptionHandler.unableToConnectToDb(e);
        }
        return retData;
    }

    public List<List<String>> retrieveDataForIncome(loggedUser loggedU, boolean byYearMonth, int year, int month, boolean dateRange, String startDate, String endDate) {
        List<List<String>> retData = new ArrayList<>();
        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = null;
            if (!byYearMonth) {
                ps = con.prepareStatement("SELECT\n" +
                        "   amount,\n" +
                        "   type,\n" +
                        "   running_balance,\n" +
                        "   date\n" +
                        "FROM\n" +
                        "   transactions\n" +
                        "WHERE type = 'Income' AND account_id = ?");
                ps.setString(1, loggedU.id);
            }
            if (byYearMonth) {
                ps = con.prepareStatement("SELECT t.amount, t.type, t.running_balance, t.date\n" +
                        " FROM transactions t\n" +
                        " LEFT JOIN categories c ON t.category_id = c.category_id\n" +
                        " WHERE t.account_id = ? AND t.type = 'Income'\n" +
                        " AND YEAR(t.date) = ? AND MONTH(t.date) = ?");
                ps.setString(1, loggedU.id);
                ps.setInt(2, year);
                ps.setInt(3, month);
            }
            if (dateRange) {
                ps = con.prepareStatement("SELECT amount, type, running_balance, date " +
                        "FROM transactions\n" +
                        "WHERE\n" +
                        "type = 'Income' AND account_id = ? AND date BETWEEN ? AND ?");
                ps.setString(1, loggedU.id);
                ps.setString(2, startDate);
                ps.setString(3, endDate);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                List<String> rowData = new ArrayList<>();
                rowData.add(rs.getString("amount"));
                rowData.add(rs.getString("type"));
                rowData.add(rs.getString("running_balance"));
                rowData.add(rs.getString("date"));
                retData.add(rowData);
            }
        } catch (SQLException e) {
            ExceptionHandler.unableToConnectToDb(e);
        }
        return retData;
    }

    public List<List<String>> retrieveDataForExpense(loggedUser loggedU, boolean byYearMonth, int year, int month, boolean dateRange, String startDate, String endDate) {
        List<List<String>> retData = new ArrayList<>();
        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = null;
            if (!byYearMonth) {
                ps = con.prepareStatement("SELECT\n" +
                        "    t.amount,\n" +
                        "    t.type,\n" +
                        "    t.running_balance,\n" +
                        "    t.payment_method,\n" +
                        "    t.category_id,\n" +
                        "    c.name,\n" +
                        "    t.description,\n" +
                        "    t.date\n" +
                        "FROM\n" +
                        "    transactions t\n" +
                        "INNER JOIN\n" +
                        "    categories c ON t.category_id = c.category_id\n" +
                        "WHERE\n" +
                        "    t.type = 'Expense'\n" +
                        "    AND t.account_id = ?");
                ps.setString(1, loggedU.id);
            }
            if (byYearMonth) {
                ps = con.prepareStatement("SELECT t.amount, t.type, t.running_balance, t.payment_method, t.category_id, c.name, t.description, t.date\n" +
                        " FROM transactions t\n" +
                        " LEFT JOIN categories c ON t.category_id = c.category_id\n" +
                        " WHERE t.account_id = ? AND t.type = 'Expense'\n" +
                        " AND YEAR(t.date) = ? AND MONTH(t.date) = ?");
                ps.setString(1, loggedU.id);
                ps.setInt(2, year);
                ps.setInt(3, month);
            }
            if (dateRange) {
                ps = con.prepareStatement("SELECT t.amount, t.type, t.running_balance, t.payment_method, t.category_id, c.name, t.description, t.date\n" +
                        "FROM transactions t\n" +
                        "INNER JOIN categories c ON t.category_id = c.category_id\n" +
                        "WHERE t.type = 'Expense'\n" +
                        "AND t.account_id = ? AND t.date BETWEEN ? AND ?");
                ps.setString(1, loggedU.id);
                ps.setString(2, startDate);
                ps.setString(3, endDate);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                List<String> rowData = new ArrayList<>();
                rowData.add(rs.getString("amount"));
                rowData.add(rs.getString("type"));
                rowData.add(rs.getString("running_balance"));
                rowData.add(rs.getString("payment_method"));
                rowData.add(rs.getString("category_id"));
                rowData.add(rs.getString("name"));
                rowData.add(rs.getString("description"));
                rowData.add(rs.getString("date"));
                retData.add(rowData);
            }
        } catch (SQLException e) {
            ExceptionHandler.unableToConnectToDb(e);
        }
        return retData;
    }

    public ArrayList<String> populateDateFilterComboBox() {
        ArrayList<String> dates = new ArrayList<>();
        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("SELECT DISTINCT " +
                    "CONCAT(YEAR(date), '-', LPAD(MONTH(date), 2, '0')) AS 'year_month' " +
                    "FROM transactions " +
                    "ORDER BY 'year_month' DESC");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                dates.add(rs.getString("year_month"));
            }
        } catch (SQLException e) {
            ExceptionHandler.unableToConnectToDb(e);
        }
        return dates;
    }
}
