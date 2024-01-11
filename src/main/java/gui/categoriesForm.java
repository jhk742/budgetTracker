package gui;
import Users.loggedUser;
import databaseHandlers.categoryFormDatabaseHandlers;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class categoriesForm extends JDialog {

    private JPanel categoriesForm;
    private JTable tableCategories;
    private JTextField txtCategoryName;
    private JTextField txtCategoryDescription;
    private JButton btnAdd;
    private JButton btnBack;

    //1. reference to the transactionManagementForm when opened within the txManagementForm
    private String homeFormName;
    private List<List<String>> tableCategoriesData;
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
                tableCategoriesData = dbHandler.retrieveCategories();
                populateTable(tableCategoriesData);
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
            boolean success = dbHandler.addCategory(name, description);
            if (success) {
                JOptionPane.showMessageDialog(null, "Successfully created category.");
                tableCategoriesData = dbHandler.retrieveCategories();
                populateTable(tableCategoriesData);
            }
        });
    }

    private void populateTable(List<List<String>> data) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID", "Name", "Description"},0
        );
        tableCategories.setModel(model);
        assert model != null;
        for (List<String> rowData : data) {
            model.addRow(rowData.toArray());
        }
    }
}
