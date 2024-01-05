package Utility;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

//To scrape the currency names and respective codes.
public class CurrencyScraper {
    public static Map<String, String> getCurrencies() {
        Map<String, String> retCurrencies = new HashMap<>();
        try {
            //URL of the page containing the data we want to scrape
            Document doc = Jsoup.connect("https://www.exchangerate-api.com/docs/supported-currencies").get();

            // Select the second table on the page
            Elements tables = doc.select("table");
            if (tables.size() >= 3) {
                Element secondTable = tables.get(2); // Index 1 corresponds to the second table
                // ":gt(0)" selects rows starting from the second row
                Elements rows = secondTable.select("tbody > tr:gt(0)");

                // Iterate over the rows in the tbody
                for (Element row : rows) {
                    // Extract data from each cell in the row
                    // ":lt(2)" selects the first two cells in the row
                    Elements cells = row.select("td:lt(2)");
                    if (cells.size() >= 2) {
                        String key = cells.get(0).text();
                        String value = cells.get(1).text();
                        retCurrencies.put(key, value);
                    }
                }
                return retCurrencies;
            } else {
                System.out.println("Table not found on website.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
