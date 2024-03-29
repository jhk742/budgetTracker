package gui;
import ExceptionHandler.Exceptions.DatabaseConnectionException;
import Users.loggedUser;
import ExceptionHandler.ExceptionHandler;
import databaseHandlers.loginFormDatabaseHandlers;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class loginForm extends JDialog {
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JButton btnSubmit;
    private JPanel loginPanel;
    private JButton btnExit;

    private loginFormDatabaseHandlers dbHandler = new loginFormDatabaseHandlers();

    public loginForm(JFrame parent) {
        super(parent);
        setTitle("Login");
        setContentPane(loginPanel);
        setMinimumSize(new Dimension(450, 474));
        setModal(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        getRootPane().setDefaultButton(btnSubmit);
        btnSubmit.addActionListener(e -> {
            try {
                String email = txtEmail.getText();
                String password = String.valueOf(txtPassword.getPassword());
                loggedUser user = dbHandler.authenticateUser(email, password); //!!!!!!

                // exists and is active
                if (user.status == 1) {
                    // open up homepage for budget-tracking app
                    homeForm hForm = new homeForm(null, user);
                    setVisible(false);
                    hForm.setVisible(true);
                }
                //exists but is inactive (ask if user wants to re-activate account)
                if (user.status == 0) {
                    int result = JOptionPane.showConfirmDialog(null,
                            "Your account has been temporarily deactivated. Would you like to reactivate it now?",
                            "Confirmation",
                            JOptionPane.YES_NO_OPTION
                    );
                    //yes, reactivate
                    if (result == 0) {
                        boolean reactivated = dbHandler.reactivateUser(email, password); //!!!!
                        if (reactivated) {
                            JOptionPane.showMessageDialog(null, "Your account has been reactivated. Please try logging in again.");
                        }
                    }
                }
                //does not exist
                if (user.status == -1) {
                    throw new RuntimeException("This account does not exist.");
                }
            } catch (DatabaseConnectionException dex) {
                SQLException originalException = (SQLException) dex.getCause();
                ExceptionHandler.unableToConnectToDb(originalException);
            } catch (RuntimeException ex) {
                ExceptionHandler.userDoesNotExist(ex);
            }
        });

        btnExit.addActionListener(e -> {
            dispose();
            System.exit(0);
        });
        setVisible(true);
    }
}
