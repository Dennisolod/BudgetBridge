package groupid;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

import groupid.model.BudgetModel;
import groupid.model.CooldownManager;
import groupid.model.MoneyLine;
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
        model.addIncome(freq, cat, amt);

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
            freq = expenseFreqCombo.getValue();
            if (freq == null) { 
                showWarn("Select expense frequency for recurring expense."); 
                return; 
            } 
            } else{
                freq = "One-time";
        }

        String cat = Optional.ofNullable(expenseCatCombo.getEditor().getText())
                             .orElse("").trim();
        if (cat.isEmpty()) { showWarn("Enter or select an expense category."); return; }

        if(freq.equals("One-time")){
            addToCurrentMonthExpense(cat, freq, amt); // Add to current month
            updateTotals();
        } else{
            // For recurring expenses, add to base expenses  
            model.addExpense(freq, cat, amt);
            updateTotals(); // Refresh current month from base
        }

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
                                    "Monthly".equals(moneyLine.getFreq())) // Always look for Monthly
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
        CooldownManager dailyCooldown = CooldownManager.getInstance();
        if (!dailyCooldown.isOnCooldown()) {
            dailyRewardButton  .setDisable(true);
            model.setGems(model.getGems().get()+50);
            model.addPoints(1000);
            dailyCooldown.startCooldown(100);
            System.out.println("The cooldown is being set");
        } else {
            dailyRewardButton .setDisable(false);
        }
        dailyRewardButton  .setDisable(true);
        model.setGems(model.getGems().get()+50);
        model.addPoints(1000);
    }
    @FXML private void weeklyRewards()  { weeklyRewardButton .setDisable(true); model.setGems(model.getGems().get()+100); model.addPoints(3000); }
    @FXML private void monthlyRewards() { monthlyRewardButton.setDisable(true); model.setGems(model.getGems().get()+250); model.addPoints(7000); }
    
    /* ── Demo & nav buttons (unchanged) ────────────────────────────── */
    @FXML private void switchToSecondary() throws IOException { App.setRoot("secondary"); }
    @FXML private void switchToPrimary()   throws IOException { App.setRoot("primary");   }
    @FXML private void switchToLeaderboard() throws IOException { App.setRoot("leaderboard"); }
    @FXML private void switchToStore()    throws IOException { App.setRoot("store");     }
    @FXML private void switchToProfile()  throws IOException { App.setRoot("profile");   }
    @FXML private void logoff() { System.exit(0); }
}