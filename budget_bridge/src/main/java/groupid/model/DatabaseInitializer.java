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
        
        String createMetaData = """
            CREATE TABLE IF NOT EXISTS meta_data (
                user_id INTEGER PRIMARY KEY,
                gems INTEGER DEFAULT 0,
                points INTEGER DEFAULT 0,
                current_theme_name TEXT,
                current_profile_icon_name TEXT,
                FOREIGN KEY(user_id) REFERENCES users(id)
            );
        """;
        
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

        String createThemes = """
            CREATE TABLE IF NOT EXISTS themes (
                user_id INTEGER NOT NULL,
                theme_name TEXT NOT NULL,
                background_color TEXT NOT NULL,
                cost INTEGER NOT NULL,
                FOREIGN KEY(user_id) REFERENCES users(id)
            );
        """;

        String createProfileIcons = """
            CREATE TABLE IF NOT EXISTS profile_icons (
                user_id INTEGER NOT NULL,
                icon_name TEXT NOT NULL,
                color TEXT NOT NULL,
                icon_literal TEXT NOT NULL,
                cost INTEGER NOT NULL,
                description TEXT NOT NULL,
                FOREIGN KEY(user_id) REFERENCES users(id)
            );
        """;

        try (Connection conn = SQLiteConnector.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUsers);
            stmt.execute(createBudgetInfo);
            stmt.execute(createMetaData);
            stmt.execute(createBadges);
            stmt.execute(createThemes);
            stmt.execute(createProfileIcons);
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
        String dropUsers = "DROP TABLE IF EXISTS users;";
        String dropThemes = "DROP TABLE IF EXISTS themes;";
        String dropBadges = "DROP TABLE IF EXISTS badges;";
        String dropMetaData = "DROP TABLE IF EXISTS meta_data;";
        String dropBudgetInfo = "DROP TABLE IF EXISTS budget_info;";
        String dropProfileIcons = "DROP TABLE IF EXISTS profile_icons;";


        try (Connection conn = SQLiteConnector.connect();
            Statement stmt = conn.createStatement()) {
            
            stmt.execute(deleteBudgetInfo);  // Clear budget_info first to avoid FK constraint issues
            stmt.execute(deleteMetaData);       // Then clear users meta data
            stmt.execute(deleteBadges);       // Then clear badges
            stmt.execute(deleteThemes);       // Then clear Themes
            stmt.execute(deleteUsers);       // Then clear users
            stmt.execute(dropUsers);        // drops users table
            stmt.execute(dropBudgetInfo);   // drops budget info table
            stmt.execute(dropBadges);       // drops badges table
            stmt.execute(dropMetaData);     // drop metadata table
            stmt.execute(dropThemes);       // drops themes table
            stmt.execute(dropProfileIcons);
            System.out.println("All data in the database has been cleared.");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
