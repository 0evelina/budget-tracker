package budgettracker.model;

/**
 * Category of an expense.
 */
public enum ExpenseType {
    FIXED("Fixed", true),
    VARIABLE("Variable", true),
    SAVINGS("Savings", false);

    private final String label;
    private final boolean spending;

    ExpenseType(String label, boolean spending) {
        this.label = label;
        this.spending = spending;
    }

    public String getLabel() {
        return label;
    }

    /**
     * @return true if money of this type is actually spent (not put aside as savings)
     */
    public boolean isSpending() {
        return spending;
    }
}
