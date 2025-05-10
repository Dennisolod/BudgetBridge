package groupid;

import java.io.IOException;

import groupid.model.BudgetModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ProfileController implements ModelAware{

    @FXML private Label      nameLabel;
    @FXML private Label      balanceLabel;
    //@FXML private ImageView  avatarView;   // possible static image

    @Override public void setModel(BudgetModel m) {
        nameLabel.textProperty().bind(m.usernameProperty());
        balanceLabel.textProperty().bind(
            m.netBalanceProperty().asString("Net balance: $%.2f"));
        // avatarView could load based on username
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
