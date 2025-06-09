package groupid;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.kordamp.ikonli.javafx.FontIcon;

import groupid.model.BadgeLine;
import groupid.model.BudgetModel;
import groupid.model.MissionLine;
import groupid.model.MoneyLine;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
        budgetPie.setData(pieData);

        budgetPie.setLabelsVisible(true);
        rebuildPieData(pieData);
        model.expenses().addListener(
            (ListChangeListener<MoneyLine>) change -> rebuildPieData(pieData));
        }

    private void rebuildPieData(ObservableList<PieChart.Data> pieData) {
        pieData.clear();
        for (MoneyLine m : model.getCurrentMonth()) {
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


        for (MoneyLine expense : model.expenses()) {
            String category = expense.getType();
            double budget = expense.getAmount();

            // Create GridPane row
            GridPane row = new GridPane();
            row.setHgap(10);
            row.setVgap(5);

            // Column constraints for consistent alignment
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setMinWidth(250); // label column
            ColumnConstraints col2 = new ColumnConstraints();
            col2.setMinWidth(250); // progress bar column
            col2.setHgrow(Priority.ALWAYS);
            ColumnConstraints col3 = new ColumnConstraints();
            col3.setMinWidth(120); // button column
            row.getColumnConstraints().addAll(col1, col2, col3);

            // Create label showing progress
            Label label = new Label(String.format("%s: $%.2f of $%.2f spent", category, expense.getSpent(), budget));
            label.getStyleClass().add("expense-label");
            label.getStyleClass().add("expense-label");

            ProgressBar bar = new ProgressBar();
            bar.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(bar, Priority.ALWAYS);
            bar.progressProperty().bind(
                Bindings.createDoubleBinding(
                    () -> budget == 0 ? 0 : expense.getSpent() / budget,
                    expense.spentProperty()
                )
            );

            // Penalty and color update logic
            expense.spentProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() > budget) {
                    bar.setStyle("-fx-accent: red;");
                    model.getGems().set(Math.max(0, model.getGems().get() - 5));
                    model.pointsProperty().set(Math.max(0, model.pointsProperty().get() - 10));
                } else {
                    bar.setStyle("-fx-accent: #f08080;");
                }

            label.setText(String.format("%s: $%.2f of $%.2f spent", category, newVal.doubleValue(), budget));
            });

            Button logButton = new Button("Log Purchase");
            logButton.setPrefWidth(120);
            logButton.setOnAction(e -> {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Log Purchase");
                dialog.setHeaderText("Enter purchase amount for " + category + ":");
                dialog.setContentText("Amount:");

                dialog.showAndWait().ifPresent(input -> {
                    try {
                        double value = Double.parseDouble(input);
                        if (value < 0) throw new NumberFormatException();
                        expense.setSpent(expense.getSpent() + value);  // update spent
                    } catch (NumberFormatException ex) {
                        System.out.println("Invalid input");
                    }
                });
            });

            // Add components to GridPane row
            row.add(label, 0, 0);
            row.add(bar, 1, 0);
            row.add(logButton, 2, 0);

            // Add spacing between rows
            VBox.setMargin(row, new Insets(8, 0, 8, 0));
            expenseProgressVBox.getChildren().add(row);
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

    @FXML private void switchToSecondary() throws IOException { App.setRoot("secondary"); }
    @FXML private void switchToPrimary() throws IOException { App.setRoot("primary"); }
    @FXML private void switchToLeaderboard() throws IOException { App.setRoot("leaderboard"); }
    @FXML private void switchToStore() throws IOException {App.setRoot("store"); }
    @FXML private void switchToProfile() throws IOException { App.setRoot("profile"); }
    @FXML private void logoff() throws IOException { System.exit(0); }


}
