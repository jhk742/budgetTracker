import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.XYSeriesLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

public class charts extends JDialog{
    private JPanel chartForm;
    private ChartPanel chartPanel;
    private JButton btnBack;
    private JRadioButton radioBtnIncome;
    private JRadioButton radioBtnExpenses;

    public charts(JFrame parent, loggedUser loggedU) {
        super(parent);
        chartForm = new JPanel();
        chartPanel = new ChartPanel(null);
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
                createLineGraph();
            }
        });

        radioBtnExpenses.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                radioBtnIncome.setSelected(false);
                createPieChart();
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
        chartForm.add(chartPanel, BorderLayout.CENTER);
    }

    private XYSeriesCollection createLineGraphDataSet() {
        //create the collection to store the individual points of the line graph
        XYSeriesCollection lineGraphDataset = new XYSeriesCollection();
        try  {
            // income will be displayed using a line-graph
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("SELECT date, amount FROM transactions WHERE type = 'Income'");
            ResultSet rs = ps.executeQuery();
            //create the series to store inside the collection
            XYSeries series = new XYSeries("Income");
            while (rs.next()) {
                String date = rs.getString("date");
                double amount = rs.getDouble("amount");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date parsedDate = dateFormat.parse(date);
                series.add(parsedDate.getTime(), amount);
            }
            //add the series to the collection
            lineGraphDataset.addSeries(series);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lineGraphDataset;
    }
    private DefaultPieDataset createPieChartDataset() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        // Connect to the database (replace with your database credentials)
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

    private void createPieChart() {
        DefaultPieDataset pieDataSet = createPieChartDataset();
        JFreeChart chart = ChartFactory.createPieChart("Expenses by Category",
                pieDataSet,
                true,
                true,
                true);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: ${1} ({2})"));
//        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(1300, 800));
//        chartForm.add(chartPanel, BorderLayout.CENTER);
        chartPanel.setChart(chart);
        pack();
    }

    private void createLineGraph() {
        XYSeriesCollection lineDataSet = createLineGraphDataSet();
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Income Line Graph",
                "Date",
                "Amount",
                lineDataSet,
                false,
                true,
                false
        );

        XYPlot plot = chart.getXYPlot();
        DateAxis dateAxis = new DateAxis("Date");
        plot.setDomainAxis(dateAxis);
        plot.setRangeAxis(new NumberAxis("Amount"));
        chartPanel.setChart(chart);
    }
}

/**
 * To see if i was at a positive or minus
 SELECT
 date,
 SUM(CASE WHEN type = 'Income' THEN amount ELSE 0 END) -
 SUM(CASE WHEN type = 'Expense' THEN amount ELSE 0 END) AS running_balance
 FROM transactions
 GROUP BY date
 ORDER BY date;
 */

/***
 * To get the ending-balance for each day
 WITH RankedTransactions AS (
 SELECT
 *,
 ROW_NUMBER() OVER (PARTITION BY date ORDER BY transaction_id DESC) AS row_num
 FROM transactions
 )
 SELECT
 transaction_id,
 date,
 description,
 amount,
 category_id,
 type,
 account_id,
 running_balance
 FROM RankedTransactions
 WHERE row_num = 1;
 */