package budgettracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Money moved to savings. It leaves the main account but is not spent,
 * so it is not counted as spending.
 */
public class SavingsExpense extends Expense {

    public SavingsExpense(String title, BigDecimal amount, LocalDate date) {
        super(title, amount, date);
    }

    @Override
    public ExpenseType getType() {
        return ExpenseType.SAVINGS;
    }

    @Override
    public String getHint() {
        return "well done, money put aside";
    }
}
