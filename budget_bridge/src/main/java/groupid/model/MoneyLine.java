package groupid.model;

import lombok.Value;

@Value
public class MoneyLine {
    String type;
    double amount;

    @Override
    public String toString() {
    return type + " : " + String.format("%.2f", amount);
}
}