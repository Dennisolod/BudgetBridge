package groupid;

import java.io.IOException;

import groupid.model.BadgeLine;
import groupid.model.BudgetModel;
import groupid.model.ThemeLine;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.Region;
import groupid.model.League;

import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

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

        loadBadgeItems();
        loadThemeItems();
        //pointsLabel.textProperty().bind(m.pointsProperty().asString("%d pts"));
        //badgeList.setItems(m.badges());

    }


    // Badges functions for the store:
    private void addBadge(BadgeLine badge, int cost) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #222c3c; -fx-background-radius: 8px; -fx-padding: 12;");

        FontIcon icon = new FontIcon(badge.getIconLiteral());
        icon.setIconSize(28);
        icon.setIconColor((Color) badge.getColor());

        VBox textBox = new VBox(5);
        Label nameLabel = new Label(badge.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label costLabel = new Label(cost + " coins");
        costLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 14px;");
        textBox.getChildren().addAll(nameLabel, costLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button purchaseButton = new Button("Purchase");
        purchaseButton.getStyleClass().add("modern-flat-button");

        if (model.ownsBadge(badge)) {
            purchaseButton.setText("Owned");
            purchaseButton.setDisable(true);
        } else {
            purchaseButton.setOnAction(e -> {
                if (model.getGems().get() >= cost) {
                    model.setGems(model.getGems().get() - cost);
                    model.unlockBadge(badge);
                    purchaseButton.setText("Owned");
                    purchaseButton.setDisable(true);
                } else {
                    System.out.println("Not enough coins.");
                }
            });
        }

        row.getChildren().addAll(icon, textBox, spacer, purchaseButton);
        badgesBox.getChildren().add(row);

    }


    public void loadBadgeItems() {
        badgesBox.getChildren().clear(); // clears if there are any left
        badgesBox.setStyle("-fx-background-color: #202538; -fx-background-radius: 8px; -fx-padding: 12;");

        // Tier 1 badges:
        addBadge(new BadgeLine("Gold Trophy", "fas-trophy", Color.GOLD), 200);
        addBadge(new BadgeLine("Shield of Honor", "fas-shield-alt", Color.DARKGRAY), 180);
        addBadge(new BadgeLine("Mythic Phoenix", "fas-fire-alt", Color.ORANGERED), 500);
        addBadge(new BadgeLine("Crown Elite", "fas-crown", Color.GOLD), 250);
        addBadge(new BadgeLine("Champion Medal", "fas-medal", Color.SILVER), 300);
        addBadge(new BadgeLine("Secret Flame", "fas-fire", Color.ORANGERED), 220);

        // Tier 2 badges
        addBadge(new BadgeLine("Mythic Phoenix", "fas-fire-alt", Color.CRIMSON), 500);
        addBadge(new BadgeLine("Infinity Crown", "fas-crown", Color.MEDIUMPURPLE), 600);
        addBadge(new BadgeLine("Legend of Time", "fas-hourglass-half", Color.DARKORANGE), 750);
        addBadge(new BadgeLine("Quantum Vault", "fas-lock", Color.DARKCYAN), 900);
        addBadge(new BadgeLine("Ethereal Wings", "fas-feather-alt", Color.LIGHTSKYBLUE), 1000);


        // Tier 3 badges: 
        addBadge(new BadgeLine("Celestial Flame", "fas-sun", Color.ORANGERED), 1500);
        addBadge(new BadgeLine("Eternal Crown", "fas-gem", Color.MEDIUMPURPLE), 2000);
        addBadge(new BadgeLine("Timekeeper's Halo", "fas-infinity", Color.DEEPSKYBLUE), 2500);
        addBadge(new BadgeLine("Quantum Ascendant", "fas-rocket", Color.LIGHTGOLDENRODYELLOW), 3000);
        addBadge(new BadgeLine("Divine Architect", "fas-chess-king", Color.GOLD), 4000);
    }

    @FXML 
    private void buyBadge() {
        if (model.pointsProperty().get() >= 30) {
            model.pointsProperty().set(model.pointsProperty().get() - 30);
            //model.badges().add("Saver Level 1");
        }
    }

    private void attemptPurchase(String itemName, int cost) {
        if (model.getGems().get() >= cost) {
            model.setGems(model.getGems().get() - cost);
            System.out.println("Unlocked: " + itemName);
            // Add to user's unlocked badges: model.addUnlockedBadge(itemName);
        } else {
            System.out.println("Not enough coins.");
    }
}

    // Theme functions for the store:
    private void addThemeToStore(ThemeLine theme) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #222c3c; -fx-background-radius: 8px; -fx-padding: 12;");

        Rectangle colorPreview = new Rectangle(30, 30, theme.getBackgroundColor());
        colorPreview.setArcWidth(10);
        colorPreview.setArcHeight(10);

        Label nameLabel = new Label(theme.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label costLabel = new Label(theme.getCost() + " coins");
        costLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button actionButton = new Button();
        actionButton.getStyleClass().add("modern-flat-button");

        if (model.ownsTheme(theme)) {
            actionButton.setText("Apply");
            actionButton.setOnAction(e -> model.applyTheme(theme));
        } else {
            actionButton.setText("Purchase");
            actionButton.setOnAction(e -> {
                if (model.getGems().get() >= theme.getCost()) {
                    model.setGems(model.getGems().get() - theme.getCost());
                    model.unlockTheme(theme);
                    model.applyTheme(theme);
                    // reloadThemeStore(); // optional refresh
                } else {
                    System.out.println("Not enough coins.");
                }
            });
    }

    row.getChildren().addAll(colorPreview, nameLabel, costLabel, spacer, actionButton);
    themesBox.getChildren().add(row);
}

    private void loadThemeItems() {
        themesBox.getChildren().clear();
        themesBox.setStyle("-fx-background-color: #202538; -fx-background-radius: 8px; -fx-padding: 12;");

        addThemeToStore(new ThemeLine("Ocean Blue", Color.web("#003CFF"), 5000));
        addThemeToStore(new ThemeLine("Mystic Purple", Color.web("#1F1B2E"), 10000));
        addThemeToStore(new ThemeLine("Golden Hour", Color.web("#FFD700"), 25000));
        addThemeToStore(new ThemeLine("Seafoam Savings", Color.web("#0AFF9D"), 35000));
        addThemeToStore(new ThemeLine("Aurora", Color.web("#003CFF"), 40000));
    }

    @FXML private void switchToSecondary() throws IOException { App.setRoot("secondary"); }
    @FXML private void switchToPrimary() throws IOException { App.setRoot("primary"); }
    @FXML private void switchToLeaderboard() throws IOException { App.setRoot("leaderboard"); }
    @FXML private void switchToStore() throws IOException {App.setRoot("store"); }
    @FXML private void switchToProfile() throws IOException { App.setRoot("profile"); }
    @FXML private void logoff() throws IOException { System.exit(0); }

    
}