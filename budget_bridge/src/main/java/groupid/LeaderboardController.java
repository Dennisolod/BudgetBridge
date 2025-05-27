package groupid;

import java.io.IOException;
import java.util.List;

import groupid.model.BadgeLine;
import groupid.model.BudgetModel;
import groupid.model.BudgetModel.League;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.layout.Region;
// Leaderboard screen
public class LeaderboardController implements ModelAware{

    @FXML private javafx.scene.layout.VBox leaderboardVBox;
    @FXML private Label userPoints;
    @FXML private Label userBadges;
    @FXML private Label userLabel;
    @FXML private Label userRankPos;
    @FXML private Label leagueLabel;

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
        
        League userLeague = m.getCurrentLeague();
        leagueLabel.getStyleClass().add("league-" + userLeague.name().toLowerCase());

        leagueLabel.setText("League: " + userLeague.name());


        String currentUser = m.usernameProperty().get();
        boolean userFound = false;
        
        int rank = 1;
        for (var entry : m.getLeaderboard()) {
            int playerPoints = entry.getValue();
            League playerLeague = getLeagueForPoints(playerPoints);
            if (playerLeague != userLeague) { continue; }

            // sets the leaderboard position of the user
            if (entry.getKey().equals(currentUser) && !userFound) {
                String r = String.valueOf(rank);
                m.setRankPos(r);
                userFound = true;
            }

            HBox row = new HBox();
            row.setSpacing(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("leaderboard-card");

            Label rankLabel = new Label(rank + ".");
            rankLabel.getStyleClass().add("leaderboard-rank");

            Label nameLabel = new Label(entry.getKey());
            nameLabel.getStyleClass().add("leaderboard-name");

            // 💎 Add badge icons
            HBox badgeBox = new HBox(5);
            badgeBox.setAlignment(Pos.CENTER_LEFT);


            List<BadgeLine> topBadges = entry.getKey().equals(currentUser)
                ? m.getTop3BadgeLines(currentUser)
                : List.of(); // empty for everyone else

            for (BadgeLine badge : topBadges) {
                FontIcon icon = new FontIcon(badge.getIconLiteral());
                icon.setIconSize(20);
                icon.setIconColor(badge.getColor());
                icon.getStyleClass().add("badge-icon");
                badgeBox.getChildren().add(icon);
            }

            // Spacer for alignment
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label pointsLabel = new Label(entry.getValue() + " pts");
            pointsLabel.getStyleClass().add("leaderboard-points");

            // Add everything to the row
            row.getChildren().addAll(rankLabel, nameLabel, badgeBox, spacer, pointsLabel);

            // Highlight current user
            if (entry.getKey().equals(currentUser)) {
                row.getStyleClass().add("leaderboard-highlight");
            }

            leaderboardVBox.getChildren().add(row);
            rank++;

        }

    }

    private League getLeagueForPoints(int points) {
        if (points >= 60000) return League.DIAMOND;
        if (points >= 40000) return League.PLATINUM;
        if (points >= 20000) return League.GOLD;
        if (points >= 10000) return League.SILVER;
        if (points >= 5000)  return League.COPPER;
        return League.BRONZE;
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
