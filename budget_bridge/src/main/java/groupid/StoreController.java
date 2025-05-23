package groupid;

import java.io.IOException;

import groupid.model.BudgetModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

// Exhchange Store screen
public class StoreController implements ModelAware{

    @FXML private Label pointsLabel;
    @FXML private ListView<String> badgeList;
    private BudgetModel model;

    @Override 
    public void setModel(BudgetModel m) {
        this.model = m;
        //pointsLabel.textProperty().bind(m.pointsProperty().asString("%d pts"));
        //badgeList.setItems(m.badges());
    }

    @FXML 
    private void buyBadge() {
        if (model.pointsProperty().get() >= 30) {
            model.pointsProperty().set(model.pointsProperty().get() - 30);
            //model.badges().add("Saver Level 1");
        }
    }

    @FXML private void switchToSecondary() throws IOException { App.setRoot("secondary"); }
    @FXML private void switchToPrimary() throws IOException { App.setRoot("primary"); }
    @FXML private void switchToLeaderboard() throws IOException { App.setRoot("leaderboard"); }
    @FXML private void switchToStore() throws IOException {App.setRoot("store"); }
    @FXML private void switchToProfile() throws IOException { App.setRoot("profile"); }
    @FXML private void switchToSettings() throws IOException { App.setRoot("settings"); }
    @FXML private void switchToTutorial() throws IOException { App.setRoot("tutorial"); }
    @FXML private void logoff() throws IOException { System.exit(0); }

    
}