package budgettracker.ui;

import budgettracker.model.Expense;
import budgettracker.model.ExpenseType;
import budgettracker.service.BudgetService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Builds text for the console: expense list and budget analysis.
 */
public final class ReportFormatter {
    private static final String LINE = "-".repeat(60);

    private ReportFormatter() {
    }

    public static String formatList(List<Expense> expenses) {
        if (expenses.isEmpty()) {
            return "No expenses yet.\n";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < expenses.size(); i++) {
            sb.append(String.format("%3d. %s%n", i + 1, expenses.get(i)));
        }
        return sb.toString();
    }

    public static String formatAnalysis(BudgetService service) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== BUDGET ANALYSIS ===\n\n");

        List<Expense> expenses = service.getExpenses();
        if (expenses.isEmpty()) {
            return sb.append("No expenses yet.\n").toString();
        }

        for (Expense e : expenses) {
            sb.append(e);
            if (!e.getHint().isEmpty()) {
                sb.append("   (").append(e.getHint()).append(")");
            }
            sb.append('\n');
        }

        sb.append('\n').append(LINE).append('\n');
        for (ExpenseType type : ExpenseType.values()) {
            sb.append(String.format("%-22s %10.2f €%n", type.getLabel() + ":", service.getTotal(type)));
        }
        sb.append(LINE).append('\n');
        sb.append(String.format("%-22s %10.2f €%n", "Total spent:", service.getTotalSpent()));
        sb.append(String.format("%-22s %10.2f €%n", "Total saved:", service.getTotalSaved()));
        sb.append(String.format("%-22s %10.2f €%n", "Total outflow:", service.getTotalOutflow()));

        BigDecimal outflow = service.getTotalOutflow();
        if (outflow.signum() > 0) {
            BigDecimal percent = service.getTotalSaved()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(outflow, 1, java.math.RoundingMode.HALF_UP);
            sb.append(String.format("%-22s %10s %%%n", "Savings rate:", percent));
        }

        sb.append('\n').append(String.format("Daily limit for variable expenses: %.2f €%n", service.getDailyLimit()));
        Map<LocalDate, BigDecimal> overLimit = service.getDaysOverLimit();
        if (overLimit.isEmpty()) {
            sb.append("You stayed within the daily limit every day.\n");
        } else {
            sb.append("You exceeded your daily limit on:\n");
            overLimit.forEach((day, total) -> sb.append(String.format(
                    "  %s: %.2f € (+%.2f €)%n", day, total, total.subtract(service.getDailyLimit()))));
        }
        return sb.toString();
    }
}
