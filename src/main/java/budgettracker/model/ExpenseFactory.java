package budgettracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Creates the right {@link Expense} subclass for a given {@link ExpenseType}.
 */
public final class ExpenseFactory {

    private ExpenseFactory() {
    }

    public static Expense create(ExpenseType type, String title, BigDecimal amount, LocalDate date) {
        return switch (type) {
            case FIXED -> new FixedExpense(title, amount, date);
            case VARIABLE -> new VariableExpense(title, amount, date);
            case SAVINGS -> new SavingsExpense(title, amount, date);
        };
    }
}
