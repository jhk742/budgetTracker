package gui;
import Users.loggedUser;
import ExceptionHandler.ExceptionHandler;
import databaseHandlers.accountBalanceFormDatabaseHandlers;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;

public class AccountBalanceForm extends JDialog {
    private JPanel accountBalanceForm;
    private JPanel totalDateRangePanel;
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
    private JTextField txtTotalStartDate;
    private JTextField txtTotalEndDate;
    private JTextField txtIncomeStartDate;
    private JTextField txtIncomeEndDate;
    private JTextField txtExpenseStartDate;
    private JTextField txtExpenseEndDate;
    private accountBalanceFormDatabaseHandlers dbHandler = new accountBalanceFormDatabaseHandlers();
    private String startDate;
    private String endDate;
    private List<List<String>> tableAllData;
    private List<List<String>> tableIncomeData;
    private List<List<String>> tableExpenseData;

    private final Map<String, JButton> dateRangeButtonMap = new HashMap<String, JButton>() {{
        put("totalDateRangeConfirmBtn", totalDateRangeConfirmBtn);
        put("incomeDateRangeConfirmBtn", incomeDateRangeConfirmBtn);
        put("expenseDateRangeConfirmBtn", expenseDateRangeConfirmBtn);
    }};
    private final ArrayList<JTextField> dateRangeTxtFields = new ArrayList<>(
            Arrays.asList(
                txtTotalStartDate,
                txtTotalEndDate,
                txtIncomeStartDate,
                txtIncomeEndDate,
                txtExpenseStartDate,
                txtExpenseEndDate
            )
    );

    private final Map<String, JTextField> dateRangeTxtFieldMap = new HashMap<String, JTextField>() {{
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
                dbHandler.getSetLoggedUserTotalBalance(loggedU);
                lblTotalBalance.setText("$" + loggedU.totalBalance);
                dbHandler.getSetLoggedUserTotalIncomeAndExpense(loggedU);
                lblTotalIncome.setText("$" + loggedU.totalIncome);
                lblTotalExpenses.setText("$" + loggedU.totalExpense);
                lblUserName.setText(loggedU.name);
                initializeUI(loggedU);
            }
        });

        btnViewAll.addActionListener(e -> {
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
            tableAllData = dbHandler.retrieveDataForAll(loggedU, "default", "", "");
            populateTable("All", tableAllData);
        });

        btnIncome.addActionListener(e -> {
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
            tableIncomeData = dbHandler.retrieveDataForIncome(loggedU, false, 0, 0, false, "", "");
            populateTable("Income", tableIncomeData);
        });

        btnExpense.addActionListener(e -> {
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
            tableExpenseData = dbHandler.retrieveDataForExpense(loggedU, false, 0, 0, false, "", "");
            populateTable("Expense", tableExpenseData);
        });

        btnBack.addActionListener(e -> {
            dispose();
            homeForm hf = new homeForm(null, loggedU);
            hf.setVisible(true);
        });

        comboBoxViewAllSort.addActionListener(e -> {
            String selectedFilterOption = String.valueOf(comboBoxViewAllSort.getSelectedItem());
            if (selectedFilterOption.equals("Income") || selectedFilterOption.equals("Expenses")) {
                tableAllData = dbHandler.retrieveDataForAll(loggedU, selectedFilterOption, "", "");
                populateTable("All", tableAllData);
            }
        });

        comboBoxIncomeFilterByDate.addActionListener(e -> {
            String date = String.valueOf(comboBoxIncomeFilterByDate.getSelectedItem());
            if (!date.equals("--- Select Year-Month ---")) {
                YearMonth yearMonth = YearMonth.parse(date);
                int year = yearMonth.getYear();
                int month = yearMonth.getMonthValue();
                tableIncomeData = dbHandler.retrieveDataForIncome(loggedU, true, year, month, false, "", "");
                populateTable("Income", tableIncomeData);
            }
        });

        comboBoxExpensesFilterByDate.addActionListener(e -> {
            String date = String.valueOf(comboBoxExpensesFilterByDate.getSelectedItem());
            if (!date.equals("--- Select Year-Month ---")) {
                YearMonth yearMonth = YearMonth.parse(date);
                int year = yearMonth.getYear();
                int month = yearMonth.getMonthValue();
                tableExpenseData = dbHandler.retrieveDataForExpense(loggedU, true, year, month, false, "", "");
                populateTable("Expense", tableExpenseData);
            }
        });

        totalDateRangeConfirmBtn.addActionListener(e -> {
            startDate = txtTotalStartDate.getText();
            endDate = txtTotalEndDate.getText();
            boolean validation = authenticateDates(startDate, endDate);
            if (validation) {
                tableAllData = dbHandler.retrieveDataForAll(loggedU, "DateRange", startDate, endDate);
                populateTable("All", tableAllData);
            }
        });

        incomeDateRangeConfirmBtn.addActionListener(e -> {
            startDate = txtIncomeStartDate.getText();
            endDate = txtIncomeEndDate.getText();
            boolean validation = authenticateDates(startDate, endDate);
            if (validation) {
                tableIncomeData = dbHandler.retrieveDataForIncome(loggedU, false, 0, 0, true, startDate, endDate);
                populateTable("Income", tableIncomeData);
            }
        });

        expenseDateRangeConfirmBtn.addActionListener(e -> {
            startDate = txtExpenseStartDate.getText();
            endDate = txtExpenseEndDate.getText();
            boolean validation = authenticateDates(startDate, endDate);
            if (validation) {
                tableExpenseData = dbHandler.retrieveDataForExpense(loggedU, false, 0, 0, true, startDate, endDate);
                populateTable("Expense", tableExpenseData);
            }
        });
    }

    private boolean authenticateDates(String startDate, String endDate) {
        //no need to check with .isEmpty() as textFields that are empty will revert back to the placeholder upon losing focus.
        if (!startDate.equals("Enter Start Date: yyyy-mm-dd") || !endDate.equals("Enter End Date: yyyy-mm-dd")) {
            try {
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                if (end.isBefore(start)) {
                    throw new Exception("Error: The start date cannot be after the end date. Please select a valid date range.");
                }
                if (start.isAfter(LocalDate.now())) {
                    throw new Exception("Error: The start date cannot be after the current date. Please select a valid date range.");
                }
                return true;
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(null, "Error: Invalid date format. Please enter dates in the format yyyy-mm-dd.");
                toggleDateRangePlaceHolder();
                return false;
            } catch (Exception e) {
                ExceptionHandler.invalidDates(e.getMessage());
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
            entry.getValue().setEnabled(dateBtn.containsKey(entry.getKey()));
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
                setComboBoxEnabledFalse();
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

    private void setComboBoxEnabledFalse() {
        comboBoxViewAllSort.setEnabled(false);
        comboBoxIncomeFilterByDate.setEnabled(false);
        comboBoxExpensesFilterByDate.setEnabled(false);
    }

    private void populateTable(String option, List<List<String>> data) {
        DefaultTableModel model = null;
        if (option.equals("All")) {
            model = new DefaultTableModel(
                    new Object[]{"Amount", "Type", "Running Balance", "Category ID", "Category Name", "Date"},0
            );
        }
        if (option.equals("Income")) {
            model = new DefaultTableModel(
                    new Object[]{"Amount", "Type", "Running Balance", "Date"},0
            );
        }
        if (option.equals("Expense")) {
            model = new DefaultTableModel(
                    new Object[]{"Amount", "Type", "Running Balance", "Payment Method", "Category ID", "Category Name", "Description", "Date"},0
            );
        }
        assert model != null;
        tableTransactionHistory.setModel(model);
        for (List<String> rowData : data) {
            model.addRow(rowData.toArray());
        }
    }

    private void initializeComboBoxes() {
        List<String> yearMonthOptions = dbHandler.retrieveYearMonth();
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
        toggleDateRangePlaceHolder();
        for (JTextField dates : dateRangeTxtFields) {
            dates.setEnabled(false);
        }
        totalDateRangeConfirmBtn.setEnabled(false);
        incomeDateRangeConfirmBtn.setEnabled(false);
        expenseDateRangeConfirmBtn.setEnabled(false);
    }
}