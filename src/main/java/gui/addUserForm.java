package gui;

import Connectors.ConnectionProvider;
import Users.loggedUser;
import ExceptionHandler.ExceptionHandler;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

            boolean passedAuthentication = authenticateValues(name, phone, address, password, status);
            if(!passedAuthentication) {
                JOptionPane.showMessageDialog(null, "You must provide all fields with a value.");
            } else {
                try {
                    Connection con = ConnectionProvider.getCon();
                    PreparedStatement ps = con.prepareStatement("insert into user (name, email, phone, address, password, status) values (?, ?, ?, ?, ?, ?)");
                    ps.setString(1, name);
                    ps.setString(2, email);
                    ps.setString(3, phone);
                    ps.setString(4, address);
                    ps.setString(5, password);
                    ps.setInt(6, status.equals("Active") ? 1 : 0);
                    int rowsAffected = ps.executeUpdate();
                    try {
                        PreparedStatement createdUser = con.prepareStatement("select * from user where name=? and email=? and password=?");
                        createdUser.setString(1, name);
                        createdUser.setString(2, email);
                        createdUser.setString(3, password);
                        ResultSet rs = createdUser.executeQuery();
                        if (rs.next()) {
                            int userId = rs.getInt("id");
                            try {
                                //retrieve and use id after creation
                                PreparedStatement psBalance = con.prepareStatement("insert into bank_accounts (user_id, account_balance) values (?, ?)");
                                psBalance.setInt(1, userId);
                                psBalance.setBigDecimal(2, new BigDecimal(txtBalance.getText()));
                                int rowsAffectedBankAccounts = psBalance.executeUpdate();
                                if (rowsAffected > 0 && rowsAffectedBankAccounts > 0) {
                                    JOptionPane.showMessageDialog(null, "User successfully created.");
                                }
                            } catch (SQLException insertNewUser) {
                                ExceptionHandler.unableToConnectToDb(insertNewUser);
                            }
                        }
                    } catch (SQLException createdUser) {
                        ExceptionHandler.unableToConnectToDb(createdUser);
                    }
                     setVisible(false);
                    //"notifies" the parent form of the change by invoking the
                    //interface method that was overridden by the parent.
                    parentForm.onDataChange();
                } catch(SQLException er) {
                      ExceptionHandler.unableToConnectToDb(er);
                }
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
