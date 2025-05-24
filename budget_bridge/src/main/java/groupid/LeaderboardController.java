package groupid;

import java.io.IOException;

import groupid.model.BudgetModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

// Leaderboard screen
public class LeaderboardController implements ModelAware{

    @FXML private javafx.scene.layout.VBox leaderboardVBox;
    @FXML private Label userPoints;
    @FXML private Label userBadges;
    @FXML private Label userLabel;
    @FXML private Label userRankPos;

    /* assuming friends leaderboard rows are hard coded in the FXML */

    @Override 
    public void setModel(BudgetModel m) {
        // userBadges.textProperty().bind(m.badges().size().asString("%d badges"));
        userPoints.textProperty().bind(m.pointsProperty().asString("%d pts"));
        userLabel.textProperty().bind(m.usernameProperty());
        userRankPos.textProperty().bind(m.getLeaderboardPos());

        // adds the current player to the leaderboard
        m.addUserToLeaderboard(m.usernameProperty().get(), m.pointsProperty().get());
        leaderboardVBox.getChildren().clear();


        String currentUser = m.usernameProperty().get();
        boolean userFound = false;
        
        int rank = 1;
        for (var entry : m.getLeaderboard()) {

            // sets the leaderboard position of the user
            if (entry.getKey().equals(currentUser) && !userFound) {
                String r = String.valueOf(rank);
                m.setRankPos(r);
                userFound = true;
            }

            GridPane row = new GridPane(); // spacing between elements
            row.setHgap(10);

            Label rankLabel = new Label(String.format("%d.", rank++));
            Label nameLabel = new Label(String.format(entry.getKey()));
            Label pointsLabel = new Label(String.format("%d pts", entry.getValue()));

            pointsLabel.getStyleClass().add("leaderboard-points");
            nameLabel.getStyleClass().add("leaderboard-name");
            rankLabel.getStyleClass().add("leaderboard-rank");


            row.add(rankLabel, 0, 0);
            row.add(nameLabel, 1, 0);
            row.add(pointsLabel, 2, 0);

            // makes the name label grow to take up extra space
            GridPane.setHgrow(nameLabel, Priority.ALWAYS);
            nameLabel.setMaxWidth(Double.MAX_VALUE);
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
