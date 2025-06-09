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
    double debt;
    double groceries = 0.0;
    double diningOut = 0.0;
    double funMoney = 0.0;
    double otherExpense = 0.0;

    List<String> goals;   
    String budgetPlan;  
}
