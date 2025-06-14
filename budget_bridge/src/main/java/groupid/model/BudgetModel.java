package groupid.model;

import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import groupid.SecondaryController;
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
    private final IntegerProperty gems = new SimpleIntegerProperty(1000000);   // VIRTUAL CURRENCY
    private final ObservableList<ThemeLine> ownedThemes = FXCollections.observableArrayList(new ThemeLine("Default Blue", Color.web("#202538"),0));
    private final ObjectProperty<ThemeLine> currentTheme = new SimpleObjectProperty<>(new ThemeLine("Default Blue", Color.web("#202538"),0));
    private double primaryIncome, sideIncome, otherIncome;
    private double rent, car, groceries, diningOut, funMoney, otherExpense;
    private List<String> goals = List.of();
    private String budgetPlan = "";
    private League lastRewardedLeague = League.BRONZE;
    private ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
    private ObservableList<ProfileIcon> unlockedProfileIcons = FXCollections.observableArrayList(new ProfileIcon("Classic Avatar", "fas-user", Color.LIGHTBLUE, 0, "Simple and clean profile look"));
    private ObjectProperty<ProfileIcon> currentProfileIcon = new SimpleObjectProperty<>(new ProfileIcon("Classic Avatar", "fas-user", Color.LIGHTBLUE, 0, "Simple and clean profile look"));
    private List<BadgeLine> top3Badges = new ArrayList<>();

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
        public boolean ownsProfileIcon(ProfileIcon profileIcon) {
        return unlockedProfileIcons.contains(profileIcon);
    }
    
    /**
     * Unlock a profile icon for the user
     */
    public void unlockProfileIcon(ProfileIcon profileIcon) {
        if (!unlockedProfileIcons.contains(profileIcon)) {
            unlockedProfileIcons.add(profileIcon);
            MetaDataDAO.saveOwnedIcons(UserDAO.getUserIdByName(username), unlockedProfileIcons);
        }
    }
    
    /**
     * Apply a profile icon as the user's current icon
     */
    public void applyProfileIcon(ProfileIcon profileIcon) {
        if (ownsProfileIcon(profileIcon)) {
            currentProfileIcon.set(profileIcon);
            MetaDataDAO.saveMetaData(UserDAO.getUserIdByName(username), this);
        }
    }
    
    /**
     * Get the currently active profile icon
     */
    public ProfileIcon getCurrentProfileIcon() {
        if (currentProfileIcon == null) {
            currentProfileIcon.set(new ProfileIcon("Classic Avatar", "fas-user", Color.LIGHTBLUE, 100, "Simple and clean profile look"));
        }
        return currentProfileIcon.get();
    }
    
    /**
     * Get the current profile icon property for binding
     */
    public ObjectProperty<ProfileIcon> currentProfileIconProperty() {
        return currentProfileIcon;
    }
    
    /**
     * Get the list of unlocked profile icons
     */
    public ObservableList<ProfileIcon> getUnlockedProfileIcons() {
        return unlockedProfileIcons;
    }
    
    /**
     * Initialize with a default profile icon (call this in your model initialization)
     */
    public void initializeDefaultProfileIcon() {
        // Create a default profile icon that's always available
        ProfileIcon defaultIcon = new ProfileIcon("Default Avatar", "fas-user-circle", 
                                                 javafx.scene.paint.Color.LIGHTGRAY, 0, "Your starting profile");
        unlockProfileIcon(defaultIcon);
        applyProfileIcon(defaultIcon);
    }
    

    public void loadBudgetInfo(BudgetInfo info) {
        incomes().clear();
        if (info.getPrimaryIncome() > 0) addIncome("Primary Job", "fixed", info.getPrimaryIncome());
        if (info.getSideIncome() > 0) addIncome("Side Hustle", "fixed", info.getSideIncome());
        if (info.getOtherIncome() > 0) addIncome("Other Income", "fixed", info.getOtherIncome());

        /* expense lines with limits */
        expenses().clear();
        if (info.getRent() > 0) {
            MoneyLine rentLine = new MoneyLine("Rent/Mortgage", "fixed",info.getRent());
            rentLine.setBudgetLimit(info.getRent());
            expenses.add(rentLine);
        }
        if (info.getCar() > 0) {
            MoneyLine carLine = new MoneyLine("Car Payment", "fixed",info.getCar());
            carLine.setBudgetLimit(info.getCar());
            expenses.add(carLine);
        }
        if (info.getIns() > 0) {
            MoneyLine insLine = new MoneyLine("Insurance", "fixed",info.getIns());
            insLine.setBudgetLimit(info.getCar());
            expenses.add(insLine);
        }

        if (info.getDebt() > 0) {
            MoneyLine debtLine = new MoneyLine("Debt", "fixed",info.getCar());
            debtLine.setBudgetLimit(info.getCar());
            expenses.add(debtLine);
        }
            MoneyLine groceriesLine = new MoneyLine("Groceries", "variable",info.getGroceries());
            groceriesLine.setBudgetLimit(info.getGroceries());
            expenses.add(groceriesLine);

            MoneyLine diningLine = new MoneyLine("Dining Out", "variable",info.getDiningOut());
            diningLine.setBudgetLimit(info.getDiningOut());
            expenses.add(diningLine);
        
            MoneyLine funLine = new MoneyLine("Fun Money", "variable",info.getFunMoney());
            funLine.setBudgetLimit(info.getFunMoney());
            expenses.add(funLine);
        
            MoneyLine otherLine = new MoneyLine("Other", "variable",info.getOtherExpense());
            otherLine.setBudgetLimit(info.getOtherExpense());
            expenses.add(otherLine);

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
            // Map.entry("Alice", 5200),
            // Map.entry("Bob", 5100),
            // Map.entry("Charlie", 5001),
            // Map.entry("Diana", 4800),
            // Map.entry("Ethan", 4200), 
            // Map.entry("Dylan", 4121),
            // Map.entry("Alice", 3400),
            // Map.entry("Bob", 3221),
            // Map.entry("Charlie", 3200),
            // Map.entry("Diana", 3100),
            // Map.entry("Ethan", 3000), 
            // Map.entry("Dylan", 2900),
            // Map.entry("Alice", 2500),
            // Map.entry("Bob", 2200),
            // Map.entry("Charlie", 1000),
            // Map.entry("Diana", 200),
            // Map.entry("Ethan", 110), 
            // Map.entry("Dylan", 100)
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
    
    public void applyTheme(ThemeLine theme) { 
        currentTheme.set(theme); 
        MetaDataDAO.saveMetaData(UserDAO.getUserIdByName(username), this); 
    }

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
    public List<BadgeLine> getTop3Badges() {return top3Badges; }

    // helpers
    public void addIncome (String d, String f, double a) { incomes .add(new MoneyLine(d, f, a));  }
    public void addExpense(String d, String f, double a) { expenses.add(new MoneyLine(d, f, a));  }
    public void addMission(Integer i) { missions.add(missionsList.get(i)); }
    public void addMissionList(String d, String f, double a) { missionsList.add(new MissionLine(d, f, a)); }
    public void setRankPos(String r) { leaderboardPos.set(r + "."); }
    public void setGemsStart(int amount) { gems.set(amount); }
    public void setPointsStart(int amount) { points.set(amount); }
    public void applyThemeStart(ThemeLine theme) { currentTheme.set(theme); }
    public void applyProfileIconStart(ProfileIcon icon) { currentProfileIcon.set(icon); }
    public void unlockBadgeStart(BadgeLine badge) { badges.add(badge); }
    public void unlockThemeStart(ThemeLine theme) {ownedThemes.add(theme); }
    public void unlockProfileIconStart(ProfileIcon icon) {unlockedProfileIcons.add(icon); }
    public void setGems(int amount) { gems.set(amount); MetaDataDAO.saveMetaData(UserDAO.getUserIdByName(username), this); }
    public boolean ownsBadge(BadgeLine badge) { return badges.stream().anyMatch(b -> b.getName().equals(badge.getName())); }
    public void unlockBadge(BadgeLine badge) {
        if (!ownsBadge(badge)) { badges.add(badge); MetaDataDAO.saveOwnedBadges(UserDAO.getUserIdByName(username), badges); } }
    public boolean ownsTheme(ThemeLine theme) { return ownedThemes.stream().anyMatch(t -> t.getName().equals(theme.getName())); }
    public void unlockTheme(ThemeLine theme) {
        if (!ownsTheme(theme)) { ownedThemes.add(theme); MetaDataDAO.saveOwnedThemes(UserDAO.getUserIdByName(username), ownedThemes); } }
    public void setTop3Badges(List<BadgeLine> threeBadges) {
        top3Badges.clear();
        top3Badges.addAll(threeBadges);
    }

    


    private void unlockLeagueReward(League league) {
        switch (league) {
            case COPPER -> {
                setGems(getGems().get() + 250);
                unlockBadge(new BadgeLine("Copper Challenger", "fas-award", Color.web("#b87333"), 250));
            }
            case SILVER -> {
                setGems(getGems().get() + 500);
                unlockBadge(new BadgeLine("Silver Sprinter", "fas-trophy", Color.web("#c0c0c0"), 500));
            }
            case GOLD -> {
                setGems(getGems().get() + 1000);
                unlockBadge(new BadgeLine("Gold Achiever", "fas-star", Color.web("#ffd700"), 1000));
            }
            case PLATINUM -> {
                setGems(getGems().get() + 2000);
                unlockBadge(new BadgeLine("Platinum Hero", "fas-shield-alt", Color.web("#e5e4e2"), 2000));
            }
            case DIAMOND -> {
                setGems(getGems().get() + 5000);
                unlockBadge(new BadgeLine("Diamond Legend", "fas-gem", Color.web("#b9f2ff"), 5000));
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

    public void refreshCurrentMonthFromBase() {
        currentMonthExpenses.clear();
        for (MoneyLine e : expenses) {
            if (!e.getFreq().equals("fixed")) { // Only recurring ones
                currentMonthExpenses.add(new MoneyLine(e.getType(), e.getFreq(), e.getAmount()));
            }
        }
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
        MetaDataDAO.saveMetaData(UserDAO.getUserIdByName(username), this);
    }

    public League getLastRewardedLeague() {
        return lastRewardedLeague;
    }

    public void setLastRewardedLeague(League league) {
        this.lastRewardedLeague = league;
    }

    public void generateCustomBudget(BudgetInfo info) {
        expenses.clear(); // Reset
        double income = info.getPrimaryIncome() + info.getSideIncome() + info.getOtherIncome();
        double fixed = info.getRent() + info.getCar() + info.getDebt();
        
        // Add fixed expenses first
        if (info.getRent() > 0) addExpense("Rent", "fixed", info.getRent());
        if (info.getCar() > 0) addExpense("Car Payment", "fixed", info.getCar());
        if (info.getDebt() > 0) addExpense("Other Debt", "fixed", info.getDebt());
        
        double remaining = income - fixed;
        if (remaining < 0) remaining = 0;
        
        // Define plan-based budget allocation
        String plan = info.getBudgetPlan();
        double savingsPct = 0.15; // Base savings
        double groceriesPct = 0.50; // Base groceries percentage
        double diningPct = 0.20; // Base dining percentage
        double funPct = 0.30; // Base fun money percentage
        boolean includeExtraDebt = false;
        double extraDebtPct = 0.0;
        
        // Plan-specific rules
        if (plan.equals("College Student")) {
            savingsPct = 0.05; // Minimal savings, focus on survival
            groceriesPct = 0.60; // More on groceries, less dining
            diningPct = 0.10;
            funPct = 0.30;
        } else if (plan.equals("Building My Career")) {
            savingsPct = 0.20; // Building wealth
            groceriesPct = 0.40; // Balanced lifestyle
            diningPct = 0.30; // More social dining
            funPct = 0.30;
            // Include extra debt payments if no existing debt
            if (info.getDebt() == 0) {
                includeExtraDebt = true;
                extraDebtPct = 0.05;
            }
        } else if (plan.equals("Family Planning")) {
            savingsPct = 0.15; // Moderate savings
            groceriesPct = 0.65; // Family groceries priority
            diningPct = 0.15; // Less dining out
            funPct = 0.20; // Reduced fun money
        } else if (plan.equals("Doing Well Financially")) {
            savingsPct = 0.30; // Aggressive savings
            if (income >= 8000) savingsPct = 0.35; // Even more for high earners
            groceriesPct = 0.35; // Can afford quality groceries
            diningPct = 0.35; // Lifestyle dining
            funPct = 0.30;
            // Extra debt payments to eliminate debt faster
            if (info.getDebt() == 0) {
                includeExtraDebt = true;
                extraDebtPct = 0.10;
            }
        } else if (plan.equals("Nearing Retirement")) {
            savingsPct = 0.25; // Catch-up savings
            if (income >= 6000) savingsPct = 0.30; // More if higher income
            groceriesPct = 0.50; // Conservative grocery spending
            diningPct = 0.20; // Moderate dining
            funPct = 0.30;
            // Focus on debt elimination before retirement
            if (info.getDebt() == 0) {
                includeExtraDebt = true;
                extraDebtPct = 0.15;
            }
        }
        
        // Apply income-based adjustments
        if (income >= 5000) savingsPct += 0.05;
        
        // Don't go below 5% or above 50% for savings
        savingsPct = Math.max(0.05, Math.min(0.50, savingsPct));
        
        double savings = savingsPct * remaining;
        addExpense("Savings", "variable", savings);
        
        // Extra toward debt if plan includes it but current debt amount = 0
        if (includeExtraDebt && info.getDebt() == 0) {
            double extraDebt = extraDebtPct * remaining;
            addExpense("Debt Payments", "variable", extraDebt);
            remaining -= extraDebt;
        }
        
        remaining -= savings;
        if (remaining < 0) remaining = 0;
        
        // Remaining split based on plan percentages
        double groceries = remaining * groceriesPct;
        double dining = remaining * diningPct;
        double fun = remaining * funPct;
        
        addExpense("Groceries", "variable", groceries);
        addExpense("Dining Out", "variable", dining);
        addExpense("Fun Money", "variable", fun);
    }
}
