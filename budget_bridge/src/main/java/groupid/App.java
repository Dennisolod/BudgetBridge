package groupid;

import java.io.IOException;
import java.util.Optional;

import groupid.model.BudgetModel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
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
        getUsername();
        addDefaultPoints();
        addDefaultMissions();;

        scene = new Scene(loadAndInject("primary"));
        stage.setScene(scene);
        
       
        // Force css updates
        //scene.getRoot().applyCss();

        stage.show();
    }

    private void getUsername() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Welcome!");
        dialog.setHeaderText("Please enter your name");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresentOrElse(name -> model.usernameProperty().set(name.strip()), ()   -> Platform.exit());                  // quit if user cancels
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadAndInject(fxml));
    }

    private void addDefaultMissions(){
        model.addMission("Mission1", "daily", (double) 0.0);
        model.addMission("Mission2", "weekly", (double) 0.0);
        model.addMission("Mission3", "monthly", (double) 0.0);

    }

    private static Parent loadAndInject(String name) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/groupid/" + name + ".fxml"));
        Parent root = loader.load();

        Object ctrl = loader.getController();
        if (ctrl instanceof ModelAware) {
            ModelAware mAware = (ModelAware) ctrl;
            mAware.setModel(model);
        }
        return root;
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        Object c = fxmlLoader.getController();

        return fxmlLoader.load();
    }

    private static void addDefaultPoints(){
        model.addPoints(1000);
    }

    public static void main(String[] args) {
        launch();
    }

}