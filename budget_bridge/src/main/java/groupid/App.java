package groupid;
import java.io.IOException;
import java.util.zip.DataFormatException;

import groupid.model.BudgetInfoDAO;
import groupid.model.BudgetModel;
import groupid.model.DatabaseInitializer;
import groupid.model.MetaDataDAO;
import groupid.model.ProfileIcon;
import groupid.model.ThemeLine;
import groupid.model.UserDAO;
import javafx.application.Application;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private static final BudgetModel model = new BudgetModel();
    private static ScreenController screenController;

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("BudgetBridge");

        // Initialize database
        // DatabaseInitializer.clearDatabase();
        DatabaseInitializer.initialize();

        // Load the main screen with ScreenController
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/groupid/main_screen.fxml"));
        Parent root = loader.load();
        screenController = loader.getController();
        screenController.setModel(model);
        
        scene = new Scene(root, 1920, 1080);
        scene.getStylesheets().add(App.class.getResource("style.css").toExternalForm());
        
        stage.setScene(scene);
        stage.setFullScreen(true);

        // Set up callbacks
        screenController.setOnUsernameCollected(() -> {
            StringProperty username = model.usernameProperty();
            System.out.println("Username collected: " + username.get());
            System.out.println("User exists check: " + UserDAO.userExists(username));
            if (!UserDAO.userExists(username)) {
                System.out.println("New user path");
                // New user - will show budget setup
                // User will be added to database when budget setup is complete
            } else {
                // Existing user - load their data and go directly to primary
                int userId = UserDAO.getUserIdByName(username);
                BudgetInfoDAO.loadBudgetInfoFromDB(username, model);
                MetaDataDAO.loadMetaData(userId, model);
                fillMissionsList();
                addDefaultMissions();
                MetaDataDAO.populateLeaderboard(model);
                
                // Navigate to primary screen
                try {
                    System.out.println("Badges loaded: " + model.badges().size());
                    navigateToPrimary();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        screenController.setOnBudgetComplete(() -> {
            // After budget setup is complete, navigate to primary
            try {
                navigateToPrimary();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        stage.show();
    }

    public static void handleNewUserBudgetSetup() {
        // This is called from ScreenController when a new user completes budget setup
        // First, add the user to the database
        StringProperty username = model.usernameProperty();
        UserDAO.addUser(username);
        
        // Now get the user ID
        int userId = UserDAO.getUserIdByName(username);
        BudgetInfoDAO.saveBudgetInfo(userId, model);
        
        // Initialize missions for new user
        fillMissionsList();
        addDefaultMissions();
        
        // Load all data
        MetaDataDAO.loadMetaData(userId, model);
        BudgetInfoDAO.loadBudgetInfoFromDB(username, model);
        MetaDataDAO.populateLeaderboard(model);
        
        // Debug prints
        UserDAO.listUsers();
        BudgetInfoDAO.printBudgetInfo(userId);
    }

    private static void navigateToPrimary() throws IOException {
        scene.setRoot(loadAndInject("primary"));
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadAndInject(fxml));
    }

    private static void fillMissionsList() {

        model.addMissionList("Avoid purchasing snacks - 50pts & 50gems", "daily", (double) 0.0);
        model.addMissionList("Save $10 by purchasing cheaper alternatives - 150pts & 100gems", "weekly", (double) 0.0);
        model.addMissionList("Save $45 on groceries while keeping a healthier diet - 1000pts & 250gems", "monthly", (double) 0.0);
    }

    private static void addDefaultMissions() {
        model.addMission(0);
        model.addMission(1);
        model.addMission(2);
    }

    private static Parent loadAndInject(String name) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/groupid/" + name + ".fxml"));
        Parent root = loader.load();

        Object ctrl = loader.getController();
        if (ctrl instanceof ModelAware) {
            ModelAware mAware = (ModelAware) ctrl;
            mAware.setModel(model);
        }

        // Apply current theme background color
        if (model.getCurrentTheme() != null) {
            Color bg = model.getCurrentTheme().getBackgroundColor();
            String webColor = String.format("#%02x%02x%02x",
                (int)(bg.getRed() * 255),
                (int)(bg.getGreen() * 255),
                (int)(bg.getBlue() * 255)
            );
            root.setStyle("-fx-background-color: " + webColor + ";");
        }
        return root;
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}