import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;

public class AccountBalanceForm extends JDialog {
    private JPanel accountBalanceForm;
    private JLabel lblUserName;
    private JTable tableTransactionHistory;
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
    private JButton expenseDateRangeConfirmBtn;
    private JButton incomeDateRangeConfirmBtn;
    private JButton totalDateRangeConfirmBtn;
    private JPanel totalDateRangePanel;
    private JTextField txtTotalStartDate;
    private JTextField txtTotalEndDate;
    private JTextField txtIncomeStartDate;
    private JTextField txtIncomeEndDate;
    private JTextField txtExpenseStartDate;
    private JTextField txtExpenseEndDate;

    private String startDate;
    private String endDate;

    private Map<String, JButton> dateRangeButtonMap = new HashMap<String, JButton>() {{
        put("totalDateRangeConfirmBtn", totalDateRangeConfirmBtn);
        put("incomeDateRangeConfirmBtn", incomeDateRangeConfirmBtn);
        put("expenseDateRangeConfirmBtn", expenseDateRangeConfirmBtn);
    }};
    private ArrayList<JTextField> dateRangeTxtFields = new ArrayList<>(
            Arrays.asList(
                    txtTotalStartDate,
                    txtTotalEndDate,
                    txtIncomeStartDate,
                    txtIncomeEndDate,
                    txtExpenseStartDate,
                    txtExpenseEndDate
            )
    );

    private Map<String, JTextField> dateRangeTxtFieldMap = new HashMap<String, JTextField>() {{
        put("txtTotalStartDate", txtTotalStartDate);
        put("txtTotalEndDate", txtTotalEndDate);
        put("txtIncomeStartDate", txtIncomeStartDate);
        put("txtIncomeEndDate", txtIncomeEndDate);
        put("txtExpenseStartDate", txtExpenseStartDate);
        put("txtExpenseEndDate", txtExpenseEndDate);
    }};

    public AccountBalanceForm(JFrame parent, loggedUser loggedU) {
        super(parent);
        setTitle("Account Balance");
        setContentPane(accountBalanceForm);
        setMinimumSize(new Dimension(1500, 1000));
        setModal(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);


        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                getLoggedUserTotalBalance(loggedU);
                getLoggedUserTotalIncomeAndExpense(loggedU);
                lblUserName.setText(loggedU.name);
                initializeUI(loggedU);
            }
        });

        btnViewAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleSpecificDateRangesAndButtons(
                    new HashMap<String, JTextField>() {{
                        put("txtTotalStartDate", txtTotalStartDate);
                        put("txtTotalEndDate", txtTotalEndDate);
                    }},
                    new HashMap<String, JButton>() {{
                        put("totalDateRangeConfirmBtn", totalDateRangeConfirmBtn);
                    }}
                );
                comboBoxViewAllSort.setSelectedIndex(0);
                toggleComboBoxes(true, false, false);
                populateTableAll(tableTransactionHistory, loggedU, "default");
            }
        });

        btnIncome.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleSpecificDateRangesAndButtons(
                    new HashMap<String, JTextField>() {{
                        put("txtIncomeStartDate", txtIncomeStartDate);
                        put("txtIncomeEndDate", txtIncomeEndDate);
                }},
                    new HashMap<String, JButton>() {{
                        put("incomeDateRangeConfirmBtn", incomeDateRangeConfirmBtn);
                    }}
                );
                comboBoxIncomeFilterByDate.setSelectedIndex(0);
                toggleComboBoxes(false, true, false);
                populateTableIncome(tableTransactionHistory, loggedU, new optionAndDateFilterObject(false, 0, 0), false);
            }
        });

        btnExpense.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleSpecificDateRangesAndButtons(
                    new HashMap<String, JTextField>() {{
                        put("txtExpenseStartDate", txtExpenseStartDate);
                        put("txtExpenseEndDate", txtExpenseEndDate);
                }},
                    new HashMap<String, JButton>() {{
                        put("expenseDateRangeConfirmBtn", expenseDateRangeConfirmBtn);
                    }}
                );
                comboBoxExpensesFilterByDate.setSelectedIndex(0);
                toggleComboBoxes(false, false, true);
                populateTableExpenses(tableTransactionHistory, loggedU, new optionAndDateFilterObject(false, 0, 0), false);
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
                if (!date.equals("--- Select Year-Month ---")) {
                    YearMonth yearMonth = YearMonth.parse(date);
                    int year = yearMonth.getYear();
                    int month = yearMonth.getMonthValue();
                    populateTableIncome(tableTransactionHistory, loggedU, new optionAndDateFilterObject(true, year, month), false);
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
                    populateTableExpenses(tableTransactionHistory, loggedU, new optionAndDateFilterObject(true, year, month), false);
                }
            }
        });

        txtTotalStartDate.addFocusListener(new FocusAdapter() {
            String startDate = "Enter Start Date: yyyy-mm-dd";
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                if (txtTotalStartDate.getText().equals(startDate)) {
                    txtTotalStartDate.setText("");
                    txtTotalStartDate.setForeground(Color.BLACK);
                }
            }

            public void focusLost(FocusEvent e) {
                if (txtTotalStartDate.getText().isEmpty()) {
                    txtTotalStartDate.setText(startDate);
                    txtTotalStartDate.setForeground(Color.GRAY);
                }
            }
        });

        totalDateRangeConfirmBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startDate = txtTotalStartDate.getText();
                endDate = txtTotalEndDate.getText();
                boolean validation = authenticateDates(startDate, endDate, new ArrayList<JTextField>(Arrays.asList(txtTotalStartDate, txtTotalEndDate)));
                if (validation) {
                    populateTableAll(tableTransactionHistory, loggedU, "DateRange");
                }
            }
        });
        incomeDateRangeConfirmBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startDate = txtIncomeStartDate.getText();
                endDate = txtIncomeEndDate.getText();
                boolean validation = authenticateDates(startDate, endDate, new ArrayList<JTextField>(Arrays.asList(txtIncomeStartDate, txtIncomeEndDate)));
                if (validation) {
                    populateTableIncome(tableTransactionHistory, loggedU, new optionAndDateFilterObject(false, 0, 0), true);
                }
            }
        });
        expenseDateRangeConfirmBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startDate = txtExpenseStartDate.getText();
                endDate = txtExpenseEndDate.getText();
                boolean validation = authenticateDates(startDate, endDate, new ArrayList<JTextField>(Arrays.asList(txtExpenseStartDate, txtExpenseEndDate)));
                if (validation) {
                    populateTableExpenses(tableTransactionHistory, loggedU, new optionAndDateFilterObject(false, 0, 0), true);
                }
            }
        });
    }

    private boolean authenticateDates(String startDate, String endDate, ArrayList<JTextField> textFields) {
        //no need to check with .isEmpty() as textFields that are empty will revert back to the placeholder upon losing focus.
        if (!startDate.equals("Enter Start Date: yyyy-mm-dd") || !endDate.equals("Enter End Date: yyyy-mm-dd")) {
            try {
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                if (end.isBefore(start)) {
                    JOptionPane.showMessageDialog(null, "Error: The start date cannot be after the end date. Please select a valid date range.");
                    //revert text back to placeholder
                    toggleDateRangePlaceHolder();
                    return false;
                }
                if (start.isAfter(LocalDate.now())) {
                    JOptionPane.showMessageDialog(null, "Error: The start date cannot be after the current date. Please select a valid date range.");
                    toggleDateRangePlaceHolder();
                    return false;
                }
                return true;
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(null, "Error: Invalid date format. Please enter dates in the format yyyy-mm-dd.");
                toggleDateRangePlaceHolder();
                return false;
            }
        }
        JOptionPane.showMessageDialog(null, "Please provide values for \"Start Date\" and \"End Date\".");
        return false;
    }

    private void toggleSpecificDateRangesAndButtons(Map<String, JTextField> dateTxtField, Map<String, JButton> dateBtn) {
        String startDate = "Enter Start Date: yyyy-mm-dd";
        String endDate = "Enter End Date: yyyy-mm-dd";
        for (Map.Entry<String, JTextField> entry : dateRangeTxtFieldMap.entrySet()) {
            String targetKey = entry.getKey();
            JTextField targetValue = entry.getValue();

            if (!dateTxtField.containsKey(targetKey)) {
                targetValue.setEnabled(false);
                if (targetKey.contains("Start")) {
                    targetValue.setText(startDate);
                    targetValue.setForeground(Color.GRAY);
                } else if (entry.getKey().contains("End")) {
                    targetValue.setText(endDate);
                    targetValue.setForeground(Color.GRAY);
                }
            } else {
                targetValue.setEnabled(true);
            }
        }
        for (Map.Entry<String, JButton> entry : dateRangeButtonMap.entrySet()) {
            String targetKey = entry.getKey();
            JButton targetButton = entry.getValue();
            if (!dateBtn.containsKey(targetKey)) {
                targetButton.setEnabled(false);
            } else {
                targetButton.setEnabled(true);
            }
        }
    }

    private void dateRangeTxtFieldAddFocusListener(JTextField txtField, String startOrEnd) {
        String startDate = "Enter Start Date: yyyy-mm-dd";
        String endDate = "Enter End Date: yyyy-mm-dd";
        txtField.setText(startOrEnd.equals("start") ? startDate : endDate);
        txtField.setForeground(Color.GRAY);
        txtField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtField.getText().equals(startDate) || txtField.getText().equals(endDate)) {
                    txtField.setText("");
                    txtField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent e) {
                if (txtField.getText().isEmpty()) {
                    txtField.setText(startOrEnd.equals("start") ? startDate : endDate);
                    txtField.setForeground(Color.GRAY);
                }
            }
        });
    }

    private void toggleDateRangePlaceHolder() {
        for (int i = 0; i < dateRangeTxtFields.size(); i++) {
            JTextField targetTxtfield = dateRangeTxtFields.get(i);
            if (i % 2 == 0) {
                dateRangeTxtFieldAddFocusListener(targetTxtfield, "start");
            } else {
                dateRangeTxtFieldAddFocusListener(targetTxtfield, "end");
            }
        }
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
                        "WHERE t.account_id = ? AND (t.type='Expense' OR t.category_id IS NULL) AND t.date IN (?, ?)");
                ps.setString(1, loggedU.id);
                ps.setString(2, startDate);
                ps.setString(3, endDate);
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

    private void populateTableIncome(JTable tableTransactionHistory, loggedUser loggedU, optionAndDateFilterObject filter, boolean dateRange) {
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
            if (dateRange) {
                System.out.println(startDate + " " + endDate);
                ps = con.prepareStatement("SELECT amount, type, running_balance, date " +
                        "FROM transactions\n" +
                        "WHERE\n" +
                        "type = 'Income' AND account_id = ? AND date IN (?, ?)");
                ps.setString(1, loggedU.id);
                ps.setString(2, startDate);
                ps.setString(3, endDate);
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

    private void populateTableExpenses(JTable tableTransactionHistory, loggedUser loggedU, optionAndDateFilterObject filter, boolean dateRange) {
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
                        " AND YEAR(t.date) = ? AND MONTH(t.date) = ?");
                ps.setString(1, loggedU.id);
                ps.setInt(2, filter.year);
                ps.setInt(3, filter.month);
            }
            if (dateRange) {
                System.out.println(startDate + " " + endDate);
                ps = con.prepareStatement("SELECT t.amount, t.type, t.running_balance, t.payment_method, t.category_id, c.name, t.description, t.date\n" +
                        "FROM transactions t\n" +
                        "INNER JOIN categories c ON t.category_id = c.category_id\n" +
                        "WHERE t.type = 'Expense'\n" +
                        "AND t.account_id = ? AND t.date IN (?, ?)");
                ps.setString(1, loggedU.id);
                ps.setString(2, startDate);
                ps.setString(3, endDate);
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
        toggleDateRangePlaceHolder(/*dateRangeTxtFields*/);
        for (JTextField dates : dateRangeTxtFields) {
            dates.setEnabled(false);
        }
        totalDateRangeConfirmBtn.setEnabled(false);
        incomeDateRangeConfirmBtn.setEnabled(false);
        expenseDateRangeConfirmBtn.setEnabled(false);
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
