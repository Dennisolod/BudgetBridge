package groupid;

import java.io.IOException;

import org.kordamp.ikonli.javafx.FontIcon;

import groupid.model.BadgeLine;
import groupid.model.BudgetModel;
import groupid.model.ProfileIcon;
import groupid.model.ThemeLine;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


// Exchange Store screen
public class StoreController implements ModelAware{
    @FXML private VBox themesBox;
    @FXML private VBox badgesBox;
    @FXML private VBox rewardsBox; // This will contain profile icons
    @FXML private Label pointsLabel;
    @FXML private Label currencyBalance;
    @FXML private FontIcon userAvatarIcon;
    @FXML private ListView<String> badgeList;
    
    private BudgetModel model;

    @Override 
    public void setModel(BudgetModel m) {
        this.model = m;
        currencyBalance.textProperty().bind(m.getGems().asString("%d Gems"));

        loadBadgeItems();
        loadThemeItems();
        loadProfileIconItems(); // Load profile icons
    }

    // Profile Icon functions for the store:
    private void addProfileIcon(ProfileIcon profileIcon) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("store-item-card"); // Use the new CSS class

        // Create icon preview
        FontIcon icon = new FontIcon(profileIcon.getIconLiteral());
        icon.setIconSize(32);
        icon.setIconColor((Color) profileIcon.getColor());
        icon.getStyleClass().add("profile-icon-preview");

        // Create text content
        VBox textBox = new VBox(5);
        textBox.setAlignment(Pos.CENTER_LEFT);
        
        Label nameLabel = new Label(profileIcon.getName());
        nameLabel.getStyleClass().add("item-title");
        
        // Add description if available
        if (!profileIcon.getDescription().isEmpty()) {
            Label descLabel = new Label(profileIcon.getDescription());
            descLabel.getStyleClass().add("item-description");
            textBox.getChildren().add(descLabel);
        }
        
        Label costLabel = new Label(profileIcon.getCost() + " gems");
        costLabel.getStyleClass().add("item-price");
        
        textBox.getChildren().addAll(nameLabel, costLabel);

        // Spacer to push button to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Purchase/Apply button
        Button actionButton = new Button();
        actionButton.getStyleClass().add("purchase-btn");

        // Check if user owns this profile icon
        if (model.ownsProfileIcon(profileIcon)) {
            // Check if it's currently active
            if (model.getCurrentProfileIcon() != null && 
                model.getCurrentProfileIcon().equals(profileIcon)) {
                actionButton.setText("Active");
                actionButton.setDisable(true);
                actionButton.getStyleClass().add("active-btn");
            } else {
                actionButton.setText("Apply");
                actionButton.setOnAction(e -> {
                    model.applyProfileIcon(profileIcon);
                    refreshProfileIconButtons(); // Refresh all buttons to update states
                });
            }
        } else {
            actionButton.setText("Purchase");
            actionButton.setOnAction(e -> {
                if (model.getGems().get() >= profileIcon.getCost()) {
                    model.setGems(model.getGems().get() - profileIcon.getCost());
                    model.unlockProfileIcon(profileIcon);
                    model.applyProfileIcon(profileIcon); // Auto-apply after purchase
                    refreshProfileIconButtons(); // Refresh all buttons
                } else {
                    // Could show a more user-friendly error message
                    System.out.println("Not enough gems to purchase " + profileIcon.getName());
                }
            });
        }

        row.getChildren().addAll(icon, textBox, spacer, actionButton);
        rewardsBox.getChildren().add(row);
    }

    private void loadProfileIconItems() {
        rewardsBox.getChildren().clear();
        rewardsBox.getStyleClass().add("store-column");

        // Basic Profile Icons (Tier 1)
        addProfileIcon(new ProfileIcon("Classic Avatar", "fas-user", Color.LIGHTBLUE, 100, "Simple and clean profile look"));
        addProfileIcon(new ProfileIcon("Star Performer", "fas-star", Color.GOLD, 150, "Show your stellar progress"));
        addProfileIcon(new ProfileIcon("Savings Shield", "fas-shield-alt", Color.DARKBLUE, 200, "Defender of budgets"));
        addProfileIcon(new ProfileIcon("Money Tree", "fas-leaf", Color.GREEN, 250, "Growing your wealth"));
        
        // Premium Profile Icons (Tier 2)
        addProfileIcon(new ProfileIcon("Diamond Elite", "fas-gem", Color.CYAN, 500, "Precious and rare"));
        addProfileIcon(new ProfileIcon("Golden Crown", "fas-crown", Color.GOLD, 750, "Royal budgeting status"));
        addProfileIcon(new ProfileIcon("Lightning Bolt", "fas-bolt", Color.YELLOW, 600, "Fast financial decisions"));
        addProfileIcon(new ProfileIcon("Fire Phoenix", "fas-fire", Color.ORANGERED, 800, "Rise from debt ashes"));
        
        // Legendary Profile Icons (Tier 3)
        addProfileIcon(new ProfileIcon("Infinity Master", "fas-infinity", Color.MEDIUMPURPLE, 1200, "Endless financial wisdom"));
        addProfileIcon(new ProfileIcon("Cosmic Dragon", "fas-dragon", Color.DARKVIOLET, 1500, "Mythical money manager"));
        addProfileIcon(new ProfileIcon("Time Keeper", "fas-hourglass-half", Color.DARKORANGE, 1000, "Master of time and money"));
        addProfileIcon(new ProfileIcon("Quantum Core", "fas-atom", Color.LIGHTCYAN, 2000, "Advanced financial physics"));
        
        // Ultra Rare Profile Icons (Tier 4)
        addProfileIcon(new ProfileIcon("Celestial Being", "fas-sun", Color.GOLD, 3000, "Enlightened financial guru"));
        addProfileIcon(new ProfileIcon("Void Walker", "fas-mask", Color.DARKSLATEGRAY, 2500, "Mysterious wealth builder"));
        addProfileIcon(new ProfileIcon("Arch Mage", "fas-magic", Color.INDIGO, 3500, "Master of financial magic"));
        addProfileIcon(new ProfileIcon("Universe Creator", "fas-globe", Color.DEEPSKYBLUE, 5000, "Builder of financial worlds"));
    }

    // Helper method to refresh all profile icon buttons
    private void refreshProfileIconButtons() {
        // Clear and reload the profile icons to update button states
        loadProfileIconItems();
    }

    // Badges functions for the store (keeping existing code):
    private void addBadge(BadgeLine badge, int cost) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("store-item-card");

        FontIcon icon = new FontIcon(badge.getIconLiteral());
        icon.setIconSize(28);
        icon.setIconColor((Color) badge.getColor());

        VBox textBox = new VBox(5);
        Label nameLabel = new Label(badge.getName());
        nameLabel.getStyleClass().add("item-title");
        Label costLabel = new Label(cost + " gems");
        costLabel.getStyleClass().add("item-price");
        textBox.getChildren().addAll(nameLabel, costLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button purchaseButton = new Button("Purchase");
        purchaseButton.getStyleClass().add("purchase-btn");

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
                    System.out.println("Not enough gems.");
                }
            });
        }

        row.getChildren().addAll(icon, textBox, spacer, purchaseButton);
        badgesBox.getChildren().add(row);
    }

    public void loadBadgeItems() {
        badgesBox.getChildren().clear();
        badgesBox.getStyleClass().add("store-column");

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
        }
    }

    private void attemptPurchase(String itemName, int cost) {
        if (model.getGems().get() >= cost) {
            model.setGems(model.getGems().get() - cost);
            System.out.println("Unlocked: " + itemName);
        } else {
            System.out.println("Not enough gems.");
        }
    }

    // Theme functions for the store (keeping existing code):
    private void addThemeToStore(ThemeLine theme) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("store-item-card");

        Rectangle colorPreview = new Rectangle(30, 30, theme.getBackgroundColor());
        colorPreview.setArcWidth(10);
        colorPreview.setArcHeight(10);

        VBox textBox = new VBox(5);
        Label nameLabel = new Label(theme.getName());
        nameLabel.getStyleClass().add("item-title");
        Label costLabel = new Label(theme.getCost() + " gems");
        costLabel.getStyleClass().add("item-price");
        textBox.getChildren().addAll(nameLabel, costLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button actionButton = new Button();
        actionButton.getStyleClass().add("purchase-btn");

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
                } else {
                    System.out.println("Not enough gems.");
                }
            });
        }

        row.getChildren().addAll(colorPreview, textBox, spacer, actionButton);
        themesBox.getChildren().add(row);
    }

    private void loadThemeItems() {
        themesBox.getChildren().clear();
        themesBox.getStyleClass().add("store-column");

        addThemeToStore(new ThemeLine("Ocean Blue", Color.web("#003CFF"), 5000));
        addThemeToStore(new ThemeLine("Mystic Purple", Color.web("#1F1B2E"), 10000));
        addThemeToStore(new ThemeLine("Golden Hour", Color.web("#FFD700"), 25000));
        addThemeToStore(new ThemeLine("Seafoam Savings", Color.web("#0AFF9D"), 35000));
        addThemeToStore(new ThemeLine("Aurora", Color.web("#003CFF"), 40000));
    }

    // Navigation methods (keeping existing):
    @FXML private void switchToSecondary() throws IOException { App.setRoot("secondary"); }
    @FXML private void switchToPrimary() throws IOException { App.setRoot("primary"); }
    @FXML private void switchToLeaderboard() throws IOException { App.setRoot("leaderboard"); }
    @FXML private void switchToStore() throws IOException {App.setRoot("store"); }
    @FXML private void switchToProfile() throws IOException { App.setRoot("profile"); }
    @FXML private void logoff() throws IOException { System.exit(0); }
}