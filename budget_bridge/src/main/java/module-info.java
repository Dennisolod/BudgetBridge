module groupid {
    requires javafx.controls;
    requires javafx.fxml;

    opens groupid to javafx.fxml;
    exports groupid;
}
