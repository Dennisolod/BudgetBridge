package groupid;

import groupid.model.BudgetModel;
import groupid.model.BadgeLine;
import groupid.model.MoneyLine;
import groupid.model.League;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;

import javafx.collections.FXCollections;

import java.io.IOException;
import java.util.List;

public class ProfileController implements ModelAware{

    @FXML private Label nameLabel;
    @FXML private Label balanceLabel;
    @FXML private Label pointsLabel;
    @FXML private Label gemsLabel;
    @FXML private Label leagueLabel;
    @FXML private FontIcon leagueIcon;
    @FXML private HBox badgeBox;
    @FXML private ListView<String> incomeList;
    @FXML private ListView<String> expenseList;

    @Override public void setModel(BudgetModel m) {
        //nameLabel.textProperty().bind(m.usernameProperty());
        //balanceLabel.textProperty().bind(m.netBalanceProperty().asString("Net balance: $%.2f"));
        
        pointsLabel.setText(m.pointsProperty().get() + " pts");
        gemsLabel.setText(m.getGems().get() + " gems");

        League league = m.getCurrentLeague();
        leagueLabel.setText(league.name());

        switch (league) {
            case BRONZE -> { leagueIcon.setIconLiteral("fas-medal"); leagueIcon.setIconColor(javafx.scene.paint.Color.web("#cd7f32")); }
            case COPPER -> { leagueIcon.setIconLiteral("fas-award"); leagueIcon.setIconColor(javafx.scene.paint.Color.web("#b87333")); }
            case SILVER -> { leagueIcon.setIconLiteral("fas-trophy"); leagueIcon.setIconColor(javafx.scene.paint.Color.web("#c0c0c0")); }
            case GOLD ->   { leagueIcon.setIconLiteral("fas-star"); leagueIcon.setIconColor(javafx.scene.paint.Color.web("#ffd700")); }
            case PLATINUM -> { leagueIcon.setIconLiteral("fas-shield-alt"); leagueIcon.setIconColor(javafx.scene.paint.Color.web("#e5e4e2")); }
            case DIAMOND -> { leagueIcon.setIconLiteral("fas-gem"); leagueIcon.setIconColor(javafx.scene.paint.Color.web("#b9f2ff")); }
        }

        // Show top 3 badges
        badgeBox.getChildren().clear();
        List<BadgeLine> topBadges = m.getTop3BadgeLines(m.usernameProperty().get());
        for (BadgeLine badge : topBadges) {
            FontIcon icon = new FontIcon(badge.getIconLiteral());
            icon.setIconSize(20);
            icon.setIconColor(badge.getColor());
            icon.getStyleClass().add("badge-icon");
            badgeBox.getChildren().add(icon);
        }

        // Populate income and expense sources
        incomeList.setItems(FXCollections.observableArrayList(
            m.incomes().stream().map(MoneyLine::toString).toList()
        ));

        expenseList.setItems(FXCollections.observableArrayList(
            m.expenses().stream().map(MoneyLine::toString).toList()
        ));


    } // end set model
    
    @FXML private void switchToSecondary() throws IOException { App.setRoot("secondary"); }
    @FXML private void switchToPrimary() throws IOException { App.setRoot("primary"); }
    @FXML private void switchToLeaderboard() throws IOException { App.setRoot("leaderboard"); }
    @FXML private void switchToStore() throws IOException {App.setRoot("store"); }
    @FXML private void switchToProfile() throws IOException { App.setRoot("profile"); }
    @FXML private void logoff() throws IOException { System.exit(0); }

    
}
