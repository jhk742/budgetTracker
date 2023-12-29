import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AccountBalanceForm extends JDialog {
    private JPanel accountBalanceForm;
    private JLabel lblUserName;
    private JTable tableTransactionHistory;
    private JTextField txtTotalBalance;
    private JTextField txtTotalIncome;
    private JTextField txtTotalExpenses;


    public AccountBalanceForm(JFrame parent, loggedUser loggedU) {
        super(parent);
        setTitle("Account Balance");
        setContentPane(accountBalanceForm);
        setMinimumSize(new Dimension(1500, 800));
        setModal(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        lblUserName.setText(loggedU.name);
        txtTotalBalance.setEditable(false);
        txtTotalIncome.setEditable(false);
        txtTotalExpenses.setEditable(false);

        //temporary implementation to display total account balance
        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("select * from bank_accounts where user_id=?");
            ps.setString(1, loggedU.id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                BigDecimal balance = rs.getBigDecimal("account_balance");
                txtTotalBalance.setText(String.valueOf(balance));
            }
        } catch (Exception er) {
            System.out.println(er);
        }
    }
}
