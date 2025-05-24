package groupid;

import java.io.IOException;

import groupid.model.BudgetModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

// Exhchange Store screen
public class StoreController implements ModelAware{
    @FXML private VBox themesBox;
    @FXML private VBox badgesBox;
    @FXML private VBox rewardsBox;
    @FXML private Label pointsLabel;
    @FXML private Label currencyBalance;

    @FXML private ListView<String> badgeList;
    
    private BudgetModel model;

    @Override 
    public void setModel(BudgetModel m) {
        this.model = m;
        currencyBalance.textProperty().bind(m.getGems().asString("%d Gems"));

        //pointsLabel.textProperty().bind(m.pointsProperty().asString("%d pts"));
        //badgeList.setItems(m.badges());
    }

    private void loadBadges() {
        // Example: Add a badge image + button
        ImageView badge = new ImageView(new Image("badge1.png"));
        badge.setFitWidth(50);
        badge.setFitHeight(50);
        Button redeem = new Button("Unlock (200 coins)");
        redeem.setOnAction(e -> attemptPurchase(200));
        badgesBox.getChildren().addAll(badge, redeem);
    }

    @FXML 
    private void buyBadge() {
        if (model.pointsProperty().get() >= 30) {
            model.pointsProperty().set(model.pointsProperty().get() - 30);
            //model.badges().add("Saver Level 1");
        }
    }

    private void loadThemes() {
        Button theme = new Button("Ocean Theme - 150 coins");
        theme.setOnAction(e -> attemptPurchase(150));
        themesBox.getChildren().add(theme);
    }

    private void loadOtherRewards() {
        Button item = new Button("Custom Avatar - 100 coins");
        item.setOnAction(e -> attemptPurchase(100));
        rewardsBox.getChildren().add(item);
    }

    private void attemptPurchase(int cost) {
        if (model.getGems().get() >= cost) {
            model.setGems(model.getGems().get() - cost);
            // Apply reward logic here (e.g., unlock badge, apply theme)
        } else {
            System.out.println("Not enough coins.");
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