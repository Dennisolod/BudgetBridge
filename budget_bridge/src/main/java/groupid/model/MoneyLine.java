package groupid.model;

public class MoneyLine {
    private String type;
    private double amount;
    private double budgetLimit;

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

    @Override
    public String toString() {
        return type + " : " + String.format("%.2f", amount);
    }
}
