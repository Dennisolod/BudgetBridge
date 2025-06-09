package groupid.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class MoneyLine {
    private String type;
    private double amount;
    private double budgetLimit;
    private final DoubleProperty spent = new SimpleDoubleProperty(0.0);

    public MoneyLine(String type, double amount) {
        this.type = type;
        this.amount = amount;
        this.budgetLimit = amount; // default: full budget
    }

    // Getters
    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public double getBudgetLimit() {
        return budgetLimit;
    }

    // Setters
    public void setType(String type) {
        this.type = type;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setBudgetLimit(double budgetLimit) {
        this.budgetLimit = budgetLimit;
    }

    public double getSpent() {
        return spent.get();
    }
    public void setSpent(double value) {
        spent.set(value);
    }
    public DoubleProperty spentProperty() {
        return spent;
    }

    @Override
    public String toString() {
        return type + " : " + String.format("%.2f", amount);
    }
}
