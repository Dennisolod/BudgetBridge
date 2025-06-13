package groupid.model;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void initialize() {
        //clearDatabase();
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
        String dropMetaData = "DROP TABLE IF EXISTS meta_data;";
        String createMetaData = """
            CREATE TABLE IF NOT EXISTS meta_data (
                user_id INTEGER PRIMARY KEY,
                gems INTEGER DEFAULT 0,
                points INTEGER DEFAULT 0,
                current_theme_name TEXT,
                FOREIGN KEY(user_id) REFERENCES users(id)
            );
        """;
        String dropBadges = "DROP TABLE IF EXISTS badges;";
        String createBadges = """
            CREATE TABLE IF NOT EXISTS badges (
                user_id INTEGER NOT NULL,
                badge_name TEXT NOT NULL,
                color TEXT NOT NULL,
                icon_literal TEXT NOT NULL,
                cost INTEGER NOT NULL,
                FOREIGN KEY(user_id) REFERENCES users(id)
            );
        """;
        String dropThemes = "DROP TABLE IF EXISTS themes;";
        String createThemes = """
            CREATE TABLE IF NOT EXISTS themes (
                user_id INTEGER NOT NULL,
                theme_name TEXT NOT NULL,
                background_color TEXT NOT NULL,
                cost INTEGER NOT NULL,
                FOREIGN KEY(user_id) REFERENCES users(id)
            );
        """;

        try (Connection conn = SQLiteConnector.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUsers);
            //stmt.execute(dropBudgetInfo); // Use this if you only want to start a fresh budget info table for the user
            // stmt.execute(dropBadges); // Use this if you only want to start a fresh budget info table for the user
            // stmt.execute(dropMetaData); // Use this if you only want to start a fresh budget info table for the user
            // stmt.execute(dropThemes);// Use this if you only want to start a fresh budget info table for the user
            stmt.execute(createBudgetInfo);
            stmt.execute(createMetaData);
            stmt.execute(createBadges);
            stmt.execute(createThemes);
            System.out.println("The database has been created");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clearDatabase() {
        String deleteUsers = "DELETE FROM users;";
        String deleteBudgetInfo = "DELETE FROM budget_info;";
        String deleteMetaData = "DELETE FROM meta_data";
        String deleteBadges = "DELETE FROM badges";
        String deleteThemes = "DELETE FROM themes";

        try (Connection conn = SQLiteConnector.connect();
            Statement stmt = conn.createStatement()) {
            
            stmt.execute(deleteBudgetInfo);  // Clear budget_info first to avoid FK constraint issues
            stmt.execute(deleteMetaData);       // Then clear users meta data
            stmt.execute(deleteBadges);       // Then clear badges
            stmt.execute(deleteThemes);       // Then clear Themes
            stmt.execute(deleteUsers);       // Then clear users
            System.out.println("All data in the database has been cleared.");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
