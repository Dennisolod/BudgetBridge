package groupid;

import java.io.IOException;
import java.net.URL;

import groupid.model.BudgetInfo;
import groupid.model.BudgetModel;
import groupid.model.UserDAO;
import javafx.animation.FadeTransition;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class ScreenController {
    @FXML private StackPane screenContainer;
    @FXML private VBox usernameScreen;
    @FXML private VBox budgetSetupScreen;
    @FXML private VBox budgetSummaryScreen;
    
    @FXML private TextField usernameField;
    @FXML private Button usernameSubmitButton;
    
    @FXML private VBox budgetItemsContainer;
    @FXML private Label totalBudgetLabel;
    @FXML private Label welcomeLabel;
    @FXML private Label usernameErrorLabel;
    @FXML private Button continueButton;
    @FXML private Button editBudgetButton;
    
    private BudgetModel model;
    private Runnable onUsernameCollected;
    private Runnable onBudgetComplete;
    private boolean isNewUser = false;
    
    public void initialize() {
        showUsernameScreen();
        
        // Add enter key handler for username field
        if (usernameField != null) {
            usernameField.requestFocus();
        }
        
        usernameField.textProperty().addListener((obs, oldText, newText) -> {
            usernameErrorLabel.setVisible(false);
        });
    }
    
    public void setModel(BudgetModel model) {
        this.model = model;
    }
    
    public void setOnUsernameCollected(Runnable callback) {
        this.onUsernameCollected = callback;
    }
    
    public void setOnBudgetComplete(Runnable callback) {
        this.onBudgetComplete = callback;
    }
    
    private void showScreen(VBox screen) {
        // Hide all screens
        usernameScreen.setVisible(false);
        budgetSetupScreen.setVisible(false);
        budgetSummaryScreen.setVisible(false);
        
        // Show the requested screen with fade animation
        screen.setVisible(true);
        FadeTransition fade = new FadeTransition(Duration.millis(300), screen);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }
    
    private void showUsernameScreen() {
        showScreen(usernameScreen);
    }
    
    private void showBudgetSetupScreen() {
        try {
            URL budgetSetupUrl = App.class.getResource("/groupid/budget_setup.fxml");
            FXMLLoader loader = new FXMLLoader(budgetSetupUrl);
            DialogPane pane = loader.load();
            BudgetSetupController ctl = loader.getController();
            
            // Apply styles
            pane.getStylesheets().add(App.class.getResource("style.css").toExternalForm());
            
            Dialog<BudgetInfo> dialog = new Dialog<>();
            dialog.setTitle("Budget Setup");
            dialog.setDialogPane(pane);
            dialog.setResultConverter(bt -> 
                (bt != null && bt.getButtonData() == ButtonBar.ButtonData.OK_DONE) 
                    ? ctl.collectResult() 
                    : null);
            
            // Prevent dialog from closing the app
            dialog.initOwner(screenContainer.getScene().getWindow());
            
            dialog.showAndWait().ifPresentOrElse(
                info -> onBudgetSetupComplete(info),
                () -> {
                    // User cancelled - go back to username screen
                    showUsernameScreen();
                    usernameField.clear();
                    model.usernameProperty().set("");
                }
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void showBudgetSummaryScreen() {
        showScreen(budgetSummaryScreen);
    }
    
    @FXML
    private void handleUsernameSubmit() {
        StringProperty username = usernameField.textProperty();
        String name = username.get().strip();  // Trim spaces

        usernameErrorLabel.setVisible(false); // Hide error by default

        if (name.isEmpty()) {
            usernameErrorLabel.setText("Username cannot be empty.");
            usernameErrorLabel.setVisible(true);
            return;
        }

        if (!name.matches("^[a-zA-Z0-9_]{3,20}$")) {
            usernameErrorLabel.setText("Username must be 3-20 characters (letters, numbers, underscores).");
            usernameErrorLabel.setVisible(true);
            return;
        }

        model.usernameProperty().set(name);

        isNewUser = !UserDAO.userExists(model.usernameProperty());

        if (isNewUser) {
            showBudgetSetupScreen();
        } else {
            if (onUsernameCollected != null) {
                onUsernameCollected.run();
            }
        }
    }
    
    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Invalid Input");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    private void handleUsernameKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleUsernameSubmit();
        }
    }
    
    public void onBudgetSetupComplete(BudgetInfo info) {
        // Load budget info into model
        model.loadBudgetInfo(info);
        model.generateCustomBudget(info);
        
        // Save to database
        App.handleNewUserBudgetSetup();
        
        // Display the budget summary
        displayBudgetSummary();
        showBudgetSummaryScreen();
    }
    
    private void displayBudgetSummary() {
        welcomeLabel.setText("Welcome, " + model.usernameProperty().get() + "!");
        budgetItemsContainer.getChildren().clear();
        double totalExpenses = 0;
        double totalFixedExpenses = 0;
        double totalVariableExpenses = 0;
        double totalSavings = 0;
        
        // Add income display
        double totalIncome = model.totalIncomeProperty().get();
        if (totalIncome > 0) {
            // Income display with header
            Label incomeHeader = new Label("MONTHLY INCOME");
            incomeHeader.getStyleClass().add("section-header");
            budgetItemsContainer.getChildren().add(incomeHeader);
            
            HBox incomeBox = createBudgetItemDisplay("Total Monthly Income", totalIncome);
            incomeBox.getStyleClass().add("income-item");
            budgetItemsContainer.getChildren().add(incomeBox);
            budgetItemsContainer.getChildren().add(createSeparator());
            
        }
        
        // Fixed Expenses Section
        VBox fixedSection = new VBox(5);
        fixedSection.getStyleClass().add("section-box");
        
        Label fixedHeader = new Label("FIXED EXPENSES");
        fixedHeader.getStyleClass().add("section-header");
        fixedSection.getChildren().add(fixedHeader);
        
        // Variable Expenses Section
        VBox variableSection = new VBox(5);
        variableSection.getStyleClass().add("section-box");
        
        Label variableHeader = new Label("VARIABLE EXPENSES");
        variableHeader.getStyleClass().add("section-header");
        variableSection.getChildren().add(variableHeader);

        // Savings Section
        VBox savingsSection = new VBox(5);
        savingsSection.getStyleClass().add("section-box");
        Label savingsHeader = new Label("SAVINGS");
        savingsHeader.getStyleClass().add("section-header");
        savingsSection.getChildren().add(savingsHeader);
        
        // Add budget items from the generated custom budget
        for (var expense : model.expenses()) {
            HBox itemBox = createBudgetItemDisplay(expense.getType(), expense.getAmount());
            
            // Categorize expenses and savings
            if (expense.getFreq().equals("fixed")) {
                itemBox.getStyleClass().add("fixed-expense");
                fixedSection.getChildren().add(itemBox);
                totalFixedExpenses += expense.getAmount();
                totalExpenses += expense.getAmount();
            } else if(expense.getFreq().equals("variable")){
                // Check if this is actually a savings item
                if (expense.getType().toLowerCase().contains("saving") || 
                    expense.getType().toLowerCase().contains("emergency") ||
                    expense.getType().toLowerCase().contains("retirement")) {
                    itemBox.getStyleClass().add("savings-item");
                    savingsSection.getChildren().add(itemBox);
                    totalSavings += expense.getAmount();
                } else {
                    itemBox.getStyleClass().add("variable-expense");
                    variableSection.getChildren().add(itemBox);
                    totalVariableExpenses += expense.getAmount();
                    totalExpenses += expense.getAmount();
                }
            }
        }
        
        // Calculate percentages
        double fixedPercent = totalIncome > 0 ? (totalFixedExpenses / totalIncome) * 100 : 0;
        double variablePercent = totalIncome > 0 ? (totalVariableExpenses / totalIncome) * 100 : 0;
        double savingsPercent = totalIncome > 0 ? (totalSavings / totalIncome) * 100 : 0;
        
        // Add sections with percentage summaries
        if (fixedSection.getChildren().size() > 1) {
            // Add total with percentage
            HBox fixedTotal = createBudgetItemDisplayWithPercent("Total Fixed", totalFixedExpenses, fixedPercent);
            fixedTotal.getStyleClass().add("subtotal-item");
            fixedSection.getChildren().add(fixedTotal);
            
            budgetItemsContainer.getChildren().add(fixedSection);
            budgetItemsContainer.getChildren().add(createSeparator());
        }
        
        if (variableSection.getChildren().size() > 1) {
            // Add total with percentage
            HBox variableTotal = createBudgetItemDisplayWithPercent("Total Variable", totalVariableExpenses, variablePercent);
            variableTotal.getStyleClass().add("subtotal-item");
            variableSection.getChildren().add(variableTotal);
            
            budgetItemsContainer.getChildren().add(variableSection);
            budgetItemsContainer.getChildren().add(createSeparator());
        }
        
        if (savingsSection.getChildren().size() > 1) {
            // Add total with percentage
            HBox savingsTotal = createBudgetItemDisplayWithPercent("Total Savings", totalSavings, savingsPercent);
            savingsTotal.getStyleClass().add("subtotal-item");
            savingsSection.getChildren().add(savingsTotal);
            
            budgetItemsContainer.getChildren().add(savingsSection);
            budgetItemsContainer.getChildren().add(createSeparator());
        }
        
        // Summary section with allocation overview
        VBox summarySection = new VBox(8);
        summarySection.getStyleClass().add("summary-section");
        
        Label summaryHeader = new Label("ALLOCATION SUMMARY");
        summaryHeader.getStyleClass().add("section-header");
        summarySection.getChildren().add(summaryHeader);
        
        // Show allocation breakdown
        HBox fixedAllocation = createAllocationDisplay("Fixed Expenses", totalFixedExpenses, fixedPercent);
        HBox variableAllocation = createAllocationDisplay("Variable Expenses", totalVariableExpenses, variablePercent);
        HBox savingsAllocation = createAllocationDisplay("Savings", totalSavings, savingsPercent);
        
        summarySection.getChildren().addAll(fixedAllocation, variableAllocation, savingsAllocation);
        
        // Add savings recommendation if applicable
        if (totalIncome > 0) {
            Label recommendationLabel = new Label();
            recommendationLabel.getStyleClass().add("recommendation-label");
            
            if (savingsPercent >= 20) {
                recommendationLabel.setText("✓ Excellent! You're saving " + String.format("%.1f%%", savingsPercent) + " of your income (recommended: 20%+)");
                recommendationLabel.getStyleClass().add("recommendation-positive");
            } else if (savingsPercent >= 10) {
                recommendationLabel.setText("◐ Good! You're saving " + String.format("%.1f%%", savingsPercent) + " of your income (target: 20%+)");
                recommendationLabel.getStyleClass().add("recommendation-neutral");
            } else {
                recommendationLabel.setText("⚠ Consider increasing savings to 20% of income (currently: " + String.format("%.1f%%", savingsPercent) + ")");
                recommendationLabel.getStyleClass().add("recommendation-warning");
            }
            
            summarySection.getChildren().add(recommendationLabel);
        }
        
        budgetItemsContainer.getChildren().add(summarySection);
        
        totalBudgetLabel.setText(String.format("Total Allocated: $%.2f (%.1f%% of income)", 
            totalExpenses + totalSavings, 
            totalIncome > 0 ? ((totalExpenses + totalSavings) / totalIncome) * 100 : 0));
    }

// New helper method for displaying items with percentages
    private HBox createBudgetItemDisplayWithPercent(String type, double amount, double percent) {
        HBox itemBox = new HBox();
        itemBox.getStyleClass().add("budget-item");
        
        Label typeLabel = new Label(type);
        typeLabel.getStyleClass().add("budget-type");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label amountLabel = new Label(String.format("$%.2f (%.1f%%)", amount, percent));
        amountLabel.getStyleClass().add("budget-amount");
        
        itemBox.getChildren().addAll(typeLabel, spacer, amountLabel);
        return itemBox;
    }

    // New helper method for allocation summary display
    private HBox createAllocationDisplay(String category, double amount, double percent) {
        HBox allocationBox = new HBox();
        allocationBox.getStyleClass().add("allocation-item");
        
        Label categoryLabel = new Label(category + ":");
        categoryLabel.getStyleClass().add("allocation-category");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label allocationLabel = new Label(String.format("$%.2f (%.1f%%)", amount, percent));
        allocationLabel.getStyleClass().add("allocation-amount");
        
        allocationBox.getChildren().addAll(categoryLabel, spacer, allocationLabel);
        return allocationBox;
    }

    // Keep the original createBudgetItemDisplay method unchanged
    private HBox createBudgetItemDisplay(String type, double amount) {
        HBox itemBox = new HBox();
        itemBox.getStyleClass().add("budget-item");
        
        Label typeLabel = new Label(type);
        typeLabel.getStyleClass().add("budget-type");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label amountLabel = new Label(String.format("$%.2f", amount));
        amountLabel.getStyleClass().add("budget-amount");
        
        itemBox.getChildren().addAll(typeLabel, spacer, amountLabel);
        return itemBox;
    }
    
    private HBox createSeparator() {
        HBox separator = new HBox();
        separator.getStyleClass().add("budget-separator");
        separator.setPrefHeight(1);
        return separator;
    }
    
    @FXML
    private void handleContinue() {
        if (onBudgetComplete != null) {
            onBudgetComplete.run();
        }
    }
    
    @FXML
    private void handleEditBudget() {
        showBudgetSetupScreen();
    }
}