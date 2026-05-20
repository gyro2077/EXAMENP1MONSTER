package ec.edu.espe.ticketpremium.controllers;

import ec.edu.espe.ticketpremium.viewmodels.LoginViewModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;
    @FXML private Label lblLoading;

    private LoginViewModel viewModel;

    public void initialize() {
        logger.info("[LOGIN-CTLR] Inicializando LoginController");
        viewModel = new LoginViewModel();
        
        txtUsername.textProperty().bindBidirectional(viewModel.username);
        txtPassword.textProperty().bindBidirectional(viewModel.password);
        lblError.textProperty().bind(viewModel.errorMessage);
        lblLoading.visibleProperty().bind(viewModel.isLoading);
        
        viewModel.loginSuccessful.addListener((obs, oldVal, newVal) -> {
            logger.info("[LOGIN-CTLR] loginSuccessful changed: {} -> {}", oldVal, newVal);
            if (newVal) {
                logger.info("[LOGIN-CTLR] Login exitoso, navegando a main...");
                Platform.runLater(() -> navigateToMain());
            }
        });
        
        logger.info("[LOGIN-CTLR] LoginController inicializado correctamente");
    }

    @FXML
    private void onLoginClick() {
        logger.info("[LOGIN-CTLR] Boton login clickeado");
        logger.info("[LOGIN-CTLR] Username actual: '{}'", viewModel.username.get());
        viewModel.login();
    }
    
    private void navigateToMain() {
        try {
            logger.info("[LOGIN-CTLR] Cargando vista dashboard.fxml...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/dashboard.fxml"));
            Parent root = loader.load();
            DashboardController dashboardController = loader.getController();
            dashboardController.initData(viewModel.username.get(), viewModel.password.get());
            
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Ticket Premium - Dashboard");
            stage.setMinWidth(1100);
            stage.setMinHeight(700);
            stage.setWidth(1400);
            stage.setHeight(900);
            stage.centerOnScreen();
            stage.show();
            logger.info("[LOGIN-CTLR] Navegacion a dashboard completada");
        } catch (Exception e) {
            logger.error("[LOGIN-CTLR] Error al navegar a dashboard: {}", e.getMessage(), e);
        }
    }

    @FXML
    private void onUsernameFocus() {
        logger.info("[LOGIN-CTLR] Username focus gained");
        txtUsername.setStyle("-fx-border-color: #94de2d;");
    }

    @FXML
    private void onUsernameBlur() {
        logger.info("[LOGIN-CTLR] Username focus lost");
        txtUsername.setStyle("");
    }

    @FXML
    private void onPasswordFocus() {
        logger.info("[LOGIN-CTLR] Password focus gained");
        txtPassword.setStyle("-fx-border-color: #94de2d;");
    }

    @FXML
    private void onPasswordBlur() {
        logger.info("[LOGIN-CTLR] Password focus lost");
        txtPassword.setStyle("");
    }
}