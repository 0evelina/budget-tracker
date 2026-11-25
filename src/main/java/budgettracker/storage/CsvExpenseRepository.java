package budgettracker.storage;

import budgettracker.model.Expense;
import budgettracker.model.ExpenseFactory;
import budgettracker.model.ExpenseType;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves and loads expenses from a CSV file.
 * Format of one line: {@code date;type;amount;title}.
 * The title is the last column, so it may contain semicolons.
 */
public class CsvExpenseRepository {
    private static final String HEADER = "date;type;amount;title";
    private static final String SEPARATOR = ";";

    private final Path file;

    public CsvExpenseRepository(Path file) {
        this.file = file;
    }

    public Path getFile() {
        return file;
    }

    public void save(List<Expense> expenses) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (Expense e : expenses) {
            lines.add(String.join(SEPARATOR,
                    e.getDate().toString(),
                    e.getType().name(),
                    e.getAmount().toPlainString(),
                    e.getTitle()));
        }
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    /**
     * @return expenses from the file, or an empty list if the file does not exist yet
     * @throws IOException if the file cannot be read or contains an invalid line
     */
    public List<Expense> load() throws IOException {
        List<Expense> result = new ArrayList<>();
        if (!Files.exists(file)) {
            return result;
        }
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).strip();
            if (line.isEmpty() || line.equals(HEADER)) {
                continue;
            }
            result.add(parseLine(line, i + 1));
        }
        return result;
    }

    private Expense parseLine(String line, int lineNumber) throws IOException {
        String[] parts = line.split(SEPARATOR, 4);
        if (parts.length != 4) {
            throw new IOException("Line " + lineNumber + ": expected 4 columns but got " + parts.length);
        }
        try {
            LocalDate date = LocalDate.parse(parts[0]);
            ExpenseType type = ExpenseType.valueOf(parts[1]);
            BigDecimal amount = new BigDecimal(parts[2]);
            return ExpenseFactory.create(type, parts[3], amount, date);
        } catch (DateTimeParseException | IllegalArgumentException e) {
            throw new IOException("Line " + lineNumber + ": " + e.getMessage(), e);
        }
    }
}
