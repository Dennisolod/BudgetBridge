package groupid;

import java.io.IOException;

import javafx.fxml.FXML;

// Homescreen / My Dashboard
public class PrimaryController {

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
}
