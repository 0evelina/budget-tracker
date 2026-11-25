package budgettracker.storage;

import budgettracker.model.Expense;
import budgettracker.model.FixedExpense;
import budgettracker.model.SavingsExpense;
import budgettracker.model.VariableExpense;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvExpenseRepositoryTest {
    private static final LocalDate DAY = LocalDate.of(2026, 11, 25);

    @TempDir
    Path tempDir;

    @Test
    void saveAndLoadReturnsSameExpenses() throws IOException {
        CsvExpenseRepository repo = new CsvExpenseRepository(tempDir.resolve("expenses.csv"));
        List<Expense> original = List.of(
                new FixedExpense("Music subscription", new BigDecimal("5.00"), DAY),
                new VariableExpense("Taxi; airport", new BigDecimal("12.40"), DAY),
                new SavingsExpense("Emergency fund", new BigDecimal("40"), DAY.minusDays(3)));

        repo.save(original);
        List<Expense> loaded = repo.load();

        assertEquals(original, loaded);
        assertEquals("Taxi; airport", loaded.get(1).getTitle());
    }

    @Test
    void missingFileGivesEmptyList() throws IOException {
        CsvExpenseRepository repo = new CsvExpenseRepository(tempDir.resolve("nope.csv"));
        assertTrue(repo.load().isEmpty());
    }

    @Test
    void saveCreatesMissingFolders() throws IOException {
        Path file = tempDir.resolve("data/sub/expenses.csv");
        new CsvExpenseRepository(file).save(List.of());
        assertTrue(Files.exists(file));
    }

    @Test
    void invalidLineGivesErrorWithLineNumber() throws IOException {
        Path file = tempDir.resolve("bad.csv");
        Files.writeString(file, "date;type;amount;title\n2026-11-25;FIXED;abc;Phone\n");

        IOException e = assertThrows(IOException.class, () -> new CsvExpenseRepository(file).load());
        assertTrue(e.getMessage().startsWith("Line 2"), e.getMessage());
    }

    @Test
    void unknownTypeIsRejected() throws IOException {
        Path file = tempDir.resolve("bad.csv");
        Files.writeString(file, "2026-11-25;FOOD;5.00;Pizza\n");
        assertThrows(IOException.class, () -> new CsvExpenseRepository(file).load());
    }
}
