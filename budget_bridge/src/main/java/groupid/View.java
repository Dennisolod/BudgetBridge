package groupid;

public enum View {
    PRIMARY    ("primary"),
    SECONDARY  ("secondary"),
    LEADERBOARD("leaderboard"),
    STORE      ("store");

    public final String fxml;
    View(String fxml) { this.fxml = fxml; }
}
