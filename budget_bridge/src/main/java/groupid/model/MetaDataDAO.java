package groupid.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.kordamp.ikonli.javafx.FontIcon;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class MetaDataDAO {
    
        public static void saveMetaData(int userId, BudgetModel model) {
        String sql = """
            INSERT INTO meta_data(user_id, gems, points, current_theme_name)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(user_id) DO UPDATE SET
                gems = excluded.gems,
                points = excluded.points,
                current_theme_name = excluded.current_theme_name;
        """;

        try (Connection conn = SQLiteConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, model.getGems().get());
            pstmt.setInt(3, model.pointsProperty().get());
            pstmt.setString(4, model.getCurrentTheme() != null ? model.getCurrentTheme().getName() : null);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveOwnedBadges(int userId, List<BadgeLine> badges) {
        String deleteSql = "DELETE FROM badges WHERE user_id = ?";
        String insertSql = "INSERT INTO badges(user_id, badge_name, color, icon_literal, cost) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = SQLiteConnector.connect()) {
            conn.setAutoCommit(false);

            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
                 PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

                deleteStmt.setInt(1, userId);
                deleteStmt.executeUpdate();

                for (BadgeLine badge : badges) {
                    insertStmt.setInt(1, userId);
                    insertStmt.setString(2, badge.getName());
                    insertStmt.setString(3, toHexPaint(badge.getColor()));
                    insertStmt.setString(4, badge.getIconLiteral());
                    insertStmt.setInt(5, badge.getCost());
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveOwnedThemes(int userId, List<ThemeLine> themes) {
        String deleteSql = "DELETE FROM themes WHERE user_id = ?";
        String insertSql = "INSERT INTO themes(user_id, theme_name, background_color, cost) VALUES (?, ?, ?, ?)";

        try (Connection conn = SQLiteConnector.connect()) {
            conn.setAutoCommit(false);

            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
                 PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

                deleteStmt.setInt(1, userId);
                deleteStmt.executeUpdate();

                for (ThemeLine theme : themes) {
                    insertStmt.setInt(1, userId);
                    insertStmt.setString(2, theme.getName());
                    insertStmt.setString(3, toHexColor(theme.getBackgroundColor()));
                    insertStmt.setInt(4, theme.getCost());
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadMetaData(int userId, BudgetModel model) {
        String metaSql = "SELECT gems, points, current_theme_name FROM meta_data WHERE user_id = ?";
        String badgeSql = "SELECT badge_name, icon_literal, color, cost FROM badges WHERE user_id = ?";
        String themeSql = "SELECT theme_name, background_color, cost FROM themes WHERE user_id = ?";

        try (Connection conn = SQLiteConnector.connect()) {
            try (PreparedStatement metaStmt = conn.prepareStatement(metaSql)) {
                metaStmt.setInt(1, userId);
                ResultSet rs = metaStmt.executeQuery();

                if (rs.next()) {
                    model.setGemsStart(rs.getInt("gems"));
                    model.setPointsStart(rs.getInt("points"));

                    String themeName = rs.getString("current_theme_name");
                    if (themeName != null) {
                        for (ThemeLine t : model.getOwnedThemes()) {
                            if (t.getName().equals(themeName)) {
                                model.applyThemeStart(t);
                                break;
                            }
                        }
                    }
                }
            }

            try (PreparedStatement badgeStmt = conn.prepareStatement(badgeSql)) {
                badgeStmt.setInt(1, userId);
                ResultSet rs = badgeStmt.executeQuery();
                while (rs.next()) {
                    String name = rs.getString("badge_name");
                    Paint color;
                    try {
                        color = Color.web(rs.getString("color"));
                    } catch (IllegalArgumentException e) {
                        color = Color.BLACK;
                    }
                    String icon = rs.getString("icon_literal");
                    int cost = rs.getInt("cost");
                    model.unlockBadgeStart(new BadgeLine(name, icon, color, cost));
                }
            }

            try (PreparedStatement themeStmt = conn.prepareStatement(themeSql)) {
                themeStmt.setInt(1, userId);
                ResultSet rs = themeStmt.executeQuery();
                while (rs.next()) {
                    String name = rs.getString("theme_name");
                    Color color;
                    try {
                        color = Color.web(rs.getString("background_color"));
                    } catch (IllegalArgumentException e) {
                        color = Color.BLACK;
                    }
                    int cost = rs.getInt("cost");
                    model.unlockThemeStart(new ThemeLine(name, color, cost));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getGems(int userId) {
        String sql = "SELECT gems FROM meta_data WHERE user_id = ?";

        try (Connection conn = SQLiteConnector.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("gems");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 10000; // default if user not found or error occurs
    }

    public static int getPoints(int userId) {
        String sql = "SELECT points FROM meta_data WHERE user_id = ?";

        try (Connection conn = SQLiteConnector.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("points");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0; // default if user not found or error occurs
    }

    // Converts a Color to a hex string that Color.web() can handle
    private static String toHexColor(Color color) {
        return String.format("#%02x%02x%02x",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255)
        );
    }
    // I made another one because for whatever reason badge line saves color as paint instead of color
    private static String toHexPaint(Paint paint) {
        if (paint instanceof Color) {
            Color color = (Color) paint;
            return String.format("#%02x%02x%02x",
                    (int)(color.getRed() * 255),
                    (int)(color.getGreen() * 255),
                    (int)(color.getBlue() * 255));
        }
        return "#000000"; // fallback or throw
    }

    public static void populateLeaderboard(BudgetModel model) {
        String userSQL = "SELECT * FROM users";
        //String badgeSQL = "SELECT * FROM badges ORDER BY cost DESC LIMIT 3 WHERE user_id = ?";

        try (Connection conn = SQLiteConnector.connect(); 
             PreparedStatement pstmt = conn.prepareStatement(userSQL)) {
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                StringProperty name = new SimpleStringProperty(rs.getString("name"));
                model.addUserToLeaderboard(rs.getString("name"), getPoints(UserDAO.getUserIdByName(name)));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void populateLeaderboardBadges(String name, BudgetModel model) {
        String badgeSQL = "SELECT * FROM badges WHERE user_id = ? ORDER BY cost DESC LIMIT 3 ";

        try (Connection conn = SQLiteConnector.connect(); 
            PreparedStatement pstmt = conn.prepareStatement(badgeSQL)) {

            StringProperty user = new SimpleStringProperty(name);
            pstmt.setInt(1, UserDAO.getUserIdByName(user));
            ResultSet rs = pstmt.executeQuery();

            List<BadgeLine> topBadges = new ArrayList<>();

            while (rs.next()) {
                BadgeLine badge = new BadgeLine(rs.getString("badge_name"),
                rs.getString("icon_literal"), 
                Color.web(rs.getString("color")),
                rs.getInt("cost"));
                topBadges.add(badge);
            }

            model.setTop3Badges(topBadges);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

