package groupid;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

import groupid.model.BudgetInfoDAO;
import groupid.model.BudgetModel;
import groupid.model.CooldownManager;
import groupid.model.MoneyLine;
import groupid.model.UserDAO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

/** Budget screen (secondary.fxml) */
public class SecondaryController implements ModelAware {

    /* ── Income controls ──────────────────────────────────────────── */
    @FXML private ComboBox<String>  incomeFreqCombo;   // Daily / Weekly / Monthly
    @FXML private ComboBox<String>  incomeCatCombo;    // Salary, Freelance, …
    @FXML private TextField         incomeAmountInput;
    @FXML private ListView<MoneyLine> incomeList;
    @FXML private VBox              incomeInputSection; // The collapsible section
    @FXML private Button            addIncomeToggleButton; // The toggle button
    @FXML private CheckBox          incomeRecurringCheckBox; // Recurring checkbox
    @FXML private VBox              incomeFreqSection; // Frequency section container

    /* ── Expense controls ─────────────────────────────────────────── */
    @FXML private ComboBox<String>  expenseFreqCombo;
    @FXML private ComboBox<String>  expenseCatCombo;
    @FXML private TextField         expenseAmountInput;
    @FXML private ListView<MoneyLine> expenseList;
    @FXML private VBox              expenseInputSection; // The collapsible section
    @FXML private Button            addExpenseToggleButton; // The toggle button
    @FXML private CheckBox          expenseRecurringCheckBox; // Recurring checkbox
    @FXML private VBox              expenseFreqSection; // Frequency section container

    /* ── Summary labels ───────────────────────────────────────────── */
    @FXML private Label totalIncomeLabel, totalExpenseLabel, netLabel;

    /* ── Mission / reward UI (unchanged) ──────────────────────────── */
    @FXML private Button dailyRewardButton, weeklyRewardButton, monthlyRewardButton;
    @FXML private Label missionDaily, missionWeekly, missionMonthly;

    private BudgetModel model;

    /* ── Initialisation (called automatically) ───────────────────── */
    @FXML
    private void initialize() {
        
        /* 1 ▸ shared frequency list */
        ObservableList<String> freqItems = FXCollections.observableArrayList(
                "Daily", "Weekly", "Monthly");
        incomeFreqCombo .setItems(freqItems);
        expenseFreqCombo.setItems(freqItems);

        /* 2 ▸ category seeds */
        incomeCatCombo .setItems(FXCollections.observableArrayList(
                "Salary", "Freelance", "Bonus", "Investment", "Other"));
        expenseCatCombo.setItems(FXCollections.observableArrayList(
                "Rent", "Groceries", "Utilities", "Entertainment", "Other"));
        incomeCatCombo .setEditable(true);
        expenseCatCombo.setEditable(true);

        /* 3 ▸ ENTER submits in amount fields */
        incomeAmountInput .setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) addInputIncome();
        });
        expenseAmountInput.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) addInputExpense();
        });

        /* 4 ▸ numeric filters (##.##) */
        TextFormatter<String> moneyFmt = new TextFormatter<>(
                c -> c.getControlNewText().matches("\\d*(\\.\\d{0,2})?") ? c : null);
        incomeAmountInput .setTextFormatter(moneyFmt);
        expenseAmountInput.setTextFormatter(new TextFormatter<>(moneyFmt.getFilter()));

        /* 5 ▸ Set up recurring checkbox listeners */
        if (incomeRecurringCheckBox != null && incomeFreqSection != null) {
            incomeRecurringCheckBox.setOnAction(e -> toggleIncomeFrequency());
        }
        if (expenseRecurringCheckBox != null && expenseFreqSection != null) {
            expenseRecurringCheckBox.setOnAction(e -> toggleExpenseFrequency());
        }
    }

    /* ── Model injection ──────────────────────────────────────────── */
    @Override
    public void setModel(BudgetModel m) {
        model = m;
        incomeList .setItems(m.incomes());
        expenseList.setItems(m.getCurrentMonth());
        m.incomes() .addListener((ListChangeListener<MoneyLine>) c -> updateTotals());
        m.expenses().addListener((ListChangeListener<MoneyLine>) c -> updateTotals());
        updateTotals();

        m.refreshCurrentMonthFromBase();
        expenseList.setItems(m.getCurrentMonth());

        missionDaily  .setText(m.missions().get(0).toString());
        missionWeekly .setText(m.missions().get(1).toString());
        missionMonthly.setText(m.missions().get(2).toString());
        
        /* Initialize collapsible sections here, after FXML injection */
        if (incomeInputSection != null) {
            incomeInputSection.setVisible(false);
            incomeInputSection.setManaged(false);
        }
        if (expenseInputSection != null) {
            expenseInputSection.setVisible(false);
            expenseInputSection.setManaged(false);
        }
        
        /* Initialize frequency sections (hidden by default) */
        if (incomeFreqSection != null) {
            incomeFreqSection.setVisible(false);
            incomeFreqSection.setManaged(false);
        }
        if (expenseFreqSection != null) {
            expenseFreqSection.setVisible(false);
            expenseFreqSection.setManaged(false);
        }
        // Set up cooldown listeners to update button text dynamically
        CooldownManager cooldownManager = CooldownManager.getInstance();
        
        // Update buttons initially
        updateRewardButtons();
        
        // Set up listeners for dynamic updates
        cooldownManager.secondsLeftProperty("daily").addListener((obs, old, newVal) -> {
            Platform.runLater(this::updateRewardButtons);
        });
        
        cooldownManager.secondsLeftProperty("weekly").addListener((obs, old, newVal) -> {
            Platform.runLater(this::updateRewardButtons);
        });
        
        cooldownManager.secondsLeftProperty("monthly").addListener((obs, old, newVal) -> {
            Platform.runLater(this::updateRewardButtons);
        });
    }

    /* ── Toggle income input section ─────────────────────────────── */
    @FXML
    private void toggleIncomeInput() {
        if (incomeInputSection == null || addIncomeToggleButton == null) return;
        
        boolean isVisible = incomeInputSection.isVisible();
        incomeInputSection.setVisible(!isVisible);
        incomeInputSection.setManaged(!isVisible);
        addIncomeToggleButton.setText(isVisible ? "Add Income" : "Hide Income Form");
    }

    /* ── Toggle frequency sections based on recurring checkbox ────── */
    private void toggleIncomeFrequency() {
        if (incomeRecurringCheckBox == null || incomeFreqSection == null) return;
        
        boolean isRecurring = incomeRecurringCheckBox.isSelected();
        incomeFreqSection.setVisible(isRecurring);
        incomeFreqSection.setManaged(isRecurring);
        
        // Clear frequency selection if unchecked
        if (!isRecurring) {
            incomeFreqCombo.setValue(null);
        }
    }

    private void toggleExpenseFrequency() {
        if (expenseRecurringCheckBox == null || expenseFreqSection == null) return;
        
        boolean isRecurring = expenseRecurringCheckBox.isSelected();
        expenseFreqSection.setVisible(isRecurring);
        expenseFreqSection.setManaged(isRecurring);
        
        // Clear frequency selection if unchecked
        if (!isRecurring) {
            expenseFreqCombo.setValue(null);
        }
    }
    @FXML
    private void toggleExpenseInput() {
        if (expenseInputSection == null || addExpenseToggleButton == null) return;
        
        boolean isVisible = expenseInputSection.isVisible();
        expenseInputSection.setVisible(!isVisible);
        expenseInputSection.setManaged(!isVisible);
        addExpenseToggleButton.setText(isVisible ? "Add Expense" : "Hide Expense Form");
    }

    /* ── Add new income ───────────────────────────────────────────── */
    @FXML
    private void addInputIncome() {

        /* amount */
        double amt;
        try { amt = Double.parseDouble(incomeAmountInput.getText().trim());
              if (amt <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showWarn("Enter a positive numeric amount.");
            return;
        }

        /* frequency */
        String freq = null;
        if (incomeRecurringCheckBox != null && incomeRecurringCheckBox.isSelected()) {
            freq = incomeFreqCombo.getValue();
            if (freq == null) { 
                showWarn("Select income frequency for recurring income."); 
                return; 
            }
        } else {
            freq = "One-time"; // Default for non-recurring
        }

        /* category */
        String cat = Optional.ofNullable(incomeCatCombo.getEditor().getText())
                             .orElse("").trim();
        if (cat.isEmpty()) { showWarn("Enter or select an income category."); return; }

        /* persist */
        model.addIncome(cat, freq, amt);
        BudgetInfoDAO.updateBudgetInfoForUser(UserDAO.getUserIdByName(model.usernameProperty()), model);

        /* remember custom category */
        if (!incomeCatCombo.getItems().contains(cat))
            incomeCatCombo.getItems().add(cat);

        /* clear */
        incomeAmountInput.clear();
        incomeFreqCombo.setValue(null);
        incomeCatCombo.getSelectionModel().clearSelection();
        incomeCatCombo.getEditor().clear();
        incomeRecurringCheckBox.setSelected(false);
        toggleIncomeFrequency(); // Hide frequency section
    }

    /* ── Add new expense (unchanged except method call) ───────────── */
    @FXML
    private void addInputExpense() {

        double amt;
        try { amt = Double.parseDouble(expenseAmountInput.getText().trim());
              if (amt <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showWarn("Enter a positive numeric amount.");
            return;
        }

        String freq = null;
        if (expenseRecurringCheckBox != null && expenseRecurringCheckBox.isSelected()) {
            freq = "fixed";
            } else{
            freq = "variable";
        }

        String cat = Optional.ofNullable(expenseCatCombo.getEditor().getText())
                             .orElse("").trim();
        if (cat.isEmpty()) { showWarn("Enter or select an expense category."); return; }

        if(freq.equals("variable")){
            addToCurrentMonthExpense(cat, freq, amt); // Add to current month
            updateTotals();
        } else{
            // For recurring expenses, add to base expenses  
            model.addExpense(cat, freq, amt);
            model.refreshCurrentMonthFromBase();
            updateTotals(); // Refresh current month from base
        }
        BudgetInfoDAO.updateBudgetInfoForUser(UserDAO.getUserIdByName(model.usernameProperty()), model);
        if (!expenseCatCombo.getItems().contains(cat))
            expenseCatCombo.getItems().add(cat);

        expenseAmountInput.clear();
        expenseFreqCombo.setValue(null);
        expenseCatCombo.getSelectionModel().clearSelection();
        expenseCatCombo.getEditor().clear();
        expenseRecurringCheckBox.setSelected(false);
        toggleExpenseFrequency(); // Hide frequency section
    }

    private void addToCurrentMonthExpense(String category, String frequency, double amount) {
        // For one-time expenses, find the Monthly category in current month and add to it
        Optional<MoneyLine> expense = model.getCurrentMonth().stream()
            .filter(moneyLine -> category.equals(moneyLine.getType()) && 
                                "variable".equals(moneyLine.getFreq())) // Always look for Monthly
            .findFirst();
        
        if (expense.isPresent()) {
            expense.get().setAmount(expense.get().getAmount() + amount);
            updateTotals();
        } else {
            showWarn("Category '" + category + "' not found in current month budget.");
        }
    }

    /* ── Totals & styles ──────────────────────────────────────────── */
    @FXML
    private void updateTotals() {
        double inc = model.incomes() .stream().mapToDouble(MoneyLine::getAmount).sum();
        double exp = model.expenses().stream().mapToDouble(MoneyLine::getAmount).sum();
        double net = inc - exp;

        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
        totalIncomeLabel .setText(nf.format(inc));
        totalExpenseLabel.setText(nf.format(exp));
        netLabel.setText((net >= 0 ? "+" : "-") + nf.format(Math.abs(net)));

        netLabel.getStyleClass().removeAll("net-positive","net-negative");
        netLabel.getStyleClass().add(net >= 0 ? "net-positive" : "net-negative");
        
        /* Refresh ListView displays to show updated amounts */
        if (incomeList != null) {
            incomeList.refresh();
        }
        if (expenseList != null) {
            expenseList.refresh();
        }
    }

    /* ── Helpers & unchanged reward / nav code ────────────────────── */
    private void showWarn(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    
     @FXML private void dailyRewards() {
        CooldownManager cooldownManager = CooldownManager.getInstance();
        
        // Check if already claimed (prevents multiple claims)
        if (cooldownManager.hasClaimedReward("daily")) {
            showWarn("Daily reward already claimed! Next available in: " + 
                     cooldownManager.getFormattedTimeLeft("daily"));
            return;
        }
        
        // Claim the reward
        model.setGems(model.getGems().get() + 50);
        model.addPoints(50);
        
        // Start cooldown (24 hours = 86400 seconds, using 100 for testing)
        cooldownManager.startCooldown("daily", 100); // Change to 86400 for production
        
        // Update button state
        updateRewardButtons();
        
        // Save to database
        BudgetInfoDAO.updateBudgetInfoForUser(UserDAO.getUserIdByName(model.usernameProperty()), model);
    }
    
    @FXML private void weeklyRewards() {
        CooldownManager cooldownManager = CooldownManager.getInstance();
        
        if (cooldownManager.hasClaimedReward("weekly")) {
            showWarn("Weekly reward already claimed! Next available in: " + 
                     cooldownManager.getFormattedTimeLeft("weekly"));
            return;
        }
        
        model.setGems(model.getGems().get() + 100);
        model.addPoints(150);
        
        // Start cooldown (7 days = 604800 seconds, using 200 for testing)
        cooldownManager.startCooldown("weekly", 200); // Change to 604800 for production
        
        updateRewardButtons();
        BudgetInfoDAO.updateBudgetInfoForUser(UserDAO.getUserIdByName(model.usernameProperty()), model);
    }
    
    @FXML private void monthlyRewards() {
        CooldownManager cooldownManager = CooldownManager.getInstance();
        
        if (cooldownManager.hasClaimedReward("monthly")) {
            showWarn("Monthly reward already claimed! Next available in: " + 
                     cooldownManager.getFormattedTimeLeft("monthly"));
            return;
        }
        
        model.setGems(model.getGems().get() + 250);
        model.addPoints(1000);
        
        // Start cooldown (30 days = 2592000 seconds, using 300 for testing)
        cooldownManager.startCooldown("monthly", 300); // Change to 2592000 for production
        
        updateRewardButtons();
        BudgetInfoDAO.updateBudgetInfoForUser(UserDAO.getUserIdByName(model.usernameProperty()), model);
    }
    
    private void updateRewardButtons() {
        CooldownManager cooldownManager = CooldownManager.getInstance();
        
        // Update button states and text
        if (dailyRewardButton != null) {
            boolean claimed = cooldownManager.hasClaimedReward("daily");
            dailyRewardButton.setDisable(claimed);
            if (claimed && cooldownManager.isOnCooldown("daily")) {
                dailyRewardButton.setText("Daily (" + cooldownManager.getFormattedTimeLeft("daily") + ")");
            } else {
                dailyRewardButton.setText("Claim Daily");
            }
        }
        
        if (weeklyRewardButton != null) {
            boolean claimed = cooldownManager.hasClaimedReward("weekly");
            weeklyRewardButton.setDisable(claimed);
            if (claimed && cooldownManager.isOnCooldown("weekly")) {
                weeklyRewardButton.setText("Weekly (" + cooldownManager.getFormattedTimeLeft("weekly") + ")");
            } else {
                weeklyRewardButton.setText("Claim Weekly");
            }
        }
        
        if (monthlyRewardButton != null) {
            boolean claimed = cooldownManager.hasClaimedReward("monthly");
            monthlyRewardButton.setDisable(claimed);
            if (claimed && cooldownManager.isOnCooldown("monthly")) {
                monthlyRewardButton.setText("Monthly (" + cooldownManager.getFormattedTimeLeft("monthly") + ")");
            } else {
                monthlyRewardButton.setText("Claim Monthly");
            }
        }
    }
    
    /* ── Demo & nav buttons (unchanged) ────────────────────────────── */
    @FXML private void switchToSecondary() throws IOException { App.setRoot("secondary"); }
    @FXML private void switchToPrimary()   throws IOException { 
        model.refreshCurrentMonthFromBase(); // Keep current month in sync

        // Manually fire UI updates when model state changes but FX doesn't detect it
        if (model.getCurrentMonth() != null) {
            // simulate a change to trigger listener in PrimaryController
            var copy = FXCollections.observableArrayList(model.getCurrentMonth());
            model.getCurrentMonth().setAll(copy);
        }
        
        App.setRoot("primary");  
    }
    @FXML private void switchToLeaderboard() throws IOException { App.setRoot("leaderboard"); }
    @FXML private void switchToStore()    throws IOException { App.setRoot("store");     }
    @FXML private void switchToProfile()  throws IOException { App.setRoot("profile");   }
    @FXML private void logoff() { System.exit(0); }
}