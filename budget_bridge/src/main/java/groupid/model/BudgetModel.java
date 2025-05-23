package groupid.model;

import java.util.AbstractMap;
import java.util.Map;

import javafx.beans.InvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class BudgetModel {

    // user‑enterable fields
    private final StringProperty username = new SimpleStringProperty();
    private final ObservableList<MoneyLine> incomes = FXCollections.observableArrayList();
    private final ObservableList<MoneyLine> expenses = FXCollections.observableArrayList();

    // gamification
    private final IntegerProperty points = new SimpleIntegerProperty(0);
    private final ObservableList<String> badges = FXCollections.observableArrayList();
    private final ObservableList<Map.Entry<String, Integer>> leaderboard = FXCollections.observableArrayList();
    
    // convenience derived values (totals, net)

    private final ReadOnlyDoubleWrapper totalIncome = new ReadOnlyDoubleWrapper();
    private final ReadOnlyDoubleWrapper totalExpense= new ReadOnlyDoubleWrapper();
    private final ReadOnlyDoubleWrapper netBalance = new ReadOnlyDoubleWrapper();

    // helper method to add user to the leaderboard
    public void addUserToLeaderboard(String userName, int points) {
        // Remove existing entry for username if it exists
        leaderboard.removeIf(entry -> entry.getKey().equals(userName));

        // Add new entry
        Map.Entry<String, Integer> newEntry = new AbstractMap.SimpleEntry<String, Integer>(userName, points);
        leaderboard.add(newEntry);

        // Sort the leaderboard in descending order of points
        leaderboard.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
    }

    public BudgetModel() {
        InvalidationListener recalc = obs -> recalcTotals();
        incomes .addListener(recalc);
        expenses.addListener(recalc);
        recalc.invalidated(null);

        leaderboard.addAll(
            Map.entry("Alice", 15000),
            Map.entry("Bob", 14000),
            Map.entry("Charlie", 13000),
            Map.entry("Diana", 12000),
            Map.entry("Ethan", 11000), 
            Map.entry("Dylan", 10000),
            Map.entry("Tom", 9000),
            Map.entry("Charles", 8000),
            Map.entry("TheLegend27", 7000),
            Map.entry("Johnnys", 6000), 
            Map.entry("Alias", 5000),
            Map.entry("Elias", 4000),
            Map.entry("Olias", 3000),
            Map.entry("Tommy", 2000),
            Map.entry("Thompson", 1000), 
            Map.entry("Johnathan", 900),
            Map.entry("Bob", 800),
            Map.entry("Charlie", 750),
            Map.entry("Diana", 700),
            Map.entry("Ethan", 600), 
            Map.entry("Alice", 579),
            Map.entry("Bob", 577),
            Map.entry("Charlie", 550),
            Map.entry("Diana", 500),
            Map.entry("Ethan", 490), 
            Map.entry("Alice", 480),
            Map.entry("Bob", 470),
            Map.entry("Charlie", 400),
            Map.entry("Diana", 200)
        );

    }
    private void recalcTotals() {
        totalIncome.set(
            incomes.stream()
                   .mapToDouble(MoneyLine::getAmount)
                   .sum()
          );
          totalExpense.set(
            expenses.stream()
                    .mapToDouble(MoneyLine::getAmount)
                    .sum()
          );        netBalance.set(totalIncome.get() - totalExpense.get());
    }

    // getters
    public StringProperty usernameProperty() { return username; }
    public ObservableList<MoneyLine> incomes() { return incomes; }
    public ObservableList<MoneyLine> expenses(){ return expenses; }
    public IntegerProperty pointsProperty() { return points; }
    public ObservableList<String> badges(){ return badges; }
    public ObservableList<Map.Entry<String, Integer>> getLeaderboard() { return leaderboard; }

    public ReadOnlyDoubleProperty totalIncomeProperty(){ return totalIncome.getReadOnlyProperty(); }
    public ReadOnlyDoubleProperty totalExpenseProperty(){ return totalExpense.getReadOnlyProperty(); }
    public ReadOnlyDoubleProperty netBalanceProperty(){ return netBalance .getReadOnlyProperty(); }

    // helpers
    public void addIncome (String d, double a) { incomes .add(new MoneyLine(d, a)); reward(); }
    public void addExpense(String d, double a) { expenses.add(new MoneyLine(d, a)); reward(); }

    private void reward() {
        points.set(points.get() + 10);
        if (points.get() >= 50 && !badges.contains("Novice Saver"))
            badges.add("Novice Saver");
    }

    public void addPoints(int p) {
        points.set(points.get() + p);
    }

}
