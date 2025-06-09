package groupid.model;

import java.time.LocalDate;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class MoneyLine {
    private String type;
    private String freq;
    private double amount;
    private double budgetLimit;
    private LocalDate dateAdded;
    private LocalDate lastOccurrence; // For recurring
    private boolean isActive;
    private final DoubleProperty spent = new SimpleDoubleProperty(0.0);

    public MoneyLine(String type, double amount) {
        this.type = type;
        this.amount = amount;
        this.budgetLimit = amount;
        
    }

    
    public MoneyLine(String type, String freq, double amount){
        this.type = type;
        this.freq = freq;
        this.amount = amount;
        this.budgetLimit = amount;
    }

    public MoneyLine(String type, String freq, double amount, LocalDate localDate){
        this.type = type;
        this.freq = freq;
        this.amount = amount;
        this.budgetLimit = amount;
        this.dateAdded = localDate;
    }

    public boolean isActiveThisMonth() {
        if ("One-time".equals(freq)) {
            return dateAdded.getMonth() == LocalDate.now().getMonth() 
                && dateAdded.getYear() == LocalDate.now().getYear();
        }
        return isActive; // Always true for recurring
    }

    // Getters
    public String getType() {
        return type;
    }

    public String getFreq() {
        return freq;
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

    public void setFreq(String freq){
        this.freq = freq;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setBudgetLimit(double budgetLimit) {
        this.budgetLimit = budgetLimit;
    }

    @Override
    public String toString() {
        return type + " : " + String.format("%.2f", amount);
    }
}
