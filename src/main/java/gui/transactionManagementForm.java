package gui;
import Users.loggedUser;
import ExceptionHandler.ExceptionHandler;
import databaseHandlers.transactionManagementFormDatabaseHandlers;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
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

    //if expense is chosen first, the categories combobox is enabled and whatever the user chooses will remain even after switching to a different type. To circumvent this, check if it was chosen before any other option(type) is chosen.
    private boolean expenseBefore= false;

    private transactionManagementFormDatabaseHandlers dbHandler = new transactionManagementFormDatabaseHandlers();

    public transactionManagementForm(JFrame parent, loggedUser loggedU) {
        super(parent);
        setTitle("Transaction Management");
        setContentPane(transactionManagementForm);
        setMinimumSize(new Dimension(1500, 800));
        setModal(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
//        dbHandler.getSetLoggedUserTotalBalance(loggedU);

        //upon opening form
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                //gets the totalBalance
                dbHandler.getSetLoggedUserTotalBalance(loggedU);
                //and sets the totalBalance in the GUI form (label)
                lblTotalBalance.setText(String.valueOf(loggedU.totalBalance));
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
                if (expenseBefore) {
                    expenseBefore = false;
                    comboBoxCategory.setSelectedIndex(0);
                }
                setFieldsEnabledFalse();
            }
            if (selectedOption.equals("Expense")) {
                expenseBefore = true;
                toggleFields("Expense", comps);
                //so that everytime the type is changed, redundant values aren't added to the combobox
                comboBoxCategory.removeAllItems();
                comboBoxCategory.addItem("");
                ArrayList<String> categories = dbHandler.retrieveCategories();
                for (String category : categories) {
                    comboBoxCategory.addItem(category);
                }
            }
            if (selectedOption.equals("Income")) {
                if (expenseBefore) {
                    expenseBefore = false;
                    comboBoxCategory.setSelectedIndex(0);
                }
                toggleFields("Income", comps);
            }
        });

        btnSubmit.addActionListener(e -> {
            int categoryId = 0;
            String categoryName = "";
            String paymentMethod = "";
            String location = "";
            String option = String.valueOf(comboBoxType.getSelectedItem());
            boolean validationStatus = authenticateFields(option);
            String description = txtDescription.getText();
            BigDecimal amount = new BigDecimal(txtAmount.getText());
            BigDecimal runningBalance = new BigDecimal(txtRunningBalance.getText());
            String type = String.valueOf(comboBoxType.getSelectedItem());
            if (type.equals("Expense")) {
                categoryName = String.valueOf(comboBoxCategory.getSelectedItem());
                paymentMethod = String.valueOf(comboBoxPaymentMethod.getSelectedItem());
                location = txtLocation.getText();
                categoryId = dbHandler.getCategoryIdByName(categoryName);
            }
            LocalDate currentDate = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedDate = currentDate.format(formatter);
            if(validationStatus) {
                if (dbHandler.insertTransaction(option, formattedDate, description, amount, type, loggedU, runningBalance, categoryId, paymentMethod, location)) {
                    if(dbHandler.updateBankAccount(option, loggedU, amount)) {
                        JOptionPane.showMessageDialog(null, "Transaction created successfully!");
                    }
                    dbHandler.getSetLoggedUserTotalBalance(loggedU);
                } else {
                    JOptionPane.showMessageDialog(null, "Could not create transaction.");
                }
                lblTotalBalance.setText(String.valueOf(loggedU.totalBalance));
                resetFields((type.equals("Expense")));
            } else {
                ExceptionHandler.allFieldsRequired("All fields are required.");
            }
        });

        txtAmount.getDocument().addDocumentListener(new DocumentListener() {
            //to update the running balance txtfield when an input is provided for amount
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

        btnReset.addActionListener(e -> {
            String type = String.valueOf(comboBoxType.getSelectedItem());
            resetFields(type.equals("Expense"));
        });

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


    private void toggleFields(String option, ArrayList<JComponent> comps) {
        for (JComponent comp : comps) {
            comp.setEnabled(option.equals("Income") ? false : true);
        }
    }

    private boolean authenticateFields(String option) {
        if (!txtDescription.getText().isEmpty() && !txtAmount.getText().isEmpty() && !txtRunningBalance.getText().isEmpty()) {
            if (option.equals("Income")) {
                return true;
            }
            if (option.equals("Expense")) {
                if (!comboBoxCategory.getSelectedItem().equals("") && !comboBoxPaymentMethod.getSelectedItem().equals("") && !txtLocation.getText().isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void resetFields(boolean filter) {
        comboBoxType.setSelectedIndex(0);
        txtDescription.setText("");
        txtAmount.setText("");
        if (filter) {
            comboBoxCategory.setSelectedIndex(0);
        }
        txtRunningBalance.setText("");
        comboBoxPaymentMethod.setSelectedIndex(0);
        txtLocation.setText("");
    }
}
