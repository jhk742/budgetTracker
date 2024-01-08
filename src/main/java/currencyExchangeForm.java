import Connectors.HttpProvider;
import Users.loggedUser;
import org.json.JSONObject;
import ExceptionHandler.ExceptionHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

public class currencyExchangeForm extends JDialog {
    HttpProvider httpProvider = new HttpProvider();
    private final String key = "1642e30ab85f5926390595f7";
    private final String url = "https://v6.exchangerate-api.com/v6/" + key;
    private JPanel currencyExchangeFormPanel;
    private JTextField txtValue;
    private JComboBox comboBoxFrom;
    private JComboBox comboBoxTo;
    private JButton btnClear;
    private JButton btnCalculate;
    private JTextPane txtPaneAnswer;
    private JButton btnBack;

    public currencyExchangeForm(JFrame parent, loggedUser loggedU) {
        super(parent);
        setTitle("Login");
        setContentPane(currencyExchangeFormPanel);
        setMinimumSize(new Dimension(500, 450));
        setModal(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initializeUI();
        btnClear.addActionListener(e -> {
            txtValue.setText("");
            txtPaneAnswer.setText("");
            comboBoxFrom.setSelectedIndex(0);
            comboBoxTo.setSelectedIndex(0);
        });

        btnCalculate.addActionListener(e -> {
            try {
                String value = txtValue.getText();
                String targetCurrency = String.valueOf(comboBoxFrom.getSelectedItem()).substring(0, 3);
                String comparedCurrency = String.valueOf(comboBoxTo.getSelectedItem()).substring(0, 3);
                JSONObject json = getExchangeRate(targetCurrency, comparedCurrency, value);
                targetCurrency = json.getString("base_code");
                comparedCurrency = json.getString("target_code");
                BigDecimal conversionResult = json.getBigDecimal("conversion_result");
                BigDecimal conversionRate = json.getBigDecimal("conversion_rate");
                txtPaneAnswer.setContentType("text/html");
                String htmlContent =
                        "<html>" +
                                "<div>" +
                                "<span> " +
                                "Answer: " +
                                "</span>" +
                                "</div>" +
                                "<div style='text-align:center;'>" +
                                "<p>" +
                                "<strong>" + Double.valueOf(value) + "</strong> " + targetCurrency + " = <strong>" + conversionResult + "</strong> " + comparedCurrency + "<br>" +
                                "1 " + targetCurrency + " = " + conversionRate + " " + comparedCurrency +
                                "</p>" +
                                "</div>" +
                                "</html>";
                txtPaneAnswer.setText(htmlContent);
            } catch (IOException er) {
                ExceptionHandler.httpError(er);
            }
        });

        btnBack.addActionListener(e -> {
            dispose();
            homeForm hf = new homeForm(null, loggedU);
            hf.setVisible(true);
        });
    }

    private JSONObject getExchangeRate(String targetCurrency, String comparedCurrency, String amount) throws IOException {
        String pairUrl = url + "/pair/" + targetCurrency + "/" + comparedCurrency + "/" + amount;
        JSONObject jsonObj = httpProvider.executeGetRequest(pairUrl);
        return jsonObj;
    }

    private void initializeUI() {
        Map<String, String> currencies = Utility.CurrencyScraper.getCurrencies();
        String selectCurrencyplaceHolder = "--- Select Currency ---";
        comboBoxFrom.addItem(selectCurrencyplaceHolder);
        comboBoxTo.addItem(selectCurrencyplaceHolder);
        for (Map.Entry<String, String> entry : currencies.entrySet()) {
            comboBoxFrom.addItem(entry.getKey() + " - " + entry.getValue());
            comboBoxTo.addItem(entry.getKey() + " - " + entry.getValue());
        }
    }
}
