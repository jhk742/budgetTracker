import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class AccountBalanceForm extends JDialog {
    private JPanel accountBalanceForm;
    private JLabel lblUserName;
    private JTable tableTransactionHistory;
    private JTextField txtTotalIncome;
    private JTextField txtTotalExpenses;
    private JLabel lblTotalBalance;
    private JLabel lblTotalIncome;
    private JLabel lblTotalExpenses;
    private JButton btnViewAll;
    private JButton btnIncome;
    private JButton btnExpense;
    private JButton btnBack;
    private JComboBox comboBoxViewAllSort;
    private JComboBox comboBoxIncomeFilterByDate;
    private JComboBox comboBoxExpensesFilterByDate;

    private boolean incomeComboBoxInitialized = false;
    private boolean expenseComboBoxInitialized = false;


    public AccountBalanceForm(JFrame parent, loggedUser loggedU) {
        super(parent);
        setTitle("Account Balance");
        setContentPane(accountBalanceForm);
        setMinimumSize(new Dimension(1500, 800));
        setModal(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initializeUI(loggedU);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                getLoggedUserTotalBalance(loggedU);
                getLoggedUserTotalIncomeAndExpense(loggedU);
                lblUserName.setText(loggedU.name);
            }
        });
        btnViewAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                comboBoxViewAllSort.setSelectedIndex(0);
                toggleComboBoxes(true, false, false);
                populateTableAll(tableTransactionHistory, loggedU, "default");
            }
        });
        btnIncome.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                comboBoxIncomeFilterByDate.setSelectedIndex(0);
                toggleComboBoxes(false, true, false);
                populateTableIncome(tableTransactionHistory, loggedU, new optionAndDateFilterObject(false, 0, 0));
            }
        });
        btnExpense.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                comboBoxExpensesFilterByDate.setSelectedIndex(0);
                toggleComboBoxes(false, false, true);
                populateTableExpenses(tableTransactionHistory, loggedU, new optionAndDateFilterObject(false, 0, 0));
            }
        });
        btnBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                homeForm hf = new homeForm(null, loggedU);
                hf.setVisible(true);
            }
        });
        comboBoxViewAllSort.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedFilterOption = String.valueOf(comboBoxViewAllSort.getSelectedItem());
                if (selectedFilterOption.equals("Income") || selectedFilterOption.equals("Expenses")) {
                    populateTableAll(tableTransactionHistory, loggedU, selectedFilterOption);
                }
            }
        });
        comboBoxIncomeFilterByDate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String date = String.valueOf(comboBoxIncomeFilterByDate.getSelectedItem());
                System.out.println(date);
                if (!date.equals("--- Select Year-Month ---")) {
                    YearMonth yearMonth = YearMonth.parse(date);
                    int year = yearMonth.getYear();
                    int month = yearMonth.getMonthValue();
                    populateTableIncome(tableTransactionHistory, loggedU, new optionAndDateFilterObject(true, year, month));
                }
            }
        });
        comboBoxExpensesFilterByDate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String date = String.valueOf(comboBoxExpensesFilterByDate.getSelectedItem());
                if (!date.equals("--- Select Year-Month ---")) {
                    YearMonth yearMonth = YearMonth.parse(date);
                    int year = yearMonth.getYear();
                    int month = yearMonth.getMonthValue();
                    populateTableExpenses(tableTransactionHistory, loggedU, new optionAndDateFilterObject(true, year, month));
                }
            }
        });
    }
    private void getLoggedUserTotalBalance(loggedUser loggedU) {
        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("select * from bank_accounts where user_id=?");
            ps.setString(1, loggedU.id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                BigDecimal totalBalance = rs.getBigDecimal("account_balance");
                loggedU.totalBalance = totalBalance;
                lblTotalBalance.setText("$" + loggedU.totalBalance);
            }
        } catch (Exception er) {
            er.printStackTrace();
        }
    }

    private void getLoggedUserTotalIncomeAndExpense(loggedUser loggedU) {
        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("SELECT SUM(CASE WHEN type = 'Income' THEN amount ELSE 0 END) AS total_income, " +
                    "SUM(CASE WHEN type = 'Expense' THEN amount ELSE 0 END) AS total_expense " +
                    "FROM transactions WHERE account_id = ?");
            ps.setString(1, loggedU.id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                BigDecimal totalIncome = rs.getBigDecimal("total_income");
                BigDecimal totalExpense = rs.getBigDecimal("total_expense");
                loggedU.totalIncome = totalIncome;
                loggedU.totalExpense = totalExpense;
                lblTotalIncome.setText("$" + loggedU.totalIncome);
                lblTotalExpenses.setText("$" + loggedU.totalExpense);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void populateTableAll(JTable tableTransactionHistory, loggedUser loggedU, String filterOption) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Amount", "Type", "Running Balance", "Category ID", "Category Name", "Date"},0
        );
        tableTransactionHistory.setModel(model);
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = ConnectionProvider.getCon();
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
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("amount"),
                        rs.getString("type"),
                        rs.getString("running_balance"),
                        rs.getString("category_id"),
                        rs.getString("name"),
                        rs.getString("date")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error while trying to retrieve information.");
            e.printStackTrace();
        }
    }

    private void populateTableIncome(JTable tableTransactionHistory, loggedUser loggedU, optionAndDateFilterObject filter) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Amount", "Type", "Running Balance", "Date"},0
        );
        tableTransactionHistory.setModel(model);
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = ConnectionProvider.getCon();
            if (filter.toggleFilter == false) {
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
            if (filter.toggleFilter == true) {
                ps = con.prepareStatement("SELECT t.amount, t.type, t.running_balance, t.date\n" +
                        " FROM transactions t\n" +
                        " LEFT JOIN categories c ON t.category_id = c.category_id\n" +
                        " WHERE t.account_id = ? AND t.type = 'Income'\n" +
                        " AND YEAR(t.date) = ? AND MONTH(t.date) = ?");
                ps.setString(1, loggedU.id);
                ps.setInt(2, filter.year);
                ps.setInt(3, filter.month);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString("amount"), rs.getString("type"), rs.getString("running_balance"), rs.getString("date")});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error while trying to retrieve data.");
            e.printStackTrace();
        }
    }

    private void populateTableExpenses(JTable tableTransactionHistory, loggedUser loggedU, optionAndDateFilterObject filter) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Amount", "Type", "Running Balance", "Payment Method", "Category ID", "Category Name", "Description", "Date"},0
        );
        tableTransactionHistory.setModel(model);
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = ConnectionProvider.getCon();
            if (filter.toggleFilter == false) {
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
            };
            if (filter.toggleFilter == true) {
                ps = con.prepareStatement("SELECT t.amount, t.type, t.running_balance, t.payment_method, t.category_id, c.name, t.description, t.date\n" +
                        " FROM transactions t\n" +
                        " LEFT JOIN categories c ON t.category_id = c.category_id\n" +
                        " WHERE t.account_id = ? AND t.type = 'Expense'\n" +
                        " AND YEAR(t.date) = ? AND MONTH(t.date) = ?;");
                ps.setString(1, loggedU.id);
                ps.setInt(2, filter.year);
                ps.setInt(3, filter.month);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("amount"),
                        rs.getString("type"),
                        rs.getString("running_balance"),
                        rs.getString("payment_method"),
                        rs.getString("category_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("date")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error while trying to retrieve data.");
            e.printStackTrace();
        }
    }

    private ArrayList<String> populateDateFilterComboBox() {
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
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error while trying to retrieve data.");
            e.printStackTrace();
        }
        return dates;
    }

    private void initializeComboBoxes() {
        List<String> yearMonthOptions = populateDateFilterComboBox();
        comboBoxExpensesFilterByDate.addItem("--- Select Year-Month ---");
        comboBoxIncomeFilterByDate.addItem("--- Select Year-Month ---");
        for (String dates : yearMonthOptions) {
            comboBoxIncomeFilterByDate.addItem(dates);
            comboBoxExpensesFilterByDate.addItem(dates);
        }
        comboBoxViewAllSort.addItem("--- Filter By ---");
        comboBoxViewAllSort.addItem("Income");
        comboBoxViewAllSort.addItem("Expenses");

    }

    private void initializeViewAllFilterComboBox() {
        comboBoxViewAllSort.removeAllItems();
        comboBoxViewAllSort.addItem("--- Filter By ---");
        comboBoxViewAllSort.addItem("Income");
        comboBoxViewAllSort.addItem("Expenses");
    }

    private void toggleComboBoxes(boolean viewAll, boolean income, boolean expenses) {
        comboBoxViewAllSort.setEnabled(viewAll);
        comboBoxIncomeFilterByDate.setEnabled(income);
        comboBoxExpensesFilterByDate.setEnabled(expenses);
        if(!viewAll) {
            comboBoxViewAllSort.setSelectedIndex(0);
        }
        if(!income) {
            comboBoxIncomeFilterByDate.setSelectedIndex(0);
        }
        if(!expenses) {
            comboBoxExpensesFilterByDate.setSelectedIndex(0);
        }
    }

    private void initializeUI(loggedUser loggedU) {
        initializeComboBoxes();
        lblUserName.setText(loggedU.name);
        comboBoxViewAllSort.setEnabled(false);
        comboBoxIncomeFilterByDate.setEnabled(false);
        comboBoxExpensesFilterByDate.setEnabled(false);
    }
}

class optionAndDateFilterObject {
    public boolean toggleFilter;
    public int year;
    public int month;

    public optionAndDateFilterObject(boolean toggleFilter, int year, int month) {
        this.toggleFilter = toggleFilter;
        this.year = year;
        this.month = month;
    }
}
