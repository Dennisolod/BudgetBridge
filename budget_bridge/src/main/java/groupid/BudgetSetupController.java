package groupid;

import java.util.List;
import java.util.stream.Stream;

import groupid.model.BudgetInfo;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;


public class BudgetSetupController {
    @FXML private DialogPane rootPane;          
    @FXML private TextField tfPrimaryIncome, tfSideIncome, tfOtherIncome;
    @FXML private TextField tfRent, tfCar, tfGroceries, tfDining, tfFun, tfOtherExpense;
    @FXML private CheckBox  chkGoal1, chkGoal2, chkGoal3, chkGoal4;
    @FXML private ToggleGroup planGroup;

    public BudgetInfo collectResult() {
        BudgetInfo info = BudgetInfo.builder()
                .primaryIncome(parse(tfPrimaryIncome))
                .sideIncome(parse(tfSideIncome))
                .otherIncome(parse(tfOtherIncome))
                .rent(parse(tfRent))
                .car(parse(tfCar))
                .groceries(parse(tfGroceries))
                .diningOut(parse(tfDining))
                .funMoney(parse(tfFun))
                .otherExpense(parse(tfOtherExpense))
                .goals(selectedGoals())
                .budgetPlan(selectedPlan())
                .build();
            return info;

    }

    private double parse(TextField tf) {
        try { return Double.parseDouble(tf.getText().strip()); }
        catch (NumberFormatException ex) { return 0.0; }
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
