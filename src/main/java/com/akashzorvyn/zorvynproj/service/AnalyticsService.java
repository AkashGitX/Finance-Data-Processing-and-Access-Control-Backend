package com.akashzorvyn.zorvynproj.service;

import com.akashzorvyn.zorvynproj.config.ChatClientWrapper;
import com.akashzorvyn.zorvynproj.entity.TransactionType;
import com.akashzorvyn.zorvynproj.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final ChatClientWrapper chatClientWrapper;

    /**
     * Endpoint 1: GET /api/analytics/expense-pattern
     * Fetches all expense data, aggregates category-wise totals and monthly trends,
     * then sends structured data to Spring AI.
     *
     * Prompt: "Analyze the expense data and identify patterns, trends,
     *          anomalies, high spending categories, and monthly growth."
     */
    public Map<String, Object> analyzeExpensePattern() {
        log.info("Analyzing expense patterns using Spring AI");

        List<Object[]> categoryTotals = transactionRepository.findExpenseCategoryTotals();
        List<Object[]> monthlyTotals = transactionRepository.findExpenseMonthlyTotals();

        StringBuilder dataBuilder = new StringBuilder();
        dataBuilder.append("Expense Data Analysis:\n\n");
        dataBuilder.append("Category-wise Expense Totals:\n");
        for (Object[] row : categoryTotals) {
            dataBuilder.append(String.format("  - %s: $%.2f%n", row[0], row[1]));
        }
        dataBuilder.append("\nMonthly Expense Totals:\n");
        for (Object[] row : monthlyTotals) {
            dataBuilder.append(String.format("  - %s: $%.2f%n", row[0], row[1]));
        }

        String prompt = dataBuilder + "\n\nAnalyze the expense data and identify patterns, trends, " +
                "anomalies, high spending categories, and monthly growth.";

        Map<String, Object> analyticsData = new HashMap<>();
        analyticsData.put("categoryTotals", categoryTotals.stream()
                .map(row -> Map.of("category", row[0], "totalAmount", row[1]))
                .toList());
        analyticsData.put("monthlyTotals", monthlyTotals.stream()
                .map(row -> Map.of("month", row[0], "totalAmount", row[1]))
                .toList());
        analyticsData.put("aiInsights", chatClientWrapper.call(prompt));
        analyticsData.put("aiEnabled", chatClientWrapper.isAvailable());
        analyticsData.put("dataSource", "Real-time aggregation from database");

        return analyticsData;
    }

    /**
     * Endpoint 2: GET /api/analytics/income-vs-expense
     * Fetches total income and expenses, sends structured data to Spring AI.
     *
     * Prompt: "Compare income and expenses and provide insights on
     *          financial health, savings trends, and potential risks."
     */
    public Map<String, Object> analyzeIncomeVsExpense() {
        log.info("Analyzing income vs expense using Spring AI");

        BigDecimal totalIncome = transactionRepository.sumByType(TransactionType.INCOME);
        BigDecimal totalExpense = transactionRepository.sumByType(TransactionType.EXPENSE);
        BigDecimal netBalance = totalIncome.subtract(totalExpense);

        double savingsRate = totalIncome.compareTo(BigDecimal.ZERO) > 0
                ? netBalance.divide(totalIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;

        String data = String.format(
                "Financial Overview:\n" +
                "  Total Income:  $%.2f\n" +
                "  Total Expense: $%.2f\n" +
                "  Net Balance:   $%.2f\n" +
                "  Savings Rate:  %.2f%%\n",
                totalIncome, totalExpense, netBalance, savingsRate);

        String prompt = data + "\nCompare income and expenses and provide insights on financial health, " +
                "savings trends, and potential risks.";

        Map<String, Object> result = new HashMap<>();
        result.put("totalIncome", totalIncome);
        result.put("totalExpense", totalExpense);
        result.put("netBalance", netBalance);
        result.put("savingsRate", savingsRate);
        result.put("aiInsights", chatClientWrapper.call(prompt));
        result.put("aiEnabled", chatClientWrapper.isAvailable());
        result.put("dataSource", "Real-time aggregation from database");

        return result;
    }
}
