package budgettracker;

import budgettracker.model.FixedExpense;
import budgettracker.model.SavingsExpense;
import budgettracker.model.VariableExpense;
import budgettracker.service.BudgetService;
import budgettracker.storage.CsvExpenseRepository;
import budgettracker.ui.ConsoleMenu;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Entry point. Run with {@code --demo} to start with sample data.
 */
public class Main {
    private static final Path DATA_FILE = Path.of("data", "expenses.csv");
    private static final Path DEMO_FILE = Path.of("data", "demo-expenses.csv");

    public static void main(String[] args) {
        BudgetService service = new BudgetService();
        boolean demo = Arrays.asList(args).contains("--demo");
        // demo mode writes to its own file so it never overwrites real data
        CsvExpenseRepository repository = new CsvExpenseRepository(demo ? DEMO_FILE : DATA_FILE);

        if (demo) {
            addDemoData(service);
            System.out.println("Demo mode: sample data loaded (changes go to " + DEMO_FILE + ").");
        } else {
            try {
                service.addAll(repository.load());
                System.out.println("Loaded " + service.getExpenses().size() + " expenses from " + DATA_FILE);
            } catch (IOException e) {
                System.out.println("Could not read " + DATA_FILE + ": " + e.getMessage());
                System.out.println("Starting with an empty list.");
            }
        }

        new ConsoleMenu(service, repository, new Scanner(System.in), System.out).run();
    }

    private static void addDemoData(BudgetService service) {
        LocalDate today = LocalDate.now();
        service.addExpense(new FixedExpense("Music subscription", new BigDecimal("5.00"), today));
        service.addExpense(new FixedExpense("Internet & mobile", new BigDecimal("8.50"), today));
        service.addExpense(new VariableExpense("Supermarket groceries", new BigDecimal("32.00"), today));
        service.addExpense(new VariableExpense("Taxi", new BigDecimal("12.00"), today.minusDays(1)));
        service.addExpense(new SavingsExpense("Emergency fund transfer", new BigDecimal("40.00"), today));
    }
}
