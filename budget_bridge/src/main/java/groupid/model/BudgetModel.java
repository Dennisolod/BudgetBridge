package groupid.model;

import java.time.LocalDate;
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
import javafx.scene.chart.PieChart;
import javafx.scene.paint.Color;
import groupid.model.League;


public class BudgetModel {

    // user‑enterable fields
    private final StringProperty username = new SimpleStringProperty();
    private final ObservableList<MoneyLine> incomes = FXCollections.observableArrayList();
    private final ObservableList<MoneyLine> expenses = FXCollections.observableArrayList();
    private final ObservableList<MoneyLine> currentMonthExpenses = FXCollections.observableArrayList();
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
    private ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();


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
        if (info.getPrimaryIncome() > 0) addIncome("Primary Job", "Monthly", info.getPrimaryIncome());
        if (info.getSideIncome() > 0) addIncome("Side Hustle", "Monthly", info.getSideIncome());
        if (info.getOtherIncome() > 0) addIncome("Other Income", "Monthly", info.getOtherIncome());

        /* expense lines with limits */
        expenses().clear();
        if (info.getRent() > 0) {
            MoneyLine rentLine = new MoneyLine("Rent/Mortgage", "Monthly",info.getRent());
            rentLine.setBudgetLimit(info.getRent());
            expenses.add(rentLine);
        }
        if (info.getCar() > 0) {
            MoneyLine carLine = new MoneyLine("Car Payment", "Monthly",info.getCar());
            carLine.setBudgetLimit(info.getCar());
            expenses.add(carLine);
        }
        if (info.getGroceries() > 0) {
            MoneyLine groceriesLine = new MoneyLine("Groceries", "Monthly",info.getGroceries());
            groceriesLine.setBudgetLimit(info.getGroceries());
            expenses.add(groceriesLine);
        }
        if (info.getDiningOut() > 0) {
            MoneyLine diningLine = new MoneyLine("Dining Out", "Monthly",info.getDiningOut());
            diningLine.setBudgetLimit(info.getDiningOut());
            expenses.add(diningLine);
        }
        if (info.getFunMoney() > 0) {
            MoneyLine funLine = new MoneyLine("Fun Money", "Monthly",info.getFunMoney());
            funLine.setBudgetLimit(info.getFunMoney());
            expenses.add(funLine);
        }
        if (info.getOtherExpense() > 0) {
            MoneyLine otherLine = new MoneyLine("Other", "Monthly",info.getOtherExpense());
            otherLine.setBudgetLimit(info.getOtherExpense());
            expenses.add(otherLine);
        }

        // Initialize current month after loading base expenses
        initializeNewMonth();
        
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


    public void initializeNewMonth() {
        currentMonthExpenses.clear();
        
        expenses.forEach(baseExpense -> {
            MoneyLine monthlyExpense = new MoneyLine(
                baseExpense.getType(),
                baseExpense.getFreq(), 
                baseExpense.getAmount(), // Start with base amount
                LocalDate.now()
            );
            currentMonthExpenses.add(monthlyExpense);
        });
    }

    public void endMonth() {
    // Optional: Save current month for historical tracking
        //saveMonthlyReport(currentMonthExpenses);
        
        // Reset to base amounts for next month
        initializeNewMonth();
    }
    /* 
    public void checkForNewMonth() {
        LocalDate lastAppRun = getLastAppRunDate(); // From preferences
        LocalDate today = LocalDate.now();
        
        if (lastAppRun.getMonth() != today.getMonth() || 
            lastAppRun.getYear() != today.getYear()) {
            
            // Only reset if it's actually a new month
            initializeNewMonth();
        }
        
        saveLastAppRunDate(today);
    }
    */


    // getters
    public StringProperty usernameProperty() { return username; }
    public ObservableList<MoneyLine> incomes() { return incomes; }
    public ObservableList<MoneyLine> expenses(){ return expenses; }
    public ObservableList<MoneyLine> getCurrentMonth() { return currentMonthExpenses; }
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
    public ObservableList<PieChart.Data> pieDataProperty() {return pieData; }
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
    public void addIncome (String d, String f, double a) { incomes .add(new MoneyLine(d, f, a));  }
    public void addExpense(String d, String f, double a) { expenses.add(new MoneyLine(d, f, a));  }
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

    public int getCostForBadge(BadgeLine badge) {
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

    public void generateCustomBudget(BudgetInfo info) {
        expenses().clear();

        // Income
        double totalIncome = info.getPrimaryIncome() + info.getSideIncome() + info.getOtherIncome();

        // Fixed expenses (user-defined)
        addExpense("Rent/Mortgage", "fixed", info.getRent());
        addExpense("Car Payment", "fixed", info.getCar());
        addExpense("Other Fixed Expenses", "fixed", info.getOtherExpense());

        double fixedExpenses = info.getRent() + info.getCar() + info.getOtherExpense();
        double discretionary = totalIncome - fixedExpenses;

        if (discretionary <= 0) {
            addExpense("Groceries", "variable", 0);
            addExpense("Dining Out", "variable", 0);
            addExpense("Fun Money", "variable", 0);
            addExpense("Savings", "variable", 0);
            return;
        }

        // Lifestyle presets
        double groceriesPct = 0.25;
        double diningPct = 0.20;
        double funPct = 0.15;
        double savingsPct = 0.40;

        switch (info.getBudgetPlan()) {
            case "College Student" -> {
                groceriesPct = 0.30;
                diningPct = 0.25;
                funPct = 0.25;
                savingsPct = 0.20;
            }
            case "Part-Time Worker" -> {
                groceriesPct = 0.25;
                diningPct = 0.25;
                funPct = 0.20;
                savingsPct = 0.30;
            }
            case "Young Professional" -> {
                groceriesPct = 0.20;
                diningPct = 0.20;
                funPct = 0.20;
                savingsPct = 0.40;
            }
            case "Living With Parents" -> {
                groceriesPct = 0.15;
                diningPct = 0.25;
                funPct = 0.25;
                savingsPct = 0.35;
            }
            case "Freelancer" -> {
                groceriesPct = 0.25;
                diningPct = 0.15;
                funPct = 0.15;
                savingsPct = 0.45;
            }
        }

        // Adjustments based on goals
        List<String> goals = info.getGoals();
        boolean saveEmergency = goals.contains("Build Emergency Fund");
        boolean saveRetirement = goals.contains("Invest for Retirement");
        boolean saveVacation = goals.contains("Save for Vacation");
        boolean payOffDebt = goals.contains("Pay Off Debt");

        if (saveVacation) {
            funPct += 0.05;
            diningPct -= 0.05;
        }

        if (payOffDebt) {
            savingsPct -= 0.10;
            addExpense("Debt Payments", "variable", 0.10 * discretionary);
        }

        // Normalize percentages for remaining pool
        double pctSum = groceriesPct + diningPct + funPct + Math.max(savingsPct, 0);
        groceriesPct /= pctSum;
        diningPct /= pctSum;
        funPct /= pctSum;
        savingsPct = Math.max(0, savingsPct / pctSum);  // Ensure non-negative

        // Apply expenses
        addExpense("Groceries", "variable", discretionary * groceriesPct);
        addExpense("Dining Out", "variable", discretionary * diningPct);
        addExpense("Fun Money", "variable", discretionary * funPct);

        // Split savings if goals chosen
        double savingsTotal = discretionary * savingsPct;
        if (saveEmergency && saveRetirement) {
            addExpense("Emergency Fund", "variable", savingsTotal * 0.5);
            addExpense("Retirement Savings", "variable", savingsTotal * 0.5);
        } else if (saveEmergency) {
            addExpense("Emergency Fund", "variable", savingsTotal);
        } else if (saveRetirement) {
            addExpense("Retirement Savings", "variable", savingsTotal);
        } else {
            addExpense("Savings", "variable", savingsTotal);
        }
    }


}
