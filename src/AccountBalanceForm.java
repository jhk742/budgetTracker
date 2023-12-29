import javax.swing.*;
import java.awt.*;
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
}
