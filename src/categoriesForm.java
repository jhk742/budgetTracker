import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class categoriesForm extends JDialog {

    private JPanel categoriesForm;
    private JTable tableCategories;
    private JTextField txtCategoryName;
    private JTextField txtCategoryDescription;
    private JButton btnAdd;
    private JButton btnBack;

    public categoriesForm(JFrame parent, loggedUser loggedU) {
        super(parent);
        setTitle("Categories");
        setContentPane(categoriesForm);
        setMinimumSize(new Dimension(1500, 800));
        setModal(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // load data into table when form opens
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                populateTable(tableCategories);
            }
        });

        btnBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                homeForm hf = new homeForm(null, loggedU);
                hf.setVisible(true);
            }
        });

        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = txtCategoryName.getText();
                String description = txtCategoryDescription.getText();
                try {
                    Connection con = ConnectionProvider.getCon();
                    PreparedStatement ps = con.prepareStatement("insert into categories (name, description) values (?, ?)");
                    ps.setString(1, name);
                    ps.setString(2, description);
                    int affectedRows = ps.executeUpdate();
                    if (affectedRows > 0) {
                        JOptionPane.showMessageDialog(null, "Successfully created category: " + name);
                        populateTable(tableCategories);
                    }
                } catch (Exception er) {
                    JOptionPane.showMessageDialog(null, "Error trying to create new category.");
                }
            }
        });
    }

    public static void populateTable(JTable tableCategories) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID", "Name", "Description"},0
        );
        tableCategories.setModel(model);
        try {
            Connection con = ConnectionProvider.getCon();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("select * from categories");
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString("category_id"), rs.getString("name"), rs.getString("description")});
            }
        } catch(Exception x) {
            JOptionPane.showMessageDialog(null, x);
        }
    }
}
