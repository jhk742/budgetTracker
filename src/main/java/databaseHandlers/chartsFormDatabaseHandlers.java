package databaseHandlers;
import Connectors.ConnectionProvider;
import ExceptionHandler.ExceptionHandler;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.jdbc.JDBCCategoryDataset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class chartsFormDatabaseHandlers {

    public DefaultPieDataset createPieChartDataset() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        // Connect to the database (replace with your database credentials)
        try  {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("SELECT c.name AS category, SUM(t.amount) AS total\n" +
                    "FROM transactions t\n" +
                    "INNER JOIN categories c ON t.category_id = c.category_id\n" +
                    "WHERE t.type = 'Expense'\n" +
                    "GROUP BY c.name");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String category = rs.getString("category");
                double total = rs.getDouble("total");
                dataset.setValue(category, total);
            }
        } catch (SQLException e) {
            ExceptionHandler.unableToConnectToDb(e);
        }
        return dataset;
    }

    public JDBCCategoryDataset createBarChartDataset() {
        JDBCCategoryDataset barChartDataSet = null;
        try {
            Connection con = ConnectionProvider.getCon();
            barChartDataSet = new JDBCCategoryDataset(con, "SELECT\n" +
                    " date,\n" +
                    " SUM(CASE WHEN type = 'Income' THEN amount ELSE 0 END) AS total_income,\n" +
                    " SUM(CASE WHEN type = 'Expense' THEN amount ELSE 0 END) AS total_expense\n" +
                    " FROM transactions\n" +
                    " GROUP BY date\n" +
                    " ORDER BY date");
        } catch (SQLException e) {
            ExceptionHandler.unableToConnectToDb(e);
        }
        return barChartDataSet;
    }

    public JDBCCategoryDataset createLineGraphDataSet(String option) {
        JDBCCategoryDataset lineGraphDataSet = null;
        try {
            Connection con = ConnectionProvider.getCon();
            if (option.equals("Income")) {
                lineGraphDataSet = new JDBCCategoryDataset(con, "SELECT date, SUM(amount) AS total_amount\n" +
                        "FROM transactions\n" +
                        "WHERE type = 'Income'\n" +
                        "GROUP BY date\n" +
                        "ORDER BY date");
            }
            if (option.equals("Profit/Loss")) {
                lineGraphDataSet = new JDBCCategoryDataset(con, "SELECT\n" +
                        " DATE(date) AS date,\n" +
                        " SUM(CASE WHEN type = 'Income' THEN amount ELSE 0 END) -\n" +
                        " SUM(CASE WHEN type = 'Expense' THEN amount ELSE 0 END) AS running_balance\n" +
                        " FROM transactions\n" +
                        " GROUP BY DATE(date)\n" +
                        " ORDER BY DATE(date)");
            }
            if (option.equals("ClosingBalance")) {
                lineGraphDataSet = new JDBCCategoryDataset(con, "WITH RankedTransactions AS (\n" +
                        " SELECT\n" +
                        " *,\n" +
                        " ROW_NUMBER() OVER (PARTITION BY date ORDER BY transaction_id DESC) AS row_num\n" +
                        " FROM transactions)\n" +
                        " SELECT\n" +
                        " date,\n" +
                        " running_balance\n" +
                        " FROM RankedTransactions\n" +
                        " WHERE row_num = 1");
            }
        } catch (SQLException e) {
            ExceptionHandler.unableToConnectToDb(e);
        }
        return lineGraphDataSet;
    }
}
