package budgettracker.ui;

import budgettracker.model.ExpenseType;
import budgettracker.service.BudgetService;
import budgettracker.storage.CsvExpenseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleMenuTest {

    @TempDir
    Path tempDir;

    private String run(BudgetService service, CsvExpenseRepository repo, String input) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);
        new ConsoleMenu(service, repo, new Scanner(input), out).run();
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    void addExpenseThenExitSavesToFile() throws IOException {
        BudgetService service = new BudgetService();
        CsvExpenseRepository repo = new CsvExpenseRepository(tempDir.resolve("expenses.csv"));

        String input = String.join("\n",
                "1", "2", "Groceries", "25,50", "2026-11-25", // add variable expense, comma as decimal sign
                "0") + "\n";
        String output = run(service, repo, input);

        assertEquals(1, service.getExpenses().size());
        assertEquals(ExpenseType.VARIABLE, service.getExpenses().get(0).getType());
        assertEquals(new BigDecimal("25.50"), service.getExpenses().get(0).getAmount());
        assertTrue(output.contains("exceeded your daily limit"), output);
        assertEquals(1, repo.load().size());
    }

    @Test
    void invalidInputDoesNotCrash() {
        BudgetService service = new BudgetService();
        CsvExpenseRepository repo = new CsvExpenseRepository(tempDir.resolve("expenses.csv"));

        String input = String.join("\n",
                "9",                       // unknown menu option
                "1", "7",                  // unknown type
                "1", "1", "Phone", "abc",  // amount not a number
                "4",                       // delete from empty list
                "5", "-3",                 // invalid limit
                "0") + "\n";
        String output = run(service, repo, input);

        assertTrue(service.getExpenses().isEmpty());
        assertTrue(output.contains("Unknown option"));
        assertTrue(output.contains("Unknown type"));
        assertTrue(output.contains("Not a number"));
        assertTrue(output.contains("Nothing to delete"));
        assertTrue(output.contains("greater than zero"));
    }

    @Test
    void changeDailyLimit() {
        BudgetService service = new BudgetService();
        CsvExpenseRepository repo = new CsvExpenseRepository(tempDir.resolve("expenses.csv"));
        run(service, repo, "5\n35\n0\n");
        assertEquals(new BigDecimal("35"), service.getDailyLimit());
    }
}
