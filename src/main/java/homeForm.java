import Users.loggedUser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class homeForm extends JDialog {

    private JPanel homePanel;
    private JButton btnUsers;
    private JButton btnTransactionManagement;
    private JButton btnAccountBalance;
    private JButton btnQuit;
    private JLabel txtUserName;
    private JButton btnCategories;
    private JButton btnCharts;
    private JButton btnCurrencyExchange;

    public homeForm(JFrame parent, loggedUser loggedU) {
        super(parent);
        setTitle("Login");
        setContentPane(homePanel);
        setMinimumSize(new Dimension(1500, 800));
        setModal(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        txtUserName.setText("Welcome back, " + loggedU.name);

        if (Integer.parseInt(loggedU.id) == 1) {
            btnUsers.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                    usersForm uForm = new usersForm(null, loggedU);
                    uForm.setVisible(true);
                }
            });
        } else {
            btnUsers.setVisible(false);
        }


        btnQuit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                loginForm lf = new loginForm(null);
            }
        });

        btnAccountBalance.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                AccountBalanceForm accountBalanceForm = new AccountBalanceForm(null, loggedU);
                accountBalanceForm.setVisible(true);
            }
        });
        btnTransactionManagement.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                transactionManagementForm tmf = new transactionManagementForm(null, loggedU);
                tmf.setVisible(true);
            }
        });
        btnCategories.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                categoriesForm cf = new categoriesForm(null, loggedU, "");
                cf.setVisible(true);
            }
        });
        btnCharts.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                charts c = new charts(null, loggedU);
                c.setVisible(true);
            }
        });
        btnCurrencyExchange.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                currencyExchangeForm cef = new currencyExchangeForm(null);
                cef.setVisible(true);
            }
        });
    }
}
