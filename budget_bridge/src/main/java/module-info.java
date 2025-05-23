module groupid {
    requires javafx.controls;
    requires javafx.fxml;

    opens groupid to javafx.fxml;
    exports groupid;
    requires static lombok;

    requires org.kordamp.ikonli.core;          // utility classes
    requires org.kordamp.ikonli.javafx;        // JavaFX bridge
    requires org.kordamp.ikonli.fontawesome5;
    requires javafx.graphics;  // the icon pack you chose

}
