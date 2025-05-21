package groupid;

import java.io.IOException;

import groupid.model.BudgetModel;
import groupid.model.MoneyLine;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;


// Budget screen
public class SecondaryController implements ModelAware {


    // injected via fx:id
    @FXML private ListView<MoneyLine> incomeList;
    @FXML private ListView<MoneyLine> expenseList;
    @FXML private Label totalIncomeLabel;
    @FXML private Label totalExpenseLabel;
    @FXML private Label netLabel;

    private BudgetModel model;

    @Override public void setModel(BudgetModel m) {
        model = m;

        /* live lists */
        //incomeList .setItems(m.incomes());
        //expenseList.setItems(m.expenses());
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