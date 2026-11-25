package budgettracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Regular payment with a stable amount: subscriptions, rent, phone, internet.
 */
public class FixedExpense extends Expense {

    public FixedExpense(String title, BigDecimal amount, LocalDate date) {
        super(title, amount, date);
    }

    @Override
    public ExpenseType getType() {
        return ExpenseType.FIXED;
    }

    @Override
    public String getHint() {
        return "recurring payment - check if you still need it";
    }
}
