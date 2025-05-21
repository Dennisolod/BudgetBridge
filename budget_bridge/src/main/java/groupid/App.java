package groupid;

import java.io.IOException;

import groupid.model.BudgetModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

        //                loadFXML("primary")
        scene = new Scene(loadFXML("primary"), 1920, 1080);
        scene.getStylesheets().add(App.class.getResource("style.css").toExternalForm());
        
        stage.setScene(scene);
       
        // Force css updates
        //scene.getRoot().applyCss();

        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        Parent root = fxmlLoader.load();

        Object c = fxmlLoader.getController();
    
        if (c instanceof PrimaryController ){
            PrimaryController pc = (PrimaryController) c;
            pc.setModel(model);
        }
        return root;
    }

    public static void main(String[] args) {
        launch();
    }

}