package budgettracker.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseTest {
    private static final LocalDate DAY = LocalDate.of(2026, 11, 25);

    @Test
    void amountIsRoundedToCents() {
        Expense e = new FixedExpense("Phone", new BigDecimal("8.505"), DAY);
        assertEquals(new BigDecimal("8.51"), e.getAmount());
    }

    @Test
    void titleIsTrimmed() {
        Expense e = new VariableExpense("  Taxi  ", new BigDecimal("12"), DAY);
        assertEquals("Taxi", e.getTitle());
    }

    @Test
    void emptyTitleIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new FixedExpense("  ", BigDecimal.TEN, DAY));
    }

    @Test
    void zeroOrNegativeAmountIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new FixedExpense("Rent", BigDecimal.ZERO, DAY));
        assertThrows(IllegalArgumentException.class,
                () -> new FixedExpense("Rent", new BigDecimal("-5"), DAY));
    }

    @Test
    void factoryCreatesCorrectSubclass() {
        assertInstanceOf(FixedExpense.class, ExpenseFactory.create(ExpenseType.FIXED, "a", BigDecimal.ONE, DAY));
        assertInstanceOf(VariableExpense.class, ExpenseFactory.create(ExpenseType.VARIABLE, "a", BigDecimal.ONE, DAY));
        assertInstanceOf(SavingsExpense.class, ExpenseFactory.create(ExpenseType.SAVINGS, "a", BigDecimal.ONE, DAY));
    }

    @Test
    void savingsIsNotSpending() {
        assertTrue(ExpenseType.FIXED.isSpending());
        assertTrue(ExpenseType.VARIABLE.isSpending());
        assertFalse(ExpenseType.SAVINGS.isSpending());
    }
}
