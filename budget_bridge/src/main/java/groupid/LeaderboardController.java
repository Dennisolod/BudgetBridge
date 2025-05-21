package groupid;

import java.io.IOException;

import groupid.model.BudgetModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

// Leaderboard screen
public class LeaderboardController implements ModelAware{

    @FXML private javafx.scene.layout.VBox leaderboardVBox;
    @FXML private Label userPoints;
    @FXML private Label userBadges;

    /* assuming friends leaderboard rows are hard coded in the FXML */

    @Override 
    public void setModel(BudgetModel m) {
        //userPoints.textProperty().bind(m.pointsProperty().asString("%d pts"));
        //userBadges.textProperty().bind(m.badges().size().asString("%d badges"));

        leaderboardVBox.getChildren().clear();

        int rank = 1;
        for (var entry : m.getLeaderboard()) {
            HBox row = new HBox(10); // spacing between elements

            Label nameLabel = new Label(String.format("%3d %-15s", rank++, entry.getKey()));
            Label pointsLabel = new Label(String.format("%5d pts", entry.getValue()));
            pointsLabel.getStyleClass().add("leaderboard-points");

            row.getChildren().addAll(nameLabel, pointsLabel);
            leaderboardVBox.getChildren().add(row);
        }
    }

    @FXML private void switchToSecondary() throws IOException { App.setRoot("secondary"); }
    @FXML private void switchToPrimary() throws IOException { App.setRoot("primary"); }
    @FXML private void switchToLeaderboard() throws IOException { App.setRoot("leaderboard"); }
    @FXML private void switchToStore() throws IOException { App.setRoot("store"); }
    @FXML private void switchToProfile() throws IOException { App.setRoot("profile"); }
    @FXML private void switchToSettings() throws IOException { App.setRoot("settings"); }
    @FXML private void switchToTutorial() throws IOException { App.setRoot("tutorial"); }
    @FXML private void logoff() throws IOException { System.exit(0); }

}
