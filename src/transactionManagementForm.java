import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class transactionManagementForm extends JDialog {
    private JPanel transactionManagementForm;
    private JTextField textField1;
    private JTextField textField2;
    private JComboBox comboBox1;
    private JComboBox comboBox2;
    private JTextField textField3;
    private JComboBox comboBox3;
    private JTextField textField4;
    private JButton btnSubmit;
    private JButton btnBack;
    private JButton btnReset;
    private JLabel lblTotalBalance;

    public transactionManagementForm(JFrame parent, loggedUser loggedU) {
        super(parent);
        setTitle("Transaction Management");
        setContentPane(transactionManagementForm);
        setMinimumSize(new Dimension(1500, 800));
        setModal(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getLoggedUserTotalBalance(loggedU);

    }

    public void getLoggedUserTotalBalance(loggedUser loggedU) {
        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("select * from bank_accounts where user_id=?");
            ps.setString(1, loggedU.id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                BigDecimal balance = rs.getBigDecimal("account_balance");
                loggedU.totalBalance = balance;
                lblTotalBalance.setText(String.valueOf(loggedU.totalBalance));
            }
        } catch (Exception er) {
            System.out.println(er);
        }
    }
}
