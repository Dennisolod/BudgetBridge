package groupid;

import groupid.model.BadgeLine;
import groupid.model.BudgetModel;
import groupid.model.MoneyLine;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.io.IOException;

import org.kordamp.ikonli.javafx.FontIcon;

public class ProfileController implements ModelAware {

    @FXML private Label pointsLabel;
    @FXML private Label gemsLabel;
    @FXML private Label leagueLabel;
    @FXML private FontIcon leagueIcon;
    @FXML private HBox badgeBox;
    @FXML private ListView<MoneyLine> incomeList;
    @FXML private ListView<MoneyLine> expenseList;
    @FXML private FlowPane badgeGallery;
    @FXML private HBox recentBadgeLabel;
    @FXML private Label profileTitle;

    @Override
    public void setModel(BudgetModel m) {
        pointsLabel.setText(m.pointsProperty().get() + " pts");
        gemsLabel.setText(m.getGems().get() + " Gems");
        leagueLabel.setText(m.getCurrentLeague().name());
        String username = m.usernameProperty().get();
        profileTitle.setText(username + (username.endsWith("s") ? "'" : "'s") + " Profile");

        incomeList.setItems(m.incomes());
        expenseList.setItems(m.expenses());

        badgeBox.getChildren().clear();
        for (BadgeLine badge : m.getTop3BadgeLines(m.usernameProperty().get())) {
            FontIcon icon = new FontIcon(badge.getIconLiteral());
            icon.setIconSize(20);
            icon.setIconColor(badge.getColor());
            icon.getStyleClass().add("badge-icon");
            badgeBox.getChildren().add(icon);
        }

        badgeGallery.getChildren().clear();
        for (BadgeLine badge : m.getOwnedBadges()) {
            FontIcon icon = new FontIcon(badge.getIconLiteral());
            icon.setIconSize(28);
            icon.setIconColor(badge.getColor());
            icon.getStyleClass().add("badge-icon");

            // Tooltip
            int price = m.getCostForBadge(badge);
            Tooltip tooltip = new Tooltip(badge.getName() + " - " + price + " coins");
            tooltip.setShowDelay(Duration.millis(100)); // ⏱ Fast show

            Tooltip.install(icon, tooltip);
            badgeGallery.getChildren().add(icon);
        }

        var recent = m.getOwnedBadges();
        if (!recent.isEmpty()) {
            BadgeLine last = recent.get(recent.size() - 1);
            HBox recentBadgeContent = new HBox(8); // spacing between icon and label
            recentBadgeContent.setAlignment(javafx.geometry.Pos.CENTER);

            FontIcon recentIcon = new FontIcon(last.getIconLiteral());
            recentIcon.setIconSize(18);
            recentIcon.setIconColor(last.getColor());

            Label nameLabel = new Label(last.getName());
            nameLabel.getStyleClass().add("badge-text");

            recentBadgeContent.getChildren().addAll(recentIcon, nameLabel);
            recentBadgeLabel.getChildren().setAll(recentBadgeContent);

        } else {
            Label placeholder = new Label("No badges earned yet.");
            placeholder.getStyleClass().add("badge-text"); // optional style
            recentBadgeLabel.getChildren().add(placeholder);        
        }
    }

    @FXML private void switchToSecondary() throws IOException { App.setRoot("secondary"); }
    @FXML private void switchToPrimary() throws IOException { App.setRoot("primary"); }
    @FXML private void switchToLeaderboard() throws IOException { App.setRoot("leaderboard"); }
    @FXML private void switchToStore() throws IOException {App.setRoot("store"); }
    @FXML private void switchToProfile() throws IOException { App.setRoot("profile"); }
    @FXML private void logoff() throws IOException { System.exit(0); }
}
