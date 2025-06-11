package groupid.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.beans.property.StringProperty;


public class BudgetInfoDAO {

    public static void saveBudgetInfo(int userId, BudgetModel model) {
        String insertSQL = "INSERT INTO budget_info (user_id, type, name, frequency, amount, budget_limit) VALUES (?, ?, ?, ?, ?, ?);";

        try (Connection conn = SQLiteConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            for (MoneyLine income : model.incomes()) {
                pstmt.setInt(1, userId);
                pstmt.setString(2, "income");
                pstmt.setString(3, income.getType());
                pstmt.setString(4, income.getFreq());
                pstmt.setDouble(5, income.getAmount());
                pstmt.setDouble(6, income.getBudgetLimit());
                pstmt.addBatch();
            }

            for (MoneyLine expense : model.expenses()) {
                pstmt.setInt(1, userId);
                pstmt.setString(2, "expense");
                pstmt.setString(3, expense.getType());
                pstmt.setString(4, expense.getFreq());
                pstmt.setDouble(5, expense.getAmount());
                pstmt.setDouble(6, expense.getBudgetLimit());
                pstmt.addBatch();
            }

            pstmt.executeBatch();
            System.out.println("Budget info saved for user ID: " + userId);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void printBudgetInfo(int userId) {
        String query = "SELECT type, name, frequency, amount, budget_limit FROM budget_info WHERE user_id = ?;";

        try (Connection conn = SQLiteConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\nBudget Info for user ID: " + userId);
            while (rs.next()) {
                String type = rs.getString("type");
                String name = rs.getString("name");
                String frequency = rs.getString("frequency");
                double amount = rs.getDouble("amount");
                double limit = rs.getDouble("budget_limit");
                System.out.printf("[%s] %s (%s): $%.2f, Limit: $%.2f\n", type, name, frequency, amount, limit);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void loadBudgetModelFromDB(StringProperty username, BudgetModel model) {
        int userId = UserDAO.getUserIdByName(username);
        if (userId == -1) return;

        model.incomes().clear();
        model.expenses().clear();

        String sql = "SELECT * FROM budget_info WHERE user_id = ?";

        try (Connection conn = SQLiteConnector.connect();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String type = rs.getString("type");           // "income" or "expense"
                String name = rs.getString("name");
                String frequency = rs.getString("frequency");
                double amount = rs.getDouble("amount");
                double budgetLimit = rs.getDouble("budget_limit");

                MoneyLine line = new MoneyLine(name, frequency, amount);
                line.setBudgetLimit(budgetLimit);

                if ("income".equalsIgnoreCase(type)) {
                    model.incomes().add(line);
                } else if ("expense".equalsIgnoreCase(type)) {
                    model.expenses().add(line);
                }
            }

            model.initializeNewMonth();
            model.refreshCurrentMonthFromBase();  // Optional: sync monthly view

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
