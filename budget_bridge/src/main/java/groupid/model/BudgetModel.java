package groupid.model;

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
    private final StringProperty username = new SimpleStringProperty("Jack S Smith");
    private final ObservableList<MoneyLine> incomes = FXCollections.observableArrayList();
    private final ObservableList<MoneyLine> expenses = FXCollections.observableArrayList();

    // gamification
    private final IntegerProperty points = new SimpleIntegerProperty(0);
    private final ObservableList<String> badges = FXCollections.observableArrayList();

    // convenience derived values (totals, net)

    private final ReadOnlyDoubleWrapper totalIncome = new ReadOnlyDoubleWrapper();
    private final ReadOnlyDoubleWrapper totalExpense= new ReadOnlyDoubleWrapper();
    private final ReadOnlyDoubleWrapper netBalance = new ReadOnlyDoubleWrapper();

    public BudgetModel() {
        InvalidationListener recalc = obs -> recalcTotals();
        incomes .addListener(recalc);
        expenses.addListener(recalc);
        recalc.invalidated(null);
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
