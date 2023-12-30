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


    public AccountBalanceForm(JFrame parent, loggedUser loggedU) {
        super(parent);
        setTitle("Account Balance");
        setContentPane(accountBalanceForm);
        setMinimumSize(new Dimension(1500, 800));
        setModal(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        lblUserName.setText(loggedU.name);

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
                populateTableAll(tableTransactionHistory, loggedU);
            }
        });
        btnIncome.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                populateTableIncome(tableTransactionHistory, loggedU);
            }
        });
        btnExpense.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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

    public static void populateTableAll(JTable tableTransactionHistory, loggedUser loggedU) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Amount", "Type", "Running Balance", "Category ID", "Category Name", "Date"},0
        );
        tableTransactionHistory.setModel(model);
        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("SELECT\n" +
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
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString("amount"), rs.getString("type"), rs.getString("running_balance"), rs.getString("category_id"), rs.getString("name"), rs.getString("date")});
            }
        } catch (Exception er) {
            JOptionPane.showMessageDialog(null, "Error while trying to retrieve information.");
        }
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
