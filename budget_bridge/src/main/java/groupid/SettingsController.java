package groupid;

import java.io.IOException;

import javafx.fxml.FXML;

public class SettingsController {
    
    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }

    @FXML
    private void switchToLeaderboard() throws IOException {
        App.setRoot("leaderboard");
    }

    @FXML
    private void switchToStore() throws IOException {
        App.setRoot("store");
    }

    @FXML
    private void switchToProfile() throws IOException {
        App.setRoot("profile");
    }

    @FXML
    private void switchToSettings() throws IOException {
        App.setRoot("settings");
    }

    @FXML
    private void switchToTutorial() throws IOException {
        App.setRoot("tutorial");
    }

    @FXML
    private void logoff() throws IOException {
        System.exit(0);
    }
}
