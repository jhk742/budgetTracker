import Connectors.HttpProvider;
import org.json.JSONObject;

import javax.swing.*;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        loginForm login = new loginForm(null);

        HttpProvider httpProvider = new HttpProvider();

//        try {
//            JSONObject jsonObj = httpProvider.makeGetRequest("https://v6.exchangerate-api.com/v6/YOUR-API-KEY/latest/USD");
//            // Check if the response is not empty before creating a JSONObject
//            if (!response.isEmpty()) {
//                JSONObject jsonObj = new JSONObject(response);
//
//                // Now you can access the data from the JSONObject
//                String result = jsonObj.getString("result");
//                System.out.println("Result: " + result);
//
//            } else {
//                System.out.println("Empty response");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}