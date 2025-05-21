package groupid;

import java.io.IOException;

import groupid.model.BudgetModel;
import groupid.model.MoneyLine;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

// Homescreen / My Dashboard
public class PrimaryController implements ModelAware {

    @FXML private Label userLabel;
    @FXML private Label netLabel;
    @FXML private ListView<MoneyLine> incomeList;
    @FXML private ListView<MoneyLine> expenseList;

    private BudgetModel model;

    @Override
    public void setModel(BudgetModel m) {
        this.model = m;

        userLabel.textProperty().bind(m.usernameProperty());
        //netLabel.textProperty().bind(m.netBalanceProperty().asString("$%.2f"));
        System.out.println("setModel called with username: " + m.usernameProperty().get());

        //incomeList.setItems(m.incomes());
        //expenseList.setItems(m.expenses());
    }

    @FXML private void onAddIncome()  { model.addIncome("Side Hustle", 200); }
    @FXML private void onAddExpense() { model.addExpense("Coffee", 5);   }

    @FXML private void switchToSecondary() throws IOException { App.setRoot("secondary"); }
    @FXML private void switchToPrimary() throws IOException { App.setRoot("primary"); }
    @FXML private void switchToLeaderboard() throws IOException { App.setRoot("leaderboard"); }
    @FXML private void switchToStore() throws IOException {App.setRoot("store"); }
    @FXML private void switchToProfile() throws IOException { App.setRoot("profile"); }
    @FXML private void switchToSettings() throws IOException { App.setRoot("settings"); }
    @FXML private void switchToTutorial() throws IOException { App.setRoot("tutorial"); }
    @FXML private void logoff() throws IOException { System.exit(0); }


}
