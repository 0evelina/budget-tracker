package budgettracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Everyday spending that changes from day to day: groceries, taxi, cafes.
 * Variable expenses are checked against the daily limit.
 */
public class VariableExpense extends Expense {

    public VariableExpense(String title, BigDecimal amount, LocalDate date) {
        super(title, amount, date);
    }

    @Override
    public ExpenseType getType() {
        return ExpenseType.VARIABLE;
    }

    @Override
    public String getHint() {
        return "counts towards the daily limit";
    }
}
