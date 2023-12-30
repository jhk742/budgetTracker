import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class charts extends JDialog{
    private JPanel chartForm;
    private JButton btnBack;
    private JRadioButton radioBtnIncome;
    private JRadioButton radioBtnExpenses;

    public charts(JFrame parent, loggedUser loggedU) {
        super(parent);
        chartForm = new JPanel();
        setTitle("Charts");
        setContentPane(chartForm);
        setMinimumSize(new Dimension(1300, 800));
        setModal(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);


        initializeUI();


        btnBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                homeForm hf = new homeForm(null, loggedU);
                hf.setVisible(true);
            }
        });

        radioBtnIncome.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                radioBtnExpenses.setSelected(false);
                createPieChart("Income");
            }
        });

        radioBtnExpenses.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                radioBtnIncome.setSelected(false);
                createPieChart("Expenses");
            }
        });
    }

    private void initializeUI() {
        chartForm.setLayout(new BorderLayout());
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioBtnIncome = new JRadioButton("Income");
        radioBtnExpenses = new JRadioButton("Expenses");
        btnBack = new JButton("Back");
        radioPanel.add(radioBtnIncome);
        radioPanel.add(radioBtnExpenses);
        chartForm.add(radioPanel, BorderLayout.NORTH);
        chartForm.add(btnBack, BorderLayout.SOUTH);
    }
    private DefaultPieDataset createDataset(String option) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        // Connect to the database (replace with your database credentials)
        if (option.equals("Expenses")) {
            try  {
                Connection con = ConnectionProvider.getCon();
                PreparedStatement ps = con.prepareStatement("SELECT c.name AS category, SUM(t.amount) AS total\n" +
                        "FROM transactions t\n" +
                        "INNER JOIN categories c ON t.category_id = c.category_id\n" +
                        "WHERE t.type = 'Expense'\n" +
                        "GROUP BY c.name");
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String category = rs.getString("category");
                    double total = rs.getDouble("total");
                    dataset.setValue(category, total);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return dataset;
        }
        if (option.equals("Income")) {
            try  {
                // income will be displayed using a line-graph
            } catch (Exception e) {
                e.printStackTrace();
            }
            return dataset;
        }
        return null;
    }

    public void createPieChart(String option) {
        DefaultPieDataset pieDataSet = createDataset(option);
        JFreeChart chart = ChartFactory.createPieChart("Expenses by Category",
                pieDataSet,
                true,
                true,
                true);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: ${1} ({2})"));
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(1300, 800));
        chartForm.add(chartPanel, BorderLayout.CENTER);

        pack();
    }
}
