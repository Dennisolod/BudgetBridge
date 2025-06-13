package groupid;

import java.util.List;
import java.util.stream.Stream;

import groupid.model.BudgetInfo;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Alert;

public class BudgetSetupController {
    @FXML private DialogPane rootPane;
    @FXML private TextField tfPrimaryIncome, tfSideIncome, tfOtherIncome;
    @FXML private TextField tfRent, tfCar, tfDebt, tfIns;
    @FXML private CheckBox chkGoal1, chkGoal2, chkGoal3, chkGoal4;
    @FXML private ToggleGroup planGroup;

    @FXML private Label errPrimaryIncome;
    @FXML private Label errSideIncome;
    @FXML private Label errOtherIncome;
    @FXML private Label errRent;
    @FXML private Label errCar;
    @FXML private Label errIns;
    @FXML private Label errDebt;

    public BudgetInfo collectResult() {
        boolean hasError = false;

        Double primary = parseAndValidate(tfPrimaryIncome, errPrimaryIncome, "Primary Income");
        Double side = parseAndValidate(tfSideIncome, errSideIncome, "Side Income");
        Double other = parseAndValidate(tfOtherIncome, errOtherIncome, "Other Income");

        Double rent = parseAndValidate(tfRent, errRent, "Rent");
        Double car = parseAndValidate(tfCar, errCar, "Car Payment");
        Double ins = parseAndValidate(tfIns, errIns, "Insurance");
        Double debt = parseAndValidate(tfDebt, errDebt, "Debt");

       // Check individually
        if (primary == null) hasError = true;
        if (side == null) hasError = true;
        if (other == null) hasError = true;
        if (rent == null) hasError = true;
        if (car == null) hasError = true;
        if (ins == null) hasError = true;
        if (debt == null) hasError = true;

        if (hasError) return null;

        BudgetInfo info = BudgetInfo.builder()
            .primaryIncome(primary)
            .sideIncome(side)
            .otherIncome(other)
            .rent(rent)
            .car(car)
            .ins(ins)
            .debt(debt)
            .goals(selectedGoals())
            .budgetPlan(selectedPlan())
            .build();

        return info;
    }

    private Double parseAndValidate(TextField tf, Label errorLabel, String fieldName) {
        String input = tf.getText().strip();

        if (input.isEmpty()) {
            errorLabel.setText(fieldName + " cannot be empty.");
            errorLabel.setVisible(true);
            return null;
        }

        try {
            double value = Double.parseDouble(input);
            if (value < 0) {
                errorLabel.setText(fieldName + " cannot be negative.");
                errorLabel.setVisible(true);
                return null;
            }
            if (value > 1_000_000) {
                errorLabel.setText(fieldName + " seems unreasonably large.");
                errorLabel.setVisible(true);
                return null;
            }
            return value;
        } catch (NumberFormatException ex) {
            errorLabel.setText(fieldName + " must be a valid number.");
            errorLabel.setVisible(true);
            return null;
        }
    }

    private List<String> selectedGoals() {
        return Stream.of(chkGoal1, chkGoal2, chkGoal3, chkGoal4)
                     .filter(CheckBox::isSelected)
                     .map(CheckBox::getText)
                     .toList();
    }

    private String selectedPlan() {
        Toggle t = planGroup.getSelectedToggle();
        return t == null ? "" : ((RadioButton) t).getText();
    }
}
