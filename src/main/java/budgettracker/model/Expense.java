package budgettracker.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Base class for every expense. Amounts are stored as {@link BigDecimal}
 * rounded to cents, so money calculations stay exact.
 */
public abstract class Expense {
    private final String title;
    private final BigDecimal amount;
    private final LocalDate date;

    protected Expense(String title, BigDecimal amount, LocalDate date) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title must not be empty");
        }
        Objects.requireNonNull(amount, "Amount must not be null");
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        this.title = title.trim();
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.date = Objects.requireNonNull(date, "Date must not be null");
    }

    public String getTitle() {
        return title;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public abstract ExpenseType getType();

    /**
     * Short human-readable comment about this expense, shown in the analysis.
     * Subclasses override it to give type-specific advice.
     */
    public String getHint() {
        return "";
    }

    @Override
    public String toString() {
        return String.format("%s  %-10s %-30s %9.2f €",
                date, "[" + getType().getLabel() + "]", title, amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expense other = (Expense) o;
        return title.equals(other.title) && amount.equals(other.amount) && date.equals(other.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), title, amount, date);
    }
}
