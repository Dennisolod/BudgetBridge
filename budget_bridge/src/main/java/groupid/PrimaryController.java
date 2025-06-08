package groupid;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.kordamp.ikonli.javafx.FontIcon;

import groupid.model.BadgeLine;
import groupid.model.BudgetModel;
import groupid.model.MissionLine;
import groupid.model.MoneyLine;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

// Homescreen / My Dashboard
public class PrimaryController implements ModelAware, Initializable {
    @FXML private BorderPane rootPane;
    @FXML private Label userLabel;
    @FXML private Label netLabel;
    @FXML private Label pointsLabel;
    @FXML private Label gemsLabel;
    @FXML private VBox incomeVBox;
    @FXML private VBox expenseVBox;
    @FXML private VBox expenseProgressVBox;

    @FXML private ListView<MoneyLine> incomeList;
    @FXML private ListView<MoneyLine> expenseList;
    @FXML private ListView<MissionLine> missionList;
    @FXML private ListView<BadgeLine> badgeList;
    @FXML private PieChart budgetPie;

    private BudgetModel model;

    @Override
    public void setModel(BudgetModel m) {
        this.model = m;

        updateExpenseProgress(m);
        
        userLabel.textProperty().bind(m.usernameProperty());
        missionList.setItems(m.missions());
        badgeList.setItems(m.getOwnedBadges());
        //pointsLabel.textProperty().bind(m.pointsProperty().asString());

        //netLabel.textProperty().bind(m.netBalanceProperty().asString("$%.2f"));

        //incomeList.setItems(m.incomes());
        //expenseList.setItems(m.expenses());

        model.currentThemeProperty().addListener((obs, oldTheme, newTheme) -> {
            if (newTheme != null) {
                rootPane.setStyle("-fx-background-color: " + toWebColor(newTheme.getBackgroundColor()) + ";");
            }
        });

        // Apply immediately if theme is already set
        if (model.getCurrentTheme() != null) {
            rootPane.setStyle("-fx-background-color: " + toWebColor(model.getCurrentTheme().getBackgroundColor()) + ";");
        }

        addMoneyLines(incomeVBox, model.incomes(), "income");
        addMoneyLines(expenseVBox, model.expenses(), "expense");

        gemsLabel.textProperty().bind(m.getGems().asString("%,d Gems!"));
        initPie();
        
    }

    private String toWebColor(Color color) {
        return String.format("#%02x%02x%02x",
            (int)(color.getRed() * 255),
            (int)(color.getGreen() * 255),
            (int)(color.getBlue() * 255)
        );
    }

    private void initPie() {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (MoneyLine m : model.expenses()){
            pieData.add(new PieChart.Data(m.getType(), m.getAmount()));
        }
        budgetPie.setData(pieData);

        budgetPie.setLabelsVisible(true);
        rebuildPieData(pieData);
        model.expenses().addListener(
            (ListChangeListener<MoneyLine>) change -> rebuildPieData(pieData));
        }

    private void rebuildPieData(ObservableList<PieChart.Data> pieData) {
        pieData.clear();
        for (MoneyLine m : model.expenses()) {
            pieData.add(new PieChart.Data(m.getType(), m.getAmount()));
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        badgeList.setCellFactory(lv -> new ListCell<>() {
            private final FontIcon icon = new FontIcon();
            private final Label    name = new Label();
            private final HBox     row  = new HBox(20, icon, name);
            @Override protected void updateItem(BadgeLine b, boolean empty) {
                super.updateItem(b, empty);
                if (empty || b == null) { setGraphic(null); }
                else {
                    icon.setIconLiteral(b.getIconLiteral());
                    icon.setIconColor(b.getColor());
                    icon.setIconSize(48);
                    name.setText(b.getName());
                    name.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
                    setGraphic(row);
                }
            }
        });
       
    }

    // HELPERS
    private void updateExpenseProgress(BudgetModel model) {
        expenseProgressVBox.getChildren().clear();  // clear old content

        double totalIncome = model.totalIncomeProperty().get();

        for (MoneyLine expense : model.expenses()) {
            String category = expense.getType();
            double amount = expense.getAmount();

            double progress = totalIncome == 0 ? 0 : amount / totalIncome;

            // Create label
            Label label = new Label(String.format("%s - $%.2f of $%.2f", category, amount, totalIncome));
            label.getStyleClass().add("expense-label");

            // Create progress bar
            ProgressBar bar = new ProgressBar(progress);
            bar.setPrefWidth(250);
            bar.setStyle("-fx-accent: #f08080;"); // light coral color

            VBox entry = new VBox(5, label, bar);
            expenseProgressVBox.getChildren().add(entry);
        }
    }


    public void addMoneyLines(VBox target, ObservableList<MoneyLine> list, String styleClass) {
        for (MoneyLine moneyLine : list) {
            Label label = new Label(String.format("$%.2f", moneyLine.getAmount()));
            label.getStyleClass().add(styleClass);
            target.getChildren().add(label);
        }
    }

    // I don't know what this function is supposed to do, when I comment it out everything works normally
    public void addBadge(BadgeLine badge) {
        if (!badgeList.getItems().contains(badge)) {
            badgeList.getItems().add(badge);
        }
    }

    @FXML private void onAddIncome()  { model.addIncome("Side Hustle", 200); }
    @FXML private void onAddExpense() { model.addExpense("Coffee", 5);   }

    @FXML private void switchToSecondary() throws IOException { App.setRoot("secondary"); }
    @FXML private void switchToPrimary() throws IOException { App.setRoot("primary"); }
    @FXML private void switchToLeaderboard() throws IOException { App.setRoot("leaderboard"); }
    @FXML private void switchToStore() throws IOException {App.setRoot("store"); }
    @FXML private void switchToProfile() throws IOException { App.setRoot("profile"); }
    @FXML private void logoff() throws IOException { System.exit(0); }


}
