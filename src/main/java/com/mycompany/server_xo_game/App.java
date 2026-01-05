package com.mycompany.server_xo_game;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("ServerPage"), 640, 480);
        stage.setScene(scene);

        // Ensure the application and server thread stop when the window is closed
        stage.setOnCloseRequest(event -> System.exit(0));

        stage.show();

        // Start the Socket Server in a separate thread so it doesn't block the UI
        new Thread(() -> Server.main(new String[]{})).start();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}