package budgettracker.ui;

import budgettracker.model.Expense;
import budgettracker.model.ExpenseFactory;
import budgettracker.model.ExpenseType;
import budgettracker.service.BudgetService;
import budgettracker.storage.CsvExpenseRepository;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Interactive text menu. Input and output are passed in,
 * so the menu can be tested without a real console.
 */
public class ConsoleMenu {
    private final BudgetService service;
    private final CsvExpenseRepository repository;
    private final Scanner in;
    private final PrintStream out;
    private boolean unsavedChanges = false;

    public ConsoleMenu(BudgetService service, CsvExpenseRepository repository, Scanner in, PrintStream out) {
        this.service = service;
        this.repository = repository;
        this.in = in;
        this.out = out;
    }

    public void run() {
        out.println("Welcome to Budget Tracker!");
        while (true) {
            printMenu();
            String choice = readLine("Choose an option: ");
            if (choice == null) { // end of input
                save();
                return;
            }
            switch (choice) {
                case "1" -> addExpense();
                case "2" -> out.print(ReportFormatter.formatList(service.getExpenses()));
                case "3" -> out.print(ReportFormatter.formatAnalysis(service));
                case "4" -> deleteExpense();
                case "5" -> changeDailyLimit();
                case "6" -> save();
                case "0" -> {
                    save();
                    out.println("Bye!");
                    return;
                }
                default -> out.println("Unknown option, try again.");
            }
        }
    }

    private void printMenu() {
        out.println();
        out.println("1. Add expense");
        out.println("2. Show all expenses");
        out.println("3. Budget analysis");
        out.println("4. Delete expense");
        out.println("5. Set daily limit (now " + service.getDailyLimit() + " €)");
        out.println("6. Save");
        out.println("0. Save and exit");
    }

    private void addExpense() {
        ExpenseType type = readType();
        if (type == null) return;

        String title = readLine("Title: ");
        if (title == null || title.isBlank()) {
            out.println("Title must not be empty.");
            return;
        }
        BigDecimal amount = readPositiveAmount("Amount (€): ");
        if (amount == null) return;
        LocalDate date = readDate();
        if (date == null) return;

        Expense expense = ExpenseFactory.create(type, title, amount, date);
        service.addExpense(expense);
        unsavedChanges = true;
        out.println("Added: " + expense);

        if (type == ExpenseType.VARIABLE && service.isOverLimit(date)) {
            out.printf("Warning: you exceeded your daily limit on %s (%.2f € of %.2f €)%n",
                    date, service.getVariableSpendingByDay().get(date), service.getDailyLimit());
        }
    }

    private void deleteExpense() {
        List<Expense> expenses = service.getExpenses();
        if (expenses.isEmpty()) {
            out.println("Nothing to delete.");
            return;
        }
        out.print(ReportFormatter.formatList(expenses));
        String input = readLine("Number to delete (empty to cancel): ");
        if (input == null || input.isBlank()) return;
        try {
            int index = Integer.parseInt(input.trim()) - 1;
            Expense removed = service.removeExpense(index);
            unsavedChanges = true;
            out.println("Deleted: " + removed);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            out.println("Invalid number.");
        }
    }

    private void changeDailyLimit() {
        BigDecimal limit = readPositiveAmount("New daily limit (€): ");
        if (limit != null) {
            service.setDailyLimit(limit);
            out.println("Daily limit set to " + limit + " €");
        }
    }

    private void save() {
        if (!unsavedChanges) return;
        try {
            repository.save(service.getExpenses());
            unsavedChanges = false;
            out.println("Saved to " + repository.getFile());
        } catch (IOException e) {
            out.println("Could not save: " + e.getMessage());
        }
    }

    private ExpenseType readType() {
        out.println("Type: 1 = Fixed, 2 = Variable, 3 = Savings");
        String input = readLine("> ");
        if (input == null) return null;
        return switch (input.trim()) {
            case "1" -> ExpenseType.FIXED;
            case "2" -> ExpenseType.VARIABLE;
            case "3" -> ExpenseType.SAVINGS;
            default -> {
                out.println("Unknown type.");
                yield null;
            }
        };
    }

    private BigDecimal readPositiveAmount(String prompt) {
        String input = readLine(prompt);
        if (input == null) return null;
        try {
            BigDecimal value = new BigDecimal(input.trim().replace(',', '.'));
            if (value.signum() <= 0) {
                out.println("Amount must be greater than zero.");
                return null;
            }
            return value;
        } catch (NumberFormatException e) {
            out.println("Not a number: " + input);
            return null;
        }
    }

    private LocalDate readDate() {
        String input = readLine("Date YYYY-MM-DD (empty = today): ");
        if (input == null) return null;
        if (input.isBlank()) return LocalDate.now();
        try {
            return LocalDate.parse(input.trim());
        } catch (DateTimeParseException e) {
            out.println("Invalid date: " + input);
            return null;
        }
    }

    private String readLine(String prompt) {
        out.print(prompt);
        return in.hasNextLine() ? in.nextLine() : null;
    }
}
