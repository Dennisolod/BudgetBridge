package groupid.model;

import java.util.AbstractMap;
import java.util.Comparator;
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
import javafx.scene.paint.Color;
import groupid.model.League;


public class BudgetModel {

    // user‑enterable fields
    private final StringProperty username = new SimpleStringProperty();
    private final ObservableList<MoneyLine> incomes = FXCollections.observableArrayList();
    private final ObservableList<MoneyLine> expenses = FXCollections.observableArrayList();
    private final ObservableList<MissionLine> missions = FXCollections.observableArrayList();
    private final ObservableList<MissionLine> missionsList = FXCollections.observableArrayList();
    private final ObservableList<BadgeLine> badges = FXCollections.observableArrayList();       // PLAYER OWNED BADGES

    // gamification
    private final IntegerProperty points = new SimpleIntegerProperty(0);
    private final ObservableList<Map.Entry<String, Integer>> leaderboard = FXCollections.observableArrayList();
    private final StringProperty rank = new SimpleStringProperty();
    private final StringProperty leaderboardPos = new SimpleStringProperty();            // LEADERBOARD POINTS
    private final IntegerProperty gems = new SimpleIntegerProperty(10000);   // VIRTUAL CURRENCY
    private final ObservableList<ThemeLine> ownedThemes = FXCollections.observableArrayList();
    private final ObjectProperty<ThemeLine> currentTheme = new SimpleObjectProperty<>();
    private double primaryIncome, sideIncome, otherIncome;
    private double rent, car, groceries, diningOut, funMoney, otherExpense;
    private List<String> goals = List.of();
    private String budgetPlan = "";
    private League lastRewardedLeague = League.BRONZE;


    // public enum League { BRONZE, COPPER, SILVER, GOLD, PLATINUM, DIAMOND }


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
        incomes().clear();
        if (info.getPrimaryIncome() > 0) addIncome("Primary Job",  info.getPrimaryIncome());
        if (info.getSideIncome()   > 0) addIncome("Side Hustle",   info.getSideIncome());
        if (info.getOtherIncome()  > 0) addIncome("Other Income",  info.getOtherIncome());

        /* expense lines */
        expenses().clear();
        if (info.getRent()        > 0) addExpense("Rent/Mortgage",  info.getRent());
        if (info.getCar()         > 0) addExpense("Car Payment",    info.getCar());
        if (info.getGroceries()   > 0) addExpense("Groceries",      info.getGroceries());
        if (info.getDiningOut()   > 0) addExpense("Dining Out",     info.getDiningOut());
        if (info.getFunMoney()    > 0) addExpense("Fun Money",      info.getFunMoney());
        if (info.getOtherExpense()> 0) addExpense("Other",          info.getOtherExpense());

        recalcTotals();
    }


    public BudgetModel() {
        InvalidationListener recalc = obs -> recalcTotals();
        incomes .addListener(recalc);
        expenses.addListener(recalc);
        recalc.invalidated(null);

        leaderboard.addAll(
            Map.entry("Alice", 5200),
            Map.entry("Bob", 5100),
            Map.entry("Charlie", 5001),
            Map.entry("Diana", 4800),
            Map.entry("Ethan", 4200), 
            Map.entry("Dylan", 4121),
            Map.entry("Alice", 3400),
            Map.entry("Bob", 3221),
            Map.entry("Charlie", 3200),
            Map.entry("Diana", 3100),
            Map.entry("Ethan", 3000), 
            Map.entry("Dylan", 2900),
            Map.entry("Alice", 2500),
            Map.entry("Bob", 2200),
            Map.entry("Charlie", 1000),
            Map.entry("Diana", 200),
            Map.entry("Ethan", 110), 
            Map.entry("Dylan", 100)
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
    public ObservableList<MissionLine> missionsList() { return missionsList; }
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
    public League getCurrentLeague() {
        int points = pointsProperty().get();
        if (points >= 60000) return League.DIAMOND;
        if (points >= 40000) return League.PLATINUM;
        if (points >= 20000) return League.GOLD;
        if (points >= 10000) return League.SILVER;
        if (points >= 5000)  return League.COPPER;
        return League.BRONZE;
    }

    public ReadOnlyDoubleProperty totalIncomeProperty(){ return totalIncome.getReadOnlyProperty(); }
    public ReadOnlyDoubleProperty totalExpenseProperty(){ return totalExpense.getReadOnlyProperty(); }
    public ReadOnlyDoubleProperty netBalanceProperty(){ return netBalance .getReadOnlyProperty(); }

    // helpers
    public void addIncome (String d, double a) { incomes .add(new MoneyLine(d, a));  }
    public void addExpense(String d, double a) { expenses.add(new MoneyLine(d, a));  }
    public void addMission(Integer i) { missions.add(missionsList.get(i)); }
    public void addMissionList(String d, String f, double a) { missionsList.add(new MissionLine(d, f, a)); }
    public void setRankPos(String r) { leaderboardPos.set(r + "."); }
    public void setGems(int amount) { gems.set(amount); }
    public boolean ownsBadge(BadgeLine badge) { return badges.stream().anyMatch(b -> b.getName().equals(badge.getName())); }
    public void unlockBadge(BadgeLine badge) { if (!ownsBadge(badge)) { badges.add(badge); } }
    public boolean ownsTheme(ThemeLine theme) { return ownedThemes.stream().anyMatch(t -> t.getName().equals(theme.getName())); }
    public void unlockTheme(ThemeLine theme) { if (!ownsTheme(theme)) { ownedThemes.add(theme); } }

    private void unlockLeagueReward(League league) {
        switch (league) {
            case COPPER -> {
                setGems(getGems().get() + 250);
                unlockBadge(new BadgeLine("Copper Challenger", "fas-award", Color.web("#b87333")));
            }
            case SILVER -> {
                setGems(getGems().get() + 500);
                unlockBadge(new BadgeLine("Silver Sprinter", "fas-trophy", Color.web("#c0c0c0")));
            }
            case GOLD -> {
                setGems(getGems().get() + 1000);
                unlockBadge(new BadgeLine("Gold Achiever", "fas-star", Color.web("#ffd700")));
            }
            case PLATINUM -> {
                setGems(getGems().get() + 2000);
                unlockBadge(new BadgeLine("Platinum Hero", "fas-shield-alt", Color.web("#e5e4e2")));
            }
            case DIAMOND -> {
                setGems(getGems().get() + 5000);
                unlockBadge(new BadgeLine("Diamond Legend", "fas-gem", Color.web("#b9f2ff")));
            }
        }
    }

    public List<BadgeLine> getTop3BadgeLines(String username) {
    return badges.stream()
        .sorted(Comparator.comparingInt(this::getCostForBadge).reversed())
        .limit(3)
        .toList();
    }

    private int getCostForBadge(BadgeLine badge) {
    return switch (badge.getName()) {
        case "Gold Trophy" -> 200;
        case "Shield of Honor" -> 180;
        case "Mythic Phoenix" -> 500;
        case "Crown Elite" -> 250;
        case "Champion Medal" -> 300;
        case "Secret Flame" -> 220;
        case "Infinity Crown" -> 600;
        case "Legend of Time" -> 750;
        case "Quantum Vault" -> 900;
        case "Ethereal Wings" -> 1000;
        case "Celestial Flame" -> 1500;
        case "Eternal Crown" -> 2000;
        case "Timekeeper's Halo" -> 2500;
        case "Quantum Ascendant" -> 3000;
        case "Divine Architect" -> 4000;
        default -> 0;
    };
}

    // add points to the player for their leaderboard position 
    // and if they progress to a new league
    public void addPoints(int p) {
        points.set(points.get() + p);

        League currentLeague = getCurrentLeague();

        if (currentLeague.ordinal() > lastRewardedLeague.ordinal()) {
            unlockLeagueReward(currentLeague);
            setLastRewardedLeague(currentLeague);
        }
    }

    public League getLastRewardedLeague() {
        return lastRewardedLeague;
    }

    public void setLastRewardedLeague(League league) {
        this.lastRewardedLeague = league;
    }

}
