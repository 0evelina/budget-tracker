package budgettracker.service;

import budgettracker.model.Expense;
import budgettracker.model.ExpenseType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Holds the list of expenses and does all budget calculations.
 */
public class BudgetService {
    public static final BigDecimal DEFAULT_DAILY_LIMIT = new BigDecimal("20.00");

    private final List<Expense> expenses = new ArrayList<>();
    private BigDecimal dailyLimit;

    public BudgetService() {
        this(DEFAULT_DAILY_LIMIT);
    }

    public BudgetService(BigDecimal dailyLimit) {
        setDailyLimit(dailyLimit);
    }

    public void addExpense(Expense expense) {
        expenses.add(Objects.requireNonNull(expense, "Expense must not be null"));
        // keep the list sorted by date, so indexes shown to the user stay valid
        expenses.sort(Comparator.comparing(Expense::getDate));
    }

    public void addAll(Collection<? extends Expense> newExpenses) {
        newExpenses.forEach(this::addExpense);
    }

    /**
     * Removes the expense at the given position of {@link #getExpenses()}.
     *
     * @return the removed expense
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public Expense removeExpense(int index) {
        return expenses.remove(index);
    }

    /**
     * @return all expenses sorted by date (read-only)
     */
    public List<Expense> getExpenses() {
        return Collections.unmodifiableList(expenses);
    }

    public BigDecimal getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(BigDecimal dailyLimit) {
        Objects.requireNonNull(dailyLimit, "Daily limit must not be null");
        if (dailyLimit.signum() <= 0) {
            throw new IllegalArgumentException("Daily limit must be greater than zero");
        }
        this.dailyLimit = dailyLimit;
    }

    public BigDecimal getTotal(ExpenseType type) {
        return sum(expenses.stream().filter(e -> e.getType() == type).toList());
    }

    /** Fixed + variable expenses: money that is really gone. */
    public BigDecimal getTotalSpent() {
        return sum(expenses.stream().filter(e -> e.getType().isSpending()).toList());
    }

    /** Money moved to savings. */
    public BigDecimal getTotalSaved() {
        return getTotal(ExpenseType.SAVINGS);
    }

    /** Everything that left the account: spending + savings. */
    public BigDecimal getTotalOutflow() {
        return sum(expenses);
    }

    /**
     * @return sum of variable expenses for every day, sorted by date
     */
    public Map<LocalDate, BigDecimal> getVariableSpendingByDay() {
        return expenses.stream()
                .filter(e -> e.getType() == ExpenseType.VARIABLE)
                .collect(Collectors.groupingBy(
                        Expense::getDate,
                        TreeMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)));
    }

    /**
     * @return only the days where variable spending is above the daily limit
     */
    public Map<LocalDate, BigDecimal> getDaysOverLimit() {
        Map<LocalDate, BigDecimal> result = new TreeMap<>();
        getVariableSpendingByDay().forEach((day, total) -> {
            if (total.compareTo(dailyLimit) > 0) {
                result.put(day, total);
            }
        });
        return result;
    }

    public boolean isOverLimit(LocalDate day) {
        return getDaysOverLimit().containsKey(day);
    }

    private static BigDecimal sum(List<Expense> list) {
        return list.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
