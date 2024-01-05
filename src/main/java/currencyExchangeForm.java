import javax.swing.*;
import java.awt.*;

public class currencyExchangeForm extends JDialog {
    private JPanel currencyExchangeFormPanel;

    public currencyExchangeForm(JFrame parent) {
        super(parent);
        setTitle("Login");
        setContentPane(currencyExchangeFormPanel);
        setMinimumSize(new Dimension(1500, 800));
        setModal(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
}
