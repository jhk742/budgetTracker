package gui;
import Users.User;
import Users.loggedUser;
import databaseHandlers.usersFormDatabaseHandlers;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class usersForm extends JDialog implements DataChangeListener {
    private JTable tableUsers;
    private JTextField txtAddress;
    private JPanel usersForm;
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnReset;
    private JButton btnToHome;
    private JButton btnDelete;
    private JTextField txtId;
    private JTextField txtName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JComboBox comboBoxStatus;

    //for selected User from table
    private User user = new User();

    private usersFormDatabaseHandlers dbHandler = new usersFormDatabaseHandlers();

    //to repopulate the jtable once a new user has been added to the database
    //via the "Add" button
    @Override
    public void onDataChange() {
        dbHandler.populateTable(tableUsers);
    }

    public usersForm(JFrame parent, loggedUser loggedU) {
        super(parent);
        setTitle("Login");
        setContentPane(usersForm);
        setMinimumSize(new Dimension(1500, 800));
        setModal(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        comboBoxStatus.addItem("");
        comboBoxStatus.addItem("Active");
        comboBoxStatus.addItem("Inactive");
        // the id field should be tampered with.
        txtId.setEditable(false);
        // the update button should only be enabled if a user is clicked on (in jtable)
        btnUpdate.setEnabled(false);
        // should only delete when a user has been clicked.
        btnDelete.setEnabled(false);

        // load data into table when form opens
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                dbHandler.populateTable(tableUsers);
            }
        });

        // retrieve data from selected row of the jtable and fill in the corresponding text-fields
        tableUsers.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                // user clicked -> update && delete button enabled
                btnUpdate.setEnabled(true);
                btnDelete.setEnabled(true);

                int row = tableUsers.getSelectedRow();
                String id = String.valueOf(tableUsers.getValueAt(row, 0));
                String name = String.valueOf(tableUsers.getValueAt(row, 1));
                String email = String.valueOf(tableUsers.getValueAt(row, 2));
                String phone = String.valueOf(tableUsers.getValueAt(row, 3));
                String address = String.valueOf(tableUsers.getValueAt(row, 4));
                String status = String.valueOf(tableUsers.getValueAt(row, 5));

                user.id = id;
                user.name = name;
                user.email = email;
                user.phone = phone;

                txtId.setText(id);
                txtName.setText(name);
                txtEmail.setText(email);
                txtPhone.setText(phone);
                txtAddress.setText(address);
                comboBoxStatus.setSelectedItem(status);
            }
        });

        // upon clicking the update button
        btnUpdate.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                dbHandler.updateUser(user, loggedU, usersForm.this);
            }
        });

        //create and add a user
        btnAdd.addActionListener(e -> {
            //pass in the reference to the form itself (JFrom.this) as
            //it implements the dataChangeListener. Once a new user is added,
            //the onDataChange method is invoked to repopulate the jtable to
            //reflect the change=(addition of a new user).
            addUserForm auf = new addUserForm(usersForm.this, loggedU);
            auf.setVisible(true);
        });

        btnDelete.addActionListener(e -> {
            dbHandler.deleteUser(user, loggedU, usersForm.this);
        });

        btnReset.addActionListener(e -> {
            txtId.setText("");
            txtName.setText("");
            txtEmail.setText("");
            txtPhone.setText("");
            txtAddress.setText("");
            comboBoxStatus.setSelectedIndex(0);
            tableUsers.clearSelection();
        });
        
        btnToHome.addActionListener(e -> {
            setVisible(false);
            homeForm hf = new homeForm(null, loggedU);
            hf.setVisible(true);
        });
    }

    public static ArrayList<String> getUserFields(usersForm uForm) {
        ArrayList<String> userFields = new ArrayList<>();
        String id = uForm.txtId.getText();
        String name = uForm.txtName.getText();
        String email = uForm.txtEmail.getText();
        String phone = uForm.txtPhone.getText();
        String address = uForm.txtAddress.getText();
        String status = String.valueOf(uForm.comboBoxStatus.getSelectedItem());
        userFields.add(id);
        userFields.add(name);
        userFields.add(email);
        userFields.add(phone);
        userFields.add(address);
        userFields.add(status);
        return userFields;
    }
}
