package com.torneo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Clase principal de JavaFX. Carga la vista FXML (que a su vez enlaza el CSS).
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                Objects.requireNonNull(Main.class.getResource("/com/torneo/view/torneo.fxml"),
                        "No se encontró torneo.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 980, 640);

        stage.setTitle("⛏ Torneo Minecraft - Inscripciones");
        stage.setScene(scene);
        stage.setMinWidth(860);
        stage.setMinHeight(560);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
