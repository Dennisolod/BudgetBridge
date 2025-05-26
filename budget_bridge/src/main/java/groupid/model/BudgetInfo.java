package groupid.model;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BudgetInfo {
    double primaryIncome;
    double sideIncome;
    double otherIncome;
    double rent;
    double car;
    double groceries;
    double diningOut;
    double funMoney;
    double otherExpense;

    List<String> goals;   
    String budgetPlan;  
}
