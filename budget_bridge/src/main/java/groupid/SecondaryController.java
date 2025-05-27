package groupid;

import java.io.IOException;

import groupid.model.BudgetModel;
import groupid.model.MoneyLine;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;


// Budget screen
public class SecondaryController implements ModelAware {


    // injected via fx:id
    @FXML private ListView<MoneyLine> incomeList;
    @FXML private ListView<MoneyLine> expenseList;
    @FXML private Label totalIncomeLabel;
    @FXML private Label totalExpenseLabel;
    @FXML private Label netLabel;
    @FXML private TextField incomeTypeInput;
    @FXML private TextField incomeAmountInput;
    @FXML private TextField expenseTypeInput;
    @FXML private TextField expenseAmountInput;
    @FXML private Button dailyRewardButton;
    @FXML private Button weeklyRewardButton;
    @FXML private Button monthlyRewardButton;
    @FXML private Label missionDaily;
    @FXML private Label missionWeekly;
    @FXML private Label missionMonthly;

    private BudgetModel model;

    @Override public void setModel(BudgetModel m) {
        model = m;

        /* live lists */
        //m.addIncome(null, 0);
        incomeList.setItems(m.incomes());
        expenseList.setItems(m.expenses());
        m.incomes().addListener((ListChangeListener<MoneyLine>) c -> updateTotals());
        m.expenses().addListener((ListChangeListener<MoneyLine>) c -> updateTotals());
        updateTotals(); 
        missionDaily.setText(model.missions().get(0).toString());
        missionWeekly.setText(model.missions().get(1).toString());
        missionMonthly.setText(model.missions().get(2).toString());
    }
    @FXML
    private void updateTotals() {
        double income  = model.incomes().stream().mapToDouble(MoneyLine::getAmount).sum();
        double expense = model.expenses().stream().mapToDouble(MoneyLine::getAmount).sum();
        double net     = income - expense;
        System.out.printf("income=%.2f  expense=%.2f%n", income, expense);
        totalIncomeLabel.setText(String.format("$%.2f", income));
        totalExpenseLabel.setText(String.format("$%.2f", expense));
        netLabel.setText(String.format("%s$%.2f", net >= 0 ? "+" : "-", Math.abs(net)));

        netLabel.getStyleClass().removeAll("net-positive","net-negative");
        netLabel.getStyleClass().add(net >= 0 ? "net-positive" : "net-negative");

        
    }


    @FXML
    private void addInputIncome() {
        try {
            String type = incomeTypeInput.getText();
            double amount = Double.parseDouble(incomeAmountInput.getText());
            model.addIncome(type, amount);
            incomeTypeInput.clear();
            incomeAmountInput.clear();
        } catch (NumberFormatException e) {
            incomeAmountInput.setText("Invalid number");
        }
    }

    @FXML
    private void addInputExpense() {
        try {
            String type = expenseTypeInput.getText();
            double amount = Double.parseDouble(expenseAmountInput.getText());
            model.addExpense(type, amount);
            expenseTypeInput.clear();
            expenseAmountInput.clear();
        } catch (NumberFormatException e) {
            expenseAmountInput.setText("Invalid number");
        }
    }

    @FXML
    private void dailyRewards() {
        dailyRewardButton.setDisable(true);
        model.setGems(model.getGems().get() + 50);
        model.addPoints(1000);
    }

    @FXML
    private void weeklyRewards() {
        weeklyRewardButton.setDisable(true);
        model.setGems(model.getGems().get() + 100);
        model.addPoints(3000);
    }

    @FXML
    private void monthlyRewards() {
        monthlyRewardButton.setDisable(true);
        model.setGems(model.getGems().get() + 250);
        model.addPoints(7000);
    }


    /* demo buttons wired in FXML */
    @FXML private void addIncome()  { model.addIncome ("Freelance",120.0); }
    @FXML private void addExpense() { model.addExpense("Snacks",15.0); }

    @FXML private void switchToSecondary() throws IOException { App.setRoot("secondary"); }
    @FXML private void switchToPrimary() throws IOException { App.setRoot("primary"); }
    @FXML private void switchToLeaderboard() throws IOException { App.setRoot("leaderboard"); }
    @FXML private void switchToStore() throws IOException {App.setRoot("store"); }
    @FXML private void switchToProfile() throws IOException { App.setRoot("profile"); }
    @FXML private void switchToSettings() throws IOException { App.setRoot("settings"); }
    @FXML private void switchToTutorial() throws IOException { App.setRoot("tutorial"); }
    @FXML private void logoff() throws IOException { System.exit(0); }

    
}