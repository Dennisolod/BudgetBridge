package groupid;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.kordamp.ikonli.javafx.FontIcon;

import groupid.model.BadgeLine;
import groupid.model.BudgetModel;
import groupid.model.MissionLine;
import groupid.model.MoneyLine;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

// Homescreen / My Dashboard
public class PrimaryController implements ModelAware, Initializable {

    @FXML private Label userLabel;
    @FXML private Label netLabel;
    @FXML private ListView<MoneyLine> incomeList;
    @FXML private ListView<MoneyLine> expenseList;
    @FXML private Label pointsLabel;
    @FXML private ListView<MissionLine> missionList;
    @FXML private ListView<BadgeLine> badgeList;

    private BudgetModel model;

    @Override
    public void setModel(BudgetModel m) {
        this.model = m;

        userLabel.textProperty().bind(m.usernameProperty());
        missionList.setItems(m.missions());
        badgeList.setItems(m.getOwnedBadges());
        //pointsLabel.textProperty().bind(m.pointsProperty().asString());

        //netLabel.textProperty().bind(m.netBalanceProperty().asString("$%.2f"));

        //incomeList.setItems(m.incomes());
        //expenseList.setItems(m.expenses());
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        badgeList.setCellFactory(lv -> new ListCell<>() {
            private final FontIcon icon = new FontIcon();
            private final Label    name = new Label();
            private final HBox     row  = new HBox(20, icon, name);
            @Override protected void updateItem(BadgeLine b, boolean empty) {
                super.updateItem(b, empty);
                if (empty || b == null) { setGraphic(null); }
                else {
                    icon.setIconLiteral(b.getIconLiteral());
                    icon.setIconColor(b.getColor());
                    icon.setIconSize(48);
                    name.setText(b.getName());
                    name.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
                    setGraphic(row);
                }
            }
        });
       
    }

    public void addBadge(BadgeLine badge) {
        if (!badgeList.getItems().contains(badge)) {
            badgeList.getItems().add(badge);
        }
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
