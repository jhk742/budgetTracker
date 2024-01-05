import Connectors.HttpProvider;
import Utility.CurrencyScraper;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
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

    public currencyExchangeForm(JFrame parent) {
        super(parent);
        setTitle("Login");
        setContentPane(currencyExchangeFormPanel);
        setMinimumSize(new Dimension(1500, 800));
        setModal(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initializeUI();
    }

    private void getExchangeRate(String url, String targetCurrency, String comparedCurrency, int amount) {
        String pairUrl = url + "/pair/" + targetCurrency + "/" + comparedCurrency + "/" + amount;
        try {
            JSONObject jsonObj = httpProvider.executeRequest(pairUrl);

        } catch(IOException e) {

        }
    }

    private void initializeUI() {
        Map<String, String> currencies = Utility.CurrencyScraper.getCurrencies();
        for (Map.Entry<String, String> entry : currencies.entrySet()) {
            comboBoxFrom.addItem(entry.getKey() + " - " + entry.getValue());
            comboBoxTo.addItem(entry.getKey() + " - " + entry.getValue());
        }
    }
}
