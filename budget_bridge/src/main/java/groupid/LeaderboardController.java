package groupid;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import groupid.model.BadgeLine;
import groupid.model.BudgetModel;
import groupid.model.League;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import org.kordamp.ikonli.javafx.FontIcon;

// Leaderboard screen
public class LeaderboardController implements ModelAware{

    @FXML private javafx.scene.layout.VBox leaderboardVBox;
    @FXML private Label userPoints;
    @FXML private Label userBadges;
    @FXML private Label userLabel;
    @FXML private Label userRankPos;
    @FXML private Label leagueLabel;
    @FXML private ProgressBar leagueProgressBar;
    @FXML private Label progressLabel;
    @FXML private FontIcon leagueIcon;
    @FXML private Label leagueUserCount;
    @FXML private Label topScorerLabel;
    @FXML private VBox leagueStatsBox;
    @FXML private Label rewardsPreviewLabel;

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

        List<Map.Entry<String, Integer>> leaguePlayers = m.getLeaderboard().stream()
            .filter(entry -> getLeagueForPoints(entry.getValue()) == userLeague)
            .toList();

        leagueUserCount.setText("Players in this league: " + leaguePlayers.size());

        Map.Entry<String, Integer> topPlayer = leaguePlayers.get(0);
        topScorerLabel.setText("Top scorer: " + topPlayer.getKey() + " (" + topPlayer.getValue() + " pts)");

        leagueLabel.setText("League: " + userLeague.name());
        leagueLabel.getStyleClass().removeIf(s -> s.startsWith("league-") && !s.equals("league-label"));
        leagueLabel.getStyleClass().add("league-" + userLeague.name().toLowerCase());

        switch (userLeague) {
            case BRONZE -> {
                leagueIcon.setIconLiteral("fas-medal");
                leagueIcon.setIconColor(javafx.scene.paint.Color.web("#cd7f32"));
            }
            case COPPER -> {
                leagueIcon.setIconLiteral("fas-award");
                leagueIcon.setIconColor(javafx.scene.paint.Color.web("#b87333"));
            }
            case SILVER -> {
                leagueIcon.setIconLiteral("fas-trophy");
                leagueIcon.setIconColor(javafx.scene.paint.Color.web("#c0c0c0"));
            }
            case GOLD -> {
                leagueIcon.setIconLiteral("fas-star");
                leagueIcon.setIconColor(javafx.scene.paint.Color.web("#ffd700"));
            }
            case PLATINUM -> {
                leagueIcon.setIconLiteral("fas-shield-alt");
                leagueIcon.setIconColor(javafx.scene.paint.Color.web("#e5e4e2"));
            }
            case DIAMOND -> {
                leagueIcon.setIconLiteral("fas-gem");
                leagueIcon.setIconColor(javafx.scene.paint.Color.web("#b9f2ff"));
            }
        }

        String currentUser = m.usernameProperty().get();
        boolean userFound = false;
        
        int rank = 1;

        // leaderboard row loop
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

            if (rank == 1) {
                nameLabel.getStyleClass().add("top-rank-highlight");
            } else {
                nameLabel.getStyleClass().add("leaderboard-name");
            }

           
            // Add badge icons
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

            int points = m.pointsProperty().get();
            League currentLeague = m.getCurrentLeague();

            int min = 0, max = 1;
            switch (currentLeague) {
                case BRONZE -> { min = 0; max = 5000; }
                case COPPER -> { min = 5000; max = 10000; }
                case SILVER -> { min = 10000; max = 20000; }
                case GOLD ->   { min = 20000; max = 40000; }
                case PLATINUM -> { min = 40000; max = 60000; }
                case DIAMOND -> { min = 60000; max = 80000; } // diamond can be capped for UI purposes
            }

            
            double progress = (double)(points - min) / (max - min);
            progress = Math.max(0, Math.min(progress, 1)); // clamp between 0 and 1

            leagueProgressBar.setProgress(progress);
            progressLabel.setText(String.format("Progress to %s: %.0f%%",
                getNextLeague(currentLeague).name(), progress * 100));


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

            // next league rewards
            League nextLeague = getNextLeague(currentLeague);
            String rewardText = switch (nextLeague) {
                case COPPER   -> "🎖 Copper Badge";
                case SILVER   -> "🥈 Silver Badge + 500 Gems";
                case GOLD     -> "🥇 Gold Trophy + 1,000 Gems";
                case PLATINUM -> "💠 Platinum Shield + 2,000 Gems";
                case DIAMOND  -> "💎 Diamond Gem + 5,000 Gems";
                default       -> "🏆 Max League Reached!";
            };

            rewardsPreviewLabel.setText("Next League Reward: " + rewardText);
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

    private League getNextLeague(League current) {
        return switch (current) {
            case BRONZE -> League.COPPER;
            case COPPER -> League.SILVER;
            case SILVER -> League.GOLD;
            case GOLD -> League.PLATINUM;
            case PLATINUM -> League.DIAMOND;
            case DIAMOND -> League.DIAMOND; // maxed
        };
    }
     
    @FXML private void switchToSecondary() throws IOException { App.setRoot("secondary"); }
    @FXML private void switchToPrimary() throws IOException { App.setRoot("primary"); }
    @FXML private void switchToLeaderboard() throws IOException { App.setRoot("leaderboard"); }
    @FXML private void switchToStore() throws IOException { App.setRoot("store"); }
    @FXML private void switchToProfile() throws IOException { App.setRoot("profile"); }
    @FXML private void logoff() throws IOException { System.exit(0); }

    
}
