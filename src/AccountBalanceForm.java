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


    public AccountBalanceForm(JFrame parent, loggedUser loggedU) {
        super(parent);
        setTitle("Account Balance");
        setContentPane(accountBalanceForm);
        setMinimumSize(new Dimension(1500, 800));
        setModal(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        lblUserName.setText(loggedU.name);
        comboBoxViewAllSort.setVisible(false);


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
                comboBoxViewAllSort.setVisible(true);
                //clear previous items so there are no duplicates when btnViewAll is selected again
                comboBoxViewAllSort.removeAllItems();

                comboBoxViewAllSort.addItem("--- Filter By ---");
                comboBoxViewAllSort.addItem("Income");
                comboBoxViewAllSort.addItem("Expenses");
                populateTableAll(tableTransactionHistory, loggedU, "default");
            }
        });
        btnIncome.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                comboBoxViewAllSort.setVisible(false);
                populateTableIncome(tableTransactionHistory, loggedU);
            }
        });
        btnExpense.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                comboBoxViewAllSort.setVisible(false);
                populateTableExpenses(tableTransactionHistory, loggedU);
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
    }
    public void getLoggedUserTotalBalance(loggedUser loggedU) {
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
            System.out.println(er);
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
                BigDecimal totalIncome = rs.getBigDecimal("total_income");
                BigDecimal totalExpense = rs.getBigDecimal("total_expense");
                loggedU.totalIncome = totalIncome;
                loggedU.totalExpense = totalExpense;
                lblTotalIncome.setText("$" + loggedU.totalIncome);
                lblTotalExpenses.setText("$" + loggedU.totalExpense);

            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static void populateTableAll(JTable tableTransactionHistory, loggedUser loggedU, String filterOption) {
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
//        if (filterOption.equals("default")) {
//            try {
//                Connection con = ConnectionProvider.getCon();
//                PreparedStatement ps = con.prepareStatement("SELECT\n" +
//                        "    t.amount,\n" +
//                        "    t.type,\n" +
//                        "    t.running_balance,\n" +
//                        "    t.category_id,\n" +
//                        "    c.name,\n" +
//                        "    t.date\n" +
//                        "FROM\n" +
//                        "    transactions t\n" +
//                        "LEFT JOIN\n" +
//                        "    categories c ON t.category_id = c.category_id\n" +
//                        "WHERE t.account_id = ? AND (t.type = 'Expense' OR t.category_id IS NULL)");
//                ps.setString(1, loggedU.id);
//                ResultSet rs = ps.executeQuery();
//                while (rs.next()) {
//                    model.addRow(new Object[]{rs.getString("amount"), rs.getString("type"), rs.getString("running_balance"), rs.getString("category_id"), rs.getString("name"), rs.getString("date")});
//                }
//            } catch (Exception er) {
//                JOptionPane.showMessageDialog(null, "Error while trying to retrieve information.");
//            }
//        }
//        if (filterOption.equals("Income")) {
//            try {
//                Connection con = ConnectionProvider.getCon();
//                PreparedStatement ps;
//            } catch (Exception er2) {
//
//            }
//        }
//        if (filterOption.equals("Expenses")) {
//            try {
//                Connection con = ConnectionProvider.getCon();
//                PreparedStatement ps;
//            } catch (Exception er2) {
//
//            }
//        }
    }

    public static void populateTableIncome(JTable tableTransactionHistory, loggedUser loggedU) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Amount", "Type", "Running Balance", "Date"},0
        );
        tableTransactionHistory.setModel(model);
        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("SELECT\n" +
                    "   amount,\n" +
                    "   type,\n" +
                    "   running_balance,\n" +
                    "   date\n" +
                    "FROM\n" +
                    "   transactions\n" +
                    "WHERE type = 'Income' AND account_id = ?");
            ps.setString(1, loggedU.id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString("amount"), rs.getString("type"), rs.getString("running_balance"), rs.getString("date")});
            }
        } catch (Exception er) {
            JOptionPane.showMessageDialog(null, "Error while trying to retrieve information.");
        }
    }

    public static void populateTableExpenses(JTable tableTransactionHistory, loggedUser loggedU) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Amount", "Type", "Running Balance", "Payment Method", "Category ID", "Category Name", "Description", "Date"},0
        );
        tableTransactionHistory.setModel(model);
        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("SELECT\n" +
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
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString("amount"), rs.getString("type"), rs.getString("running_balance"), rs.getString("payment_method"), rs.getString("category_id"), rs.getString("name"), rs.getString("description"), rs.getString("date")});
            }
        } catch (Exception er) {
            JOptionPane.showMessageDialog(null, "Error while trying to retrieve information.");
        }
    }
}
