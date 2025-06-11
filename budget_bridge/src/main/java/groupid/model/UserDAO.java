package groupid.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javafx.beans.property.StringProperty;

public class UserDAO {

    public static void addUser(StringProperty name) {
        String sql = "INSERT INTO users(name) VALUES (?)";

        try (Connection conn = SQLiteConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name.get());
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
