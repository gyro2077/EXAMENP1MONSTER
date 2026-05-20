package ec.edu.espe.ticketpremium.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main extends Application {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Override
    public void start(Stage stage) {
        try {
            logger.info("Starting Ticket Premium Desktop Application");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/main.css").toURI().toString());

            stage.setTitle("Ticket Premium - Login");
            stage.setScene(scene);
            stage.setMinWidth(900);
            stage.setMinHeight(600);
            stage.setWidth(1100);
            stage.setHeight(700);
            stage.centerOnScreen();
            stage.show();

            logger.info("Application started successfully");

        } catch (Exception e) {
            logger.error("Failed to start application", e);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}