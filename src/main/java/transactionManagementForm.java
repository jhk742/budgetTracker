import Connectors.ConnectionProvider;
import Users.loggedUser;
import ExceptionHandler.ExceptionHandler;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class transactionManagementForm extends JDialog {
    private JPanel transactionManagementForm;
    private JTextField txtDescription;
    private JTextField txtAmount;
    private JComboBox comboBoxCategory;
    private JComboBox comboBoxType;
    private JTextField txtRunningBalance;
    private JComboBox comboBoxPaymentMethod;
    private JTextField txtLocation;
    private JButton btnSubmit;
    private JButton btnBack;
    private JButton btnReset;
    private JLabel lblTotalBalance;
    private JButton btnAddCategory;

    public transactionManagementForm(JFrame parent, loggedUser loggedU) {
        super(parent);
        setTitle("Transaction Management");
        setContentPane(transactionManagementForm);
        setMinimumSize(new Dimension(1500, 800));
        setModal(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getLoggedUserTotalBalance(loggedU);

        //upon opening form
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                getLoggedUserTotalBalance(loggedU);
                // fill type combobox with three options (default: blank / income / expense)
                comboBoxType.addItem("");
                comboBoxType.addItem("Income");
                comboBoxType.addItem("Expense");
                comboBoxPaymentMethod.addItem("");
                comboBoxPaymentMethod.addItem("Card");
                comboBoxPaymentMethod.addItem("Cash");
                setFieldsEnabledFalse();
            }
        });


        comboBoxType.addActionListener(e -> {
            ArrayList<JComponent> comps = new ArrayList<>();
            comps.add(comboBoxCategory);
            comps.add(comboBoxPaymentMethod);
            comps.add(txtLocation);
            String selectedOption = (String) comboBoxType.getSelectedItem();
            txtDescription.setEnabled(true);
            txtAmount.setEnabled(true);
            if (selectedOption.isEmpty()) {
                setFieldsEnabledFalse();
            }
            if (selectedOption.equals("Expense")) {
                toggleFields("Expense", comps);
                //so that everytime the type is changed, redundant values aren't added to the combobox
                comboBoxCategory.removeAllItems();
                comboBoxCategory.addItem("");
                try {
                    Connection con = ConnectionProvider.getCon();
                    Statement st = con.createStatement();
                    ResultSet rs = st.executeQuery("select name from categories");
                    while (rs.next()) {
                        comboBoxCategory.addItem(rs.getString("name"));
                    }
                } catch (SQLException er) {
                    ExceptionHandler.unableToConnectToDb(er);
                }
            }
            if (selectedOption.equals("Income")) {
                toggleFields("Income", comps);
            }
        });

        btnSubmit.addActionListener(e -> {
            String option = String.valueOf(comboBoxType.getSelectedItem());
            boolean validationStatus = authenticateFields(option);
            String description = txtDescription.getText();
            BigDecimal amount = new BigDecimal(txtAmount.getText());
            String runningBalance = txtRunningBalance.getText();
            String type = String.valueOf(comboBoxType.getSelectedItem());
            LocalDate currentDate = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedDate = currentDate.format(formatter);
            if(validationStatus) {
                try {
                    Connection con = ConnectionProvider.getCon();
                    PreparedStatement ps = null;
                    if (option.equals("Expense")) {
                        // first retrieve the category id
                        int categoryId = getCategoryIdByName(comboBoxCategory);
                        String paymentMethod = String.valueOf(comboBoxPaymentMethod.getSelectedItem());
                        String location = txtLocation.getText();
                        // insert into transactions table
                        ps = con.prepareStatement("INSERT INTO transactions (date, description, amount, category_id, " +
                                "type, account_id, running_balance, payment_method, location) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
                        ps.setString(1, formattedDate);
                        ps.setString(2, description);
                        ps.setBigDecimal(3, amount);
                        ps.setInt(4, categoryId);
                        ps.setString(5, type);
                        ps.setInt(6, Integer.parseInt(loggedU.id));
                        ps.setBigDecimal(7, new BigDecimal(runningBalance));
                        ps.setString(8, paymentMethod);
                        ps.setString(9, location);
                        int rowsAffectedTransaction = ps.executeUpdate();
                        if (rowsAffectedTransaction > 0) {
                            updateBankAccount("Expense", loggedU, amount);
                        }
                    }
                    if (option.equals("Income")) {
                        ps = con.prepareStatement("INSERT INTO transactions (date, description, amount, " +
                                "type, account_id, running_balance) " +
                                "VALUES (?, ?, ?, ?, ?, ?)");
                        ps.setString(1, formattedDate);
                        ps.setString(2, description);
                        ps.setBigDecimal(3, amount);
                        ps.setString(4, type);
                        ps.setInt(5, Integer.parseInt(loggedU.id));
                        ps.setBigDecimal(6, new BigDecimal(runningBalance));
                        int rowsAffectedTransaction = ps.executeUpdate();
                        if (rowsAffectedTransaction > 0) {
                            updateBankAccount("Income", loggedU, amount);
                        }
                    }
                    //update to show new balance and reset all other fields to default (blanks)
                    getLoggedUserTotalBalance(loggedU);
                    resetFields();
                } catch (SQLException er) {
                    ExceptionHandler.unableToConnectToDb(er);
                }
            } else {
                ExceptionHandler.allFieldsRequired("All fields are required.");
            }
        });

        txtAmount.getDocument().addDocumentListener(new DocumentListener() {
            //to update the running balance txtfield when a input is provided for amount
            @Override
            public void insertUpdate(DocumentEvent e) {
                BigDecimal amount = new BigDecimal(txtAmount.getText());
                if(String.valueOf(comboBoxType.getSelectedItem()).equals("Income")) {
                    txtRunningBalance.setText(String.valueOf(loggedU.totalBalance.add(amount)));
                }
                if (String.valueOf(comboBoxType.getSelectedItem()).equals("Expense")) {
                    txtRunningBalance.setText(String.valueOf(loggedU.totalBalance.subtract(amount)));
                }
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        btnReset.addActionListener(e -> resetFields());

        btnBack.addActionListener(e -> {
            dispose();
            homeForm hf = new homeForm(null, loggedU);
            hf.setVisible(true);
        });

        btnAddCategory.addActionListener(e -> {
            dispose();
            //send in the name of this form so when the back btn is pressed within the category form, it shows this form and not home
            categoriesForm cf = new categoriesForm(null, loggedU, "transactionManagementForm");
            cf.setVisible(true);
        });
    }

    private void setFieldsEnabledFalse() {
        txtDescription.setEnabled(false);
        txtAmount.setEnabled(false);
        comboBoxCategory.setEnabled(false);
        txtRunningBalance.setEnabled(false);
        comboBoxPaymentMethod.setEnabled(false);
        txtLocation.setEnabled(false);
    }

    private void getLoggedUserTotalBalance(loggedUser loggedU) {
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

    private void toggleFields(String option, ArrayList<JComponent> comps) {
        for (JComponent comp : comps) {
            comp.setEnabled(option.equals("Income") ? false : true);
        }
    }

    private boolean authenticateFields(String option) {
        if (!txtDescription.getText().equals("") && !txtAmount.getText().equals("") && !txtRunningBalance.getText().equals("")) {
            if (option.equals("Income")) {
                return true;
            }
            if (option.equals("Expense")) {
                if (!comboBoxCategory.getSelectedItem().equals("") && !comboBoxPaymentMethod.getSelectedItem().equals("") && !txtLocation.getText().equals("")) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int getCategoryIdByName(JComboBox comboBox) {
        String categoryName = String.valueOf(comboBox.getSelectedItem());
        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("select category_id from categories where name=?");
            ps.setString(1, categoryName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("category_id");
                return id;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "That category does not exist.");
            System.out.println(e);
        }
        return -1;
    }

    private static void updateBankAccount(String option, loggedUser loggedU, BigDecimal amount) {
        try {
            Connection con = ConnectionProvider.getCon();
            String updateQuery = "update bank_accounts set account_balance = "
                    + (option.equals("Expense") ? "account_balance - ?" : "account_balance + ?")
                    + " WHERE user_id = ?";
            PreparedStatement psBankAccounts = con.prepareStatement(updateQuery);
            psBankAccounts.setBigDecimal(1, amount);
            psBankAccounts.setInt(2, Integer.parseInt(loggedU.id));
            int rowsAffectedBankAccount = psBankAccounts.executeUpdate();
            if (rowsAffectedBankAccount > 0) {
                JOptionPane.showMessageDialog(null, "Transaction created successfully!");
            }
        } catch (Exception er2) {
            JOptionPane.showMessageDialog(null, "Error while trying to update user's Bank Account information.");
        }
    }

    private void resetFields() {
        comboBoxType.setSelectedIndex(0);
        txtDescription.setText("");
        txtAmount.setText("");
        if (comboBoxCategory.isEnabled()) {
            comboBoxCategory.setSelectedIndex(0);
        }
        txtRunningBalance.setText("");
        comboBoxPaymentMethod.setSelectedIndex(0);
        txtLocation.setText("");
    }
}
