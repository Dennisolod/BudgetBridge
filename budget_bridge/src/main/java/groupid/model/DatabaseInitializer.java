package groupid.model;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void initialize() {
        String createUsers = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL
            );
        """;
        String dropBudgetInfo = "DROP TABLE IF EXISTS budget_info;";
        String createBudgetInfo = """
            CREATE TABLE IF NOT EXISTS budget_info (
                user_id INTEGER NOT NULL,
                type TEXT NOT NULL,           -- "income" or "expense"
                name TEXT NOT NULL,           -- e.g. "Rent", "Car Payment"
                frequency TEXT NOT NULL,      -- e.g. "Monthly"
                amount REAL NOT NULL,
                budget_limit REAL,
                FOREIGN KEY(user_id) REFERENCES users(id)
            );
        """;

        try (Connection conn = SQLiteConnector.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUsers);
            //stmt.execute(dropBudgetInfo);
            stmt.execute(createBudgetInfo);
            System.out.println("The database has been created");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
