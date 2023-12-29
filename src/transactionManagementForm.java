import javax.swing.*;
import java.awt.*;

public class transactionManagementForm extends JDialog {
    private JPanel transactionManagementForm;

    public transactionManagementForm(JFrame parent, loggedUser loggedU) {
        super(parent);
        setTitle("Login");
        setContentPane(transactionManagementForm);
        setMinimumSize(new Dimension(1500, 800));
        setModal(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    }
}
