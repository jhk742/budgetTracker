package gui;

import Connectors.ConnectionProvider;
import Users.loggedUser;
import org.jfree.chart.*;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.jdbc.JDBCCategoryDataset;
import ExceptionHandler.ExceptionHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class charts extends JDialog{
    private JPanel chartForm;
    private ChartPanel chartPanel;
    private JButton btnBack;
    private JRadioButton radioBtnIncome;
    private JRadioButton radioBtnExpenses;
    private JRadioButton radioBtnNetIncome;
    private JRadioButton radioBtnClosingBalance;
    private JRadioButton radioBtnCompareIncomeExpense;

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
                radioBtnNetIncome.setSelected(false);
                radioBtnClosingBalance.setSelected(false);
                radioBtnCompareIncomeExpense.setSelected(false);
                createLineGraph("Income");
            }
        });

        radioBtnExpenses.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                radioBtnIncome.setSelected(false);
                radioBtnNetIncome.setSelected(false);
                radioBtnClosingBalance.setSelected(false);
                radioBtnCompareIncomeExpense.setSelected(false);
                createPieChart();
            }
        });
        radioBtnNetIncome.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                radioBtnExpenses.setSelected(false);
                radioBtnIncome.setSelected(false);
                radioBtnClosingBalance.setSelected(false);
                radioBtnCompareIncomeExpense.setSelected(false);
                createLineGraph("Profit/Loss");
            }
        });
        radioBtnClosingBalance.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                radioBtnExpenses.setSelected(false);
                radioBtnIncome.setSelected(false);
                radioBtnNetIncome.setSelected(false);
                radioBtnCompareIncomeExpense.setSelected(false);
                createLineGraph("ClosingBalance");
            }
        });
        radioBtnCompareIncomeExpense.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                radioBtnExpenses.setSelected(false);
                radioBtnIncome.setSelected(false);
                radioBtnNetIncome.setSelected(false);
                radioBtnClosingBalance.setSelected(false);
                createBarChart();
            }
        });
    }

    private void initializeUI() {
        chartForm.setLayout(new BorderLayout());
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioBtnIncome = new JRadioButton("Income");
        radioBtnExpenses = new JRadioButton("Expenses");
        radioBtnNetIncome = new JRadioButton("Net Income");
        radioBtnCompareIncomeExpense = new JRadioButton("Income vs Expenses");
        radioBtnClosingBalance = new JRadioButton("Daily Closing Balance");
        btnBack = new JButton("Back");
        radioPanel.add(radioBtnIncome);
        radioPanel.add(radioBtnExpenses);
        radioPanel.add(radioBtnNetIncome);
        radioPanel.add(radioBtnClosingBalance);
        radioPanel.add(radioBtnCompareIncomeExpense);
        chartForm.add(radioPanel, BorderLayout.NORTH);
        chartForm.add(btnBack, BorderLayout.SOUTH);
        chartForm.add(chartPanel, BorderLayout.CENTER);
    }

    private JDBCCategoryDataset createLineGraphDataSet(String option) {
        try {
            Connection con = ConnectionProvider.getCon();
            JDBCCategoryDataset lineGraphDataSet = null;
            if (option.equals("Income")) {
                lineGraphDataSet = new JDBCCategoryDataset(con, "SELECT date, SUM(amount) AS total_amount\n" +
                        "FROM transactions\n" +
                        "WHERE type = 'Income'\n" +
                        "GROUP BY date\n" +
                        "ORDER BY date");
            }
            if (option.equals("Profit/Loss")) {
                lineGraphDataSet = new JDBCCategoryDataset(con, "SELECT\n" +
                        " DATE(date) AS date,\n" +
                        " SUM(CASE WHEN type = 'Income' THEN amount ELSE 0 END) -\n" +
                        " SUM(CASE WHEN type = 'Expense' THEN amount ELSE 0 END) AS running_balance\n" +
                        " FROM transactions\n" +
                        " GROUP BY DATE(date)\n" +
                        " ORDER BY DATE(date)");
            }
            if (option.equals("ClosingBalance")) {
                lineGraphDataSet = new JDBCCategoryDataset(con, "WITH RankedTransactions AS (\n" +
                        " SELECT\n" +
                        " *,\n" +
                        " ROW_NUMBER() OVER (PARTITION BY date ORDER BY transaction_id DESC) AS row_num\n" +
                        " FROM transactions\n" +
                        " )\n" +
                        " SELECT\n" +
                        " date,\n" +
                        " running_balance\n" +
                        " FROM RankedTransactions\n" +
                        " WHERE row_num = 1;\n");
            }
            return lineGraphDataSet;
        } catch (SQLException e) {
            ExceptionHandler.unableToConnectToDb(e);
        }
        return null;
    }

    private JDBCCategoryDataset createBarChartDataset() {
        try {
            Connection con = ConnectionProvider.getCon();
            JDBCCategoryDataset barChartDataSet = new JDBCCategoryDataset(con, "SELECT\n" +
                " date,\n" +
                " SUM(CASE WHEN type = 'Income' THEN amount ELSE 0 END) AS total_income,\n" +
                " SUM(CASE WHEN type = 'Expense' THEN amount ELSE 0 END) AS total_expense\n" +
                " FROM transactions\n" +
                " GROUP BY date\n" +
                " ORDER BY date");
            return barChartDataSet;
        } catch (SQLException e) {
            ExceptionHandler.unableToConnectToDb(e);
        }
        return null;
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
        } catch (SQLException e) {
            ExceptionHandler.unableToConnectToDb(e);
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
        chartPanel.setPreferredSize(new Dimension(1300, 800));
        chartPanel.setChart(chart);
        pack();
    }

    private void createBarChart() {
        CategoryDataset barDataSet = createBarChartDataset();
        JFreeChart chart = ChartFactory.createBarChart("Income vs Expenses",
                "Date",
                "Income/Expense",
                barDataSet,
                PlotOrientation.VERTICAL,
                false,
                true,
                true
        );
        LegendItemCollection legendItems = new LegendItemCollection();
        legendItems.add(new LegendItem("Income", null, null, null, new Rectangle(10, 10), Color.red));
        legendItems.add(new LegendItem("Expenses", null, null, null, new Rectangle(10, 10), Color.blue));
        chart.addLegend(new LegendTitle(chart.getCategoryPlot()));
        chart.getCategoryPlot().setFixedLegendItems(legendItems);
        chartPanel.setChart(chart);
    }

    private void createLineGraph(String option) {
        if(option.equals("Income")) {
            CategoryDataset lineDataSet = createLineGraphDataSet(option);
            JFreeChart chart = ChartFactory.createLineChart(
                    "Income Line Graph",
                    "Date",
                    "Amount",
                    lineDataSet,
                    PlotOrientation.VERTICAL,
                    false,
                    true,
                    true
            );
            LineAndShapeRenderer renderer = (LineAndShapeRenderer) chart.getCategoryPlot().getRenderer();
            renderer.setDefaultLinesVisible(true);
            renderer.setDefaultShapesFilled(true);
            renderer.setDefaultShapesVisible(true);
            chartPanel.setChart(chart);
        }
        if(option.equals("Profit/Loss")) {
            CategoryDataset lineDataSet = createLineGraphDataSet(option);
            JFreeChart chart = ChartFactory.createLineChart(
                    "Daily Financial Performance",
                    "Date",
                    "Net Income",
                    lineDataSet,
                    PlotOrientation.VERTICAL,
                    false,
                    true,
                    true
            );
            LineAndShapeRenderer renderer = (LineAndShapeRenderer) chart.getCategoryPlot().getRenderer();
            renderer.setDefaultLinesVisible(true);
            renderer.setDefaultShapesFilled(true);
            renderer.setDefaultShapesVisible(true);
            chartPanel.setChart(chart);
        }
        if(option.equals("ClosingBalance")) {
            CategoryDataset lineDataSet = createLineGraphDataSet(option);
            JFreeChart chart = ChartFactory.createLineChart(
                    "Daily Closing Balance",
                    "Date",
                    "Closing Balance",
                    lineDataSet,
                    PlotOrientation.VERTICAL,
                    false,
                    true,
                    true
            );
            LineAndShapeRenderer renderer = (LineAndShapeRenderer) chart.getCategoryPlot().getRenderer();
            renderer.setDefaultLinesVisible(true);
            renderer.setDefaultShapesFilled(true);
            renderer.setDefaultShapesVisible(true);
            chartPanel.setChart(chart);
        }
    }
}