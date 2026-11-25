package budgettracker.service;

import budgettracker.model.Expense;
import budgettracker.model.ExpenseType;
import budgettracker.model.FixedExpense;
import budgettracker.model.SavingsExpense;
import budgettracker.model.VariableExpense;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BudgetServiceTest {
    private static final LocalDate MON = LocalDate.of(2026, 11, 23);
    private static final LocalDate TUE = LocalDate.of(2026, 11, 24);

    private BudgetService service;

    @BeforeEach
    void setUp() {
        service = new BudgetService(new BigDecimal("20.00"));
        service.addExpense(new FixedExpense("Music subscription", new BigDecimal("5.00"), MON));
        service.addExpense(new FixedExpense("Internet & mobile", new BigDecimal("8.50"), MON));
        service.addExpense(new VariableExpense("Groceries", new BigDecimal("32.00"), MON));
        service.addExpense(new VariableExpense("Taxi", new BigDecimal("12.00"), TUE));
        service.addExpense(new SavingsExpense("Emergency fund", new BigDecimal("40.00"), MON));
    }

    @Test
    void totalsByType() {
        assertEquals(new BigDecimal("13.50"), service.getTotal(ExpenseType.FIXED));
        assertEquals(new BigDecimal("44.00"), service.getTotal(ExpenseType.VARIABLE));
        assertEquals(new BigDecimal("40.00"), service.getTotal(ExpenseType.SAVINGS));
    }

    @Test
    void savingsAreNotCountedAsSpent() {
        assertEquals(new BigDecimal("57.50"), service.getTotalSpent());
        assertEquals(new BigDecimal("40.00"), service.getTotalSaved());
        assertEquals(new BigDecimal("97.50"), service.getTotalOutflow());
    }

    @Test
    void dailyLimitIsCheckedPerDayNotPerExpense() {
        // two small purchases on the same day: each is under the limit, together over it
        service.addExpense(new VariableExpense("Coffee", new BigDecimal("4.00"), TUE));
        service.addExpense(new VariableExpense("Lunch", new BigDecimal("6.00"), TUE));

        Map<LocalDate, BigDecimal> over = service.getDaysOverLimit();
        assertEquals(2, over.size());
        assertEquals(new BigDecimal("32.00"), over.get(MON));
        assertEquals(new BigDecimal("22.00"), over.get(TUE));
    }

    @Test
    void dayExactlyAtLimitIsNotOver() {
        service.addExpense(new VariableExpense("Lunch", new BigDecimal("8.00"), TUE)); // 12 + 8 = 20
        assertFalse(service.isOverLimit(TUE));
        assertTrue(service.isOverLimit(MON));
    }

    @Test
    void fixedExpensesDoNotCountTowardsDailyLimit() {
        service.addExpense(new FixedExpense("Rent", new BigDecimal("300.00"), TUE));
        assertFalse(service.isOverLimit(TUE));
    }

    @Test
    void expensesAreSortedByDate() {
        service.addExpense(new FixedExpense("Old bill", new BigDecimal("1.00"), MON.minusDays(10)));
        assertEquals("Old bill", service.getExpenses().get(0).getTitle());
        assertEquals("Taxi", service.getExpenses().get(service.getExpenses().size() - 1).getTitle());
    }

    @Test
    void removeExpense() {
        Expense removed = service.removeExpense(service.getExpenses().size() - 1);
        assertEquals("Taxi", removed.getTitle());
        assertEquals(4, service.getExpenses().size());
    }

    @Test
    void expenseListIsReadOnly() {
        assertThrows(UnsupportedOperationException.class, () -> service.getExpenses().clear());
    }

    @Test
    void invalidDailyLimitIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.setDailyLimit(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> service.setDailyLimit(new BigDecimal("-1")));
    }

    @Test
    void emptyServiceHasZeroTotals() {
        BudgetService empty = new BudgetService();
        assertEquals(BigDecimal.ZERO, empty.getTotalOutflow());
        assertTrue(empty.getDaysOverLimit().isEmpty());
    }
}
