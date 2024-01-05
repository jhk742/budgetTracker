import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
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
    private JTextField txtStatus;
    private User user = new User();
    private ArrayList<String> userFields;

    //to repopulate the jtable once a new user has been added to the database
    //via the "Add" button
    @Override
    public void onDataChange() {
        populateTable(tableUsers);
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
                populateTable(tableUsers);
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
                ArrayList<String> userFields = getUserFields(usersForm.this);
                //send in original values that match the values stored within the table pre-update
                String passWord = getUserPassword(Integer.parseInt(user.id), user.name, user.email, user.phone);
                System.out.println(passWord);
                try {
                    //update with new values provided in the textFields
                    Connection con = ConnectionProvider.getCon();
                    PreparedStatement ps = con.prepareStatement("update user set name=?, email=?, phone=?, address=?, status=? where id=? and password=?");
                    ps.setString(1, userFields.get(1));
                    ps.setString(2, userFields.get(2));
                    ps.setString(3, userFields.get(3));
                    ps.setString(4, userFields.get(4));
                    ps.setInt(5, userFields.get(5).equals("Active") ? 1 : 2);
                    ps.setString(6, userFields.get(0));
                    ps.setString(7, passWord);
                    int affectedRows = ps.executeUpdate();
                    if (affectedRows > 0) {
                        JOptionPane.showMessageDialog(null, "The user information has been successfully updated.");
                        setVisible(false);
                        usersForm uf = new usersForm(null, loggedU);
                        uf.setVisible(true);
                    }
                } catch (Exception er) {
                    JOptionPane.showMessageDialog(null, er);
                }
            }
        });

        //create and add a user
        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //pass in the reference to the form itself (JFrom.this) as
                //it implemetns the dataChangeListener. Once a new user is added,
                //the onDataChange method is invoked to repopulate the jtable to
                //reflect the change=(addition of a new user).
                addUserForm auf = new addUserForm(usersForm.this, loggedU);
                auf.setVisible(true);
            }
        });

        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<String> userFields = getUserFields(usersForm.this);
                String passWord = getUserPassword(Integer.parseInt(user.id), user.name, user.email, user.phone);
                try {
                    Connection con = ConnectionProvider.getCon();
                    PreparedStatement ps = con.prepareStatement("delete from user where id=? and name=? and email=? and password=?");
                    ps.setInt(1, Integer.parseInt(userFields.get(0)));
                    ps.setString(2, userFields.get(1));
                    ps.setString(3, userFields.get(2));
                    ps.setString(4, passWord);
                    int rowsAffected = ps.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(null, "User successfully deleted.");
                        setVisible(false);
                        usersForm uf = new usersForm(null, loggedU);
                        uf.setVisible(true);
                    }
                } catch(Exception er) {
                    JOptionPane.showMessageDialog(null, "We encountered an error while trying to delete the user.");
                }
            }
        });
//new Object[]{"ID","Name","Email","Phone","Address","Status"},0
        btnReset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtId.setText("");
                txtName.setText("");
                txtEmail.setText("");
                txtPhone.setText("");
                txtAddress.setText("");
                comboBoxStatus.setSelectedIndex(0);
                tableUsers.clearSelection();
            }
        });
        
        btnToHome.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                homeForm hf = new homeForm(null, loggedU);
                hf.setVisible(true);
            }
        });
    }

    public static String getUserPassword(int id, String name, String email, String phone) {
        String ret = "";
        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("select * from user where id=? and name=? and email=? and phone=?");
            ps.setInt(1, id);
            ps.setString(2, name);
            ps.setString(3, email);
            ps.setString(4, phone);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ret = rs.getString("password");
            }
            return ret;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
        return null;
    }

    public static void populateTable(JTable tableUsers) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID","Name","Email","Phone","Address","Status"},0
        );
        tableUsers.setModel(model);
        try {
            Connection con = ConnectionProvider.getCon();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("select * from user");
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString("id"), rs.getString("name"), rs.getString("email"), rs.getString("phone"), rs.getString("address"), rs.getString("status").equals("1") ? "Active" : "Inactive"});
            }

        } catch(Exception x) {
            JOptionPane.showMessageDialog(null, x);
        }
    }

    public ArrayList<String> getUserFields(usersForm uForm) {
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
