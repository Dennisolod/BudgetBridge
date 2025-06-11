package groupid.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javafx.beans.property.StringProperty;

public class UserDAO {

    public static void addUser(StringProperty name) {
        if (!userExists(name)) {
            String sql = "INSERT INTO users(name) VALUES (?)";

            try (Connection conn = SQLiteConnector.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name.get());
                pstmt.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("User already exists: " + name.get());
        }
    }

    public static void removeUser(StringProperty name) {
        String sql = "DELETE FROM users WHERE name = ?";

        try (Connection conn = SQLiteConnector.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name.get());
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("User removed: " + name.get());
            } else {
                System.out.println("No user found with name: " + name.get());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clearAllUsers() {
    String sql = "DELETE FROM users";

    try (Connection conn = SQLiteConnector.connect();
         Statement stmt = conn.createStatement()) {

        int affectedRows = stmt.executeUpdate(sql);
        System.out.println("All users deleted. Rows affected: " + affectedRows);

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    public static boolean userExists(StringProperty name) {
        String sql = "SELECT COUNT(*) FROM users WHERE name = ?";
        try (Connection conn = SQLiteConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name.get());
                ResultSet rs = pstmt.executeQuery();
                return rs.getInt(1) > 0;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
    }

    public static Integer getUserIdByName(StringProperty name) {
        String sql = "SELECT id FROM users WHERE name = ?";

        try (Connection conn = SQLiteConnector.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name.get());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null; // return null if user not found or on error
    }

    public static void listUsers() {
        String sql = "SELECT * FROM users";

        try (Connection conn = SQLiteConnector.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("Current users in DB:");

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");

                System.out.printf("ID: %d | Name: %s%n", id, name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
