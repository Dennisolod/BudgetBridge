package groupid;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

import groupid.model.BudgetModel;
import groupid.model.MoneyLine;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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

    /* ── Expense controls ─────────────────────────────────────────── */
    @FXML private ComboBox<String>  expenseFreqCombo;
    @FXML private ComboBox<String>  expenseCatCombo;
    @FXML private TextField         expenseAmountInput;
    @FXML private ListView<MoneyLine> expenseList;
    @FXML private VBox              expenseInputSection; // The collapsible section
    @FXML private Button            addExpenseToggleButton; // The toggle button

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
    }

    /* ── Model injection ──────────────────────────────────────────── */
    @Override
    public void setModel(BudgetModel m) {
        model = m;
        incomeList .setItems(m.incomes());
        expenseList.setItems(m.expenses());
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

    /* ── Toggle expense input section ────────────────────────────── */
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
        String freq = incomeFreqCombo.getValue();
        if (freq == null) { showWarn("Select income frequency."); return; }

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

        String freq = expenseFreqCombo.getValue();
        if (freq == null) { showWarn("Select expense frequency."); return; }

        String cat = Optional.ofNullable(expenseCatCombo.getEditor().getText())
                             .orElse("").trim();
        if (cat.isEmpty()) { showWarn("Enter or select an expense category."); return; }

        model.addExpense(freq, cat, amt);

        if (!expenseCatCombo.getItems().contains(cat))
            expenseCatCombo.getItems().add(cat);

        expenseAmountInput.clear();
        expenseFreqCombo.setValue(null);
        expenseCatCombo.getSelectionModel().clearSelection();
        expenseCatCombo.getEditor().clear();
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
    }

    /* ── Helpers & unchanged reward / nav code ────────────────────── */
    private void showWarn(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    /* ── Reward buttons (unchanged) ────────────────────────────────── */
    @FXML private void dailyRewards()   { dailyRewardButton  .setDisable(true); model.setGems(model.getGems().get()+50);  model.addPoints(1000); }
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