package groupid;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

import groupid.model.BudgetInfo;
import groupid.model.BudgetModel;
import groupid.model.DatabaseInitializer;
import groupid.model.UserDAO;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private static final BudgetModel model = new BudgetModel();

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("BudgetBridge");

        scene = new Scene(loadFXML("primary"), 1920, 1080);
        scene.getStylesheets().add(App.class.getResource("style.css").toExternalForm());
        
        scene = new Scene(loadAndInject("primary"));
        stage.setScene(scene);

        getUsername();
        getBudgetInfo(stage);
        fillMissionsList();
        addDefaultPoints();
        addDefaultMissions();

        DatabaseInitializer.initialize();
        UserDAO.addUser(model.usernameProperty());
        UserDAO.listUsers();

        // getBudgetInfo(stage);
        // Force css updates
        // scene.getRoot().applyCss();

        stage.show();
    }

    private void getUsername() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Welcome to BudgetBridge!");
        dialog.setHeaderText("Please enter your name");
        dialog.setContentText("Name:");


        Optional<String> result = dialog.showAndWait();
        result.ifPresentOrElse(name -> model.usernameProperty().set(name.strip()), ()   -> Platform.exit());                  // quit if user cancels
    }

    private void getBudgetInfo(Stage owner) throws IOException {
        URL url = App.class.getResource("/groupid/budget_setup.fxml");
        FXMLLoader loader = new FXMLLoader(url);       
        DialogPane pane = loader.load();               
        BudgetSetupController ctl = loader.getController();

        Dialog<BudgetInfo> dialog = new Dialog<>(); 
        dialog.setTitle("Welcome — Budget Setup");
        dialog.initOwner(owner);
        dialog.setDialogPane(pane);

        dialog.setResultConverter(bt ->
            (bt != null && bt.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                ? ctl.collectResult()
                : null);

        pane.getStylesheets().add(App.class.getResource("style.css").toExternalForm());

        dialog.showAndWait().ifPresent(info -> {
            model.loadBudgetInfo(info);
            model.generateCustomBudget(info);
            showBudgetSummaryDialog(model);
        });
    }

    private void showBudgetSummaryDialog(BudgetModel model) {
        StringBuilder sb = new StringBuilder("Here’s your suggested budget:\n\n");

        for (var m : model.expenses()) {
            sb.append(String.format("%s: $%.2f\n", m.getType(), m.getAmount()));
        }

        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Suggested Budget");
        alert.setHeaderText("Review Your Personalized Budget");
        alert.setContentText(sb.toString());

        // Optional: Use expandable text for long content
        javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        javafx.scene.layout.GridPane expContent = new javafx.scene.layout.GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);

        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
    }


    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadAndInject(fxml));

    }

    private void fillMissionsList() {
        model.addMissionList("Avoid purchasing snacks - 1000pts & 50gems", "daily", (double) 0.0);
        model.addMissionList("Save $10 by purchasing cheaper alternatives - 3000pts & 100gems", "weekly", (double) 0.0);
        model.addMissionList("Save $45 on groceries while keeping a healthier diet - 7000pts & 250gems", "monthly", (double) 0.0);
    }

    private void addDefaultMissions(){
        // when we implement more goals, we can just multiply 3 by the goal number and add 1 and 2 to that number for the 2nd and 3rd mission indexes
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
        Object c = fxmlLoader.getController();

        return fxmlLoader.load();
    }

    private static void addDefaultPoints(){
        model.addPoints(0);
    }

    public static void main(String[] args) {
        launch();
    }

}