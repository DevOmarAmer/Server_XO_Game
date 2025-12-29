module com.mycompany.server_xo_game {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires derbyclient;

    opens com.mycompany.server_xo_game to javafx.fxml;
    exports com.mycompany.server_xo_game;
}
