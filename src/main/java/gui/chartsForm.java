package gui;
import Users.loggedUser;
import org.jfree.chart.*;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import javax.swing.*;
import java.awt.*;
import databaseHandlers.chartsFormDatabaseHandlers;

public class chartsForm extends JDialog{
    private JPanel chartForm;
    private ChartPanel chartPanel;
    private JButton btnBack;
    private JRadioButton radioBtnIncome;
    private JRadioButton radioBtnExpenses;
    private JRadioButton radioBtnNetIncome;
    private JRadioButton radioBtnClosingBalance;
    private JRadioButton radioBtnCompareIncomeExpense;
    private chartsFormDatabaseHandlers dbHandler = new chartsFormDatabaseHandlers();

    public chartsForm(JFrame parent, loggedUser loggedU) {
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

        btnBack.addActionListener(e -> {
            dispose();
            homeForm hf = new homeForm(null, loggedU);
            hf.setVisible(true);
        });

        radioBtnIncome.addActionListener(e -> {
            radioBtnExpenses.setSelected(false);
            radioBtnNetIncome.setSelected(false);
            radioBtnClosingBalance.setSelected(false);
            radioBtnCompareIncomeExpense.setSelected(false);
            createLineGraph("Income");
        });

        radioBtnExpenses.addActionListener(e -> {
            radioBtnIncome.setSelected(false);
            radioBtnNetIncome.setSelected(false);
            radioBtnClosingBalance.setSelected(false);
            radioBtnCompareIncomeExpense.setSelected(false);
            createPieChart();
        });

        radioBtnNetIncome.addActionListener(e -> {
            radioBtnExpenses.setSelected(false);
            radioBtnIncome.setSelected(false);
            radioBtnClosingBalance.setSelected(false);
            radioBtnCompareIncomeExpense.setSelected(false);
            createLineGraph("Profit/Loss");
        });

        radioBtnClosingBalance.addActionListener(e -> {
            radioBtnExpenses.setSelected(false);
            radioBtnIncome.setSelected(false);
            radioBtnNetIncome.setSelected(false);
            radioBtnCompareIncomeExpense.setSelected(false);
            createLineGraph("ClosingBalance");
        });

        radioBtnCompareIncomeExpense.addActionListener(e -> {
            radioBtnExpenses.setSelected(false);
            radioBtnIncome.setSelected(false);
            radioBtnNetIncome.setSelected(false);
            radioBtnClosingBalance.setSelected(false);
            createBarChart();
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

    private void createPieChart() {
        DefaultPieDataset pieDataSet = dbHandler.createPieChartDataset();
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
        CategoryDataset barDataSet = dbHandler.createBarChartDataset();
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
            CategoryDataset lineDataSet = dbHandler.createLineGraphDataSet(option);
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
            CategoryDataset lineDataSet = dbHandler.createLineGraphDataSet(option);
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
            CategoryDataset lineDataSet = dbHandler.createLineGraphDataSet(option);
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