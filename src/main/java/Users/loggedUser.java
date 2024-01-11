package Users;

import java.math.BigDecimal;

public class loggedUser {
    public String id;
    public String name;
    public String email;
    public String phone;
    public String address;
    public String password;
    public int status;
    public BigDecimal totalBalance;
    public BigDecimal totalIncome;
    public BigDecimal totalExpense;

    public loggedUser() {}

    //for testing purposes
    public loggedUser(String id) {
        this.id = id;
    }
}
