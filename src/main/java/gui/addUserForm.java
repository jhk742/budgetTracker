package gui;
import Users.loggedUser;
import databaseHandlers.addUserFormDatabaseHandlers;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class addUserForm extends JDialog {
    private JTextField txtName;
    private JTextField txtPassword;
    private JTextField txtAddress;
    private JTextField txtPhone;
    private JComboBox comboBoxStatus;
    private JButton btnAdd;
    private JButton btnCancel;
    private JPanel addUserForm;
    private JTextField txtEmail;
    private JTextField txtBalance;

    private usersForm parentForm;

    private addUserFormDatabaseHandlers dbHandler = new addUserFormDatabaseHandlers();

    public addUserForm(JDialog parent, loggedUser loggedU) {
        super(parent);
        setTitle("Login");
        setContentPane(addUserForm);
        setMinimumSize(new Dimension(300, 300));
        setModal(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        //storing the reference to "parentForm" to use onDataChange();
        parentForm = (usersForm) parent;
        comboBoxStatus.addItem("");
        comboBoxStatus.addItem("Active");
        comboBoxStatus.addItem("Inactive");

        btnAdd.addActionListener(e -> {
            String name = txtName.getText();
            String email = txtEmail.getText();
            String phone = txtPhone.getText();
            String address = txtAddress.getText();
            String password = txtPassword.getText();
            String status = String.valueOf(comboBoxStatus.getSelectedItem());
            BigDecimal startingBalance = new BigDecimal(txtBalance.getText());

            boolean passedAuthentication = authenticateValues(name, phone, address, password, status);
            if(!passedAuthentication) {
                JOptionPane.showMessageDialog(null, "You must provide all fields with a value.");
            } else {
                dbHandler.insertUser(name, email, phone, address, password, status, startingBalance);
                setVisible(false);
                //"notifies" the parent form of the change by invoking the
                //interface method that was overridden by the parent.
                parentForm.onDataChange();
            }
        });

        btnCancel.addActionListener(e -> dispose());
    }

    public static boolean authenticateValues(String name, String phone, String address, String password, String status) {
        if (!name.equals("") || !phone.equals("") || !address.equals("") || !password.equals("") || !status.equals("")) {
            return true;
        }
        return false;
    }
}
