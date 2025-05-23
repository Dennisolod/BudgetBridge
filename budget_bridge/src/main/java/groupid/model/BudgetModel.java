package groupid.model;

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
import javafx.scene.paint.Paint;

public class BudgetModel {

    // user‑enterable fields
    private final StringProperty username = new SimpleStringProperty();
    private final ObservableList<MoneyLine> incomes = FXCollections.observableArrayList();
    private final ObservableList<MoneyLine> expenses = FXCollections.observableArrayList();
    private final ObservableList<MissionLine> missions = FXCollections.observableArrayList();
    private final ObservableList<BadgeLine> badges = FXCollections.observableArrayList();
    // gamification
    private final IntegerProperty points = new SimpleIntegerProperty(0);
    private final ObservableList<Map.Entry<String, Integer>> leaderboard = FXCollections.observableArrayList();
    
    // convenience derived values (totals, net)

    private final ReadOnlyDoubleWrapper totalIncome = new ReadOnlyDoubleWrapper();
    private final ReadOnlyDoubleWrapper totalExpense= new ReadOnlyDoubleWrapper();
    private final ReadOnlyDoubleWrapper netBalance = new ReadOnlyDoubleWrapper();

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
            Map.entry("Dylan", 10000)
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
    public ObservableList<MissionLine> missions() { return missions; }
    public IntegerProperty pointsProperty() { return points; }
    public ObservableList<BadgeLine> badges(){ return badges; }
    public ObservableList<Map.Entry<String, Integer>> getLeaderboard() { return leaderboard; }

    public ReadOnlyDoubleProperty totalIncomeProperty(){ return totalIncome.getReadOnlyProperty(); }
    public ReadOnlyDoubleProperty totalExpenseProperty(){ return totalExpense.getReadOnlyProperty(); }
    public ReadOnlyDoubleProperty netBalanceProperty(){ return netBalance .getReadOnlyProperty(); }

    // helpers
    public void addIncome (String d, double a) { incomes .add(new MoneyLine(d, a)); }
    public void addExpense(String d, double a) { expenses.add(new MoneyLine(d, a)); }
    public void addMission(String d, String f, double p) { missions.add(new MissionLine(d, f, p)); }
    public void addBadge(String n, String p, Paint c) { badges.add(new BadgeLine(n, p, c)); }

    /*private void reward() {
        points.set(points.get() + 10);
        if (points.get() >= 50 && !badges.contains("Novice Saver"))
            badges.add("Novice Saver");
    }*/

    public void addPoints(int p) {
        points.set(points.get() + p);
    }

}
