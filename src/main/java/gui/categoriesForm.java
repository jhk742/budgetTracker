package gui;

import Connectors.ConnectionProvider;
import Users.loggedUser;
import ExceptionHandler.ExceptionHandler;
import databaseHandlers.categoryFormDatabaseHandlers;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;

public class categoriesForm extends JDialog {

    private JPanel categoriesForm;
    private JTable tableCategories;
    private JTextField txtCategoryName;
    private JTextField txtCategoryDescription;
    private JButton btnAdd;
    private JButton btnBack;

    //1. reference to the transactionManagementForm when opened within the txManagementForm
    private String homeFormName;
    private categoryFormDatabaseHandlers dbHandler = new categoryFormDatabaseHandlers();

    public categoriesForm(JFrame parent, loggedUser loggedU, String formName) {
        super(parent);
        setTitle("Categories");
        setContentPane(categoriesForm);
        setMinimumSize(new Dimension(1500, 800));
        setModal(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        //2. for when opened from transactionManagement
        homeFormName = formName;

        // load data into table when form opens
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                try {
                    dbHandler.populateTable(tableCategories);
                } catch (SQLException er) {
                    ExceptionHandler.unableToConnectToDb(er);
                }
            }
        });

        btnBack.addActionListener(e -> {
            dispose();
            if (homeFormName.equals("transactionManagementForm")) {
                transactionManagementForm tmf = new transactionManagementForm(null, loggedU);
                tmf.setVisible(true);
            } else {
                homeForm hf = new homeForm(null, loggedU);
                hf.setVisible(true);
            }

        });

        btnAdd.addActionListener(e -> {
            String name = txtCategoryName.getText();
            String description = txtCategoryDescription.getText();
            dbHandler.addCategory(tableCategories, name, description);
        });
    }
}
