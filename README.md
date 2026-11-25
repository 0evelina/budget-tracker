# Budget Tracker

A console application in Java for tracking personal expenses. It splits money into
**fixed**, **variable** and **savings** expenses, checks variable spending against a
daily limit and keeps everything in a CSV file between runs.

## Features

- Add, list and delete expenses through a text menu
- Three expense types: Fixed (subscriptions, bills), Variable (groceries, taxi), Savings
- Budget analysis: totals per type, total spent, total saved, savings rate
- Daily limit check: warns when variable expenses **for one day** go over the limit
- Data is saved to `data/expenses.csv` and loaded on the next start
  (demo mode uses a separate `data/demo-expenses.csv`)
- Money is calculated with `BigDecimal`, so there are no rounding errors
- Input validation: wrong numbers, dates or menu options don't crash the program
- Unit tests with JUnit 5

## Example

```
=== BUDGET ANALYSIS ===

2026-11-24  [Variable] Taxi                               12.00 €   (counts towards the daily limit)
2026-11-25  [Fixed]    Music subscription                  5.00 €   (recurring payment - check if you still need it)
2026-11-25  [Fixed]    Internet & mobile                   8.50 €   (recurring payment - check if you still need it)
2026-11-25  [Variable] Supermarket groceries              32.00 €   (counts towards the daily limit)
2026-11-25  [Savings]  Emergency fund transfer            40.00 €   (well done, money put aside)

------------------------------------------------------------
Fixed:                      13.50 €
Variable:                   44.00 €
Savings:                    40.00 €
------------------------------------------------------------
Total spent:                57.50 €
Total saved:                40.00 €
Total outflow:              97.50 €
Savings rate:                 41.0 %

Daily limit for variable expenses: 20.00 €
You exceeded your daily limit on:
  2026-11-25: 32.00 € (+12.00 €)
```

## Project structure

```
src/main/java/budgettracker/
├── Main.java                      entry point
├── model/
│   ├── Expense.java               abstract base class
│   ├── FixedExpense.java
│   ├── VariableExpense.java
│   ├── SavingsExpense.java
│   ├── ExpenseType.java           enum of categories
│   └── ExpenseFactory.java        creates the right subclass by type
├── service/
│   └── BudgetService.java         totals, daily limit logic
├── storage/
│   └── CsvExpenseRepository.java  save / load CSV
└── ui/
    ├── ConsoleMenu.java           interactive menu
    └── ReportFormatter.java       text output for lists and analysis
src/test/java/budgettracker/       JUnit 5 tests
```

OOP concepts used: inheritance and an abstract class (`Expense`), polymorphism
(`getType()`, `getHint()`), encapsulation (private fields, read-only list),
enum, factory method, separation into layers (model / service / storage / ui).

## Requirements

- Java 17 or newer
- Maven 3.8+ (IntelliJ IDEA has Maven built in)

## How to run

**In IntelliJ IDEA:** open the project folder, wait until Maven loads, then run
`Main` (green ▶ next to `main`). To start with sample data, add `--demo` in
*Run → Edit Configurations → Program arguments*.

**From the terminal:**

```bash
mvn package
java -jar target/budget-tracker-1.0.0.jar          # normal mode
java -jar target/budget-tracker-1.0.0.jar --demo   # with sample data
```

## Running the tests

```bash
mvn test
```

or right-click `src/test/java` in IntelliJ → *Run 'All Tests'*.

## CSV format

```
date;type;amount;title
2026-11-25;FIXED;5.00;Music subscription
2026-11-25;VARIABLE;32.00;Supermarket groceries
```

## Ideas for the future

- Monthly reports and filtering by month
- Custom categories (food, transport, entertainment)
- Monthly income and remaining budget
- Export analysis to a file
