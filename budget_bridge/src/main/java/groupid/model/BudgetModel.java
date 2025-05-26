package groupid.model;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

import javafx.beans.InvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class BudgetModel {

    // user‑enterable fields
    private final StringProperty username = new SimpleStringProperty();
    private final ObservableList<MoneyLine> incomes = FXCollections.observableArrayList();
    private final ObservableList<MoneyLine> expenses = FXCollections.observableArrayList();
    private final ObservableList<MissionLine> missions = FXCollections.observableArrayList();
    private final ObservableList<BadgeLine> badges = FXCollections.observableArrayList();       // PLAYER OWNED BADGES

    // gamification
    private final IntegerProperty points = new SimpleIntegerProperty(0);
    private final ObservableList<Map.Entry<String, Integer>> leaderboard = FXCollections.observableArrayList();
    private final StringProperty rank = new SimpleStringProperty();
    private final StringProperty leaderboardPos = new SimpleStringProperty();            // LEADERBOARD POINTS
    private final IntegerProperty gems = new SimpleIntegerProperty(1000000);   // VIRTUAL CURRENCY
    private final ObservableList<ThemeLine> ownedThemes = FXCollections.observableArrayList();
    private final ObjectProperty<ThemeLine> currentTheme = new SimpleObjectProperty<>();
    private double primaryIncome, sideIncome, otherIncome;
    private double rent, car, groceries, diningOut, funMoney, otherExpense;
    private List<String> goals = List.of();
    private String budgetPlan = "";



    // convenience derived values (totals, net)
    private final ReadOnlyDoubleWrapper totalIncome = new ReadOnlyDoubleWrapper();
    private final ReadOnlyDoubleWrapper totalExpense= new ReadOnlyDoubleWrapper();
    private final ReadOnlyDoubleWrapper netBalance = new ReadOnlyDoubleWrapper();

    // helper method to add the user to the leaderboard
    public void addUserToLeaderboard(String userName, int points) {
        // Remove existing entry for username if it exists
        leaderboard.removeIf(entry -> entry.getKey().equals(userName));

        // Add new entry
        Map.Entry<String, Integer> newEntry = new AbstractMap.SimpleEntry<String, Integer>(userName, points);
        leaderboard.add(newEntry);

        // Sort the leaderboard in descending order of points
        leaderboard.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
    }

    public void loadBudgetInfo(BudgetInfo info) {
        primaryIncome = info.getPrimaryIncome();
        sideIncome = info.getSideIncome();
        otherIncome = info.getOtherIncome();

        rent = info.getRent();
        car = info.getCar();
        groceries = info.getGroceries();
        diningOut = info.getDiningOut();
        funMoney = info.getFunMoney();
        otherExpense = info.getOtherExpense();
        goals = info.getGoals();
        budgetPlan = info.getBudgetPlan();
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
    public StringProperty rankProperty() { return rank; }
    public StringProperty getLeaderboardPos() { return leaderboardPos; }
    public IntegerProperty getGems() { return gems; }
    public ObservableList<BadgeLine> getOwnedBadges() { return badges; }
    public ObservableList<ThemeLine> getOwnedThemes() { return ownedThemes; }
    public ThemeLine getCurrentTheme() { return currentTheme.get(); }
    public void applyTheme(ThemeLine theme) { currentTheme.set(theme); }
    public ObjectProperty<ThemeLine> currentThemeProperty() { return currentTheme; }

    public ReadOnlyDoubleProperty totalIncomeProperty(){ return totalIncome.getReadOnlyProperty(); }
    public ReadOnlyDoubleProperty totalExpenseProperty(){ return totalExpense.getReadOnlyProperty(); }
    public ReadOnlyDoubleProperty netBalanceProperty(){ return netBalance .getReadOnlyProperty(); }

    // helpers
    public void addIncome (String d, double a) { incomes .add(new MoneyLine(d, a));  }
    public void addExpense(String d, double a) { expenses.add(new MoneyLine(d, a));  }
    public void addMission(String d, String f, double a) { missions.add(new MissionLine(d, f, a)); }
    public void setRankPos(String r) { leaderboardPos.set(r + "."); }
    public void setGems(int amount) { gems.set(amount); }
    public boolean ownsBadge(BadgeLine badge) { return badges.stream().anyMatch(b -> b.getName().equals(badge.getName())); }
    public void unlockBadge(BadgeLine badge) { if (!ownsBadge(badge)) { badges.add(badge); } }
    public boolean ownsTheme(ThemeLine theme) { return ownedThemes.stream().anyMatch(t -> t.getName().equals(theme.getName())); }
    public void unlockTheme(ThemeLine theme) { if (!ownsTheme(theme)) { ownedThemes.add(theme); } }

    /*private void reward() {
        points.set(points.get() + 10);
        if (points.get() >= 50 && !badges.contains("Novice Saver"))
            badges.add("Novice Saver");
    }*/

    // add points to the player for their leaderboard position
    public void addPoints(int p) {
        points.set(points.get() + p);
    }

}
