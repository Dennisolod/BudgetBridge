package groupid.model;

//public record MoneyLine(String description, double amount){ }
import lombok.Value;

@Value
public class MoneyLine {
    String type;
    double amount;
}