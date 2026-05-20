package ec.edu.espe.ticketpremium.controllers;

import ec.edu.espe.ticketpremium.models.ComprobanteDTO;
import ec.edu.espe.ticketpremium.viewmodels.MainViewModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DashboardController {
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @FXML private Button btnPartidos;
    @FXML private Button btnReportes;
    @FXML private Label lblUser;
    @FXML private StackPane contentArea;

    private String username;
    private String password;
    private MainViewModel viewModel;

    public void initData(String username, String password) {
        this.username = username;
        this.password = password;
        this.viewModel = new MainViewModel(username, password);
        lblUser.setText(username);
        logger.info("[DASHBOARD] Dashboard initialized for user: {}", username);
        onShowPartidos();
    }

    @FXML
    void onShowPartidos() {
        logger.info("[DASHBOARD] Loading partidos view");
        setActiveButton(btnPartidos);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/partidos.fxml"));
            Parent view = loader.load();
            PartidosController controller = loader.getController();
            controller.initData(username, password, viewModel, this);
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (Exception e) {
            logger.error("[DASHBOARD] Error loading partidos view: {}", e.getMessage(), e);
        }
    }

    @FXML
    void onShowReportes() {
        logger.info("[DASHBOARD] Loading reportes view");
        setActiveButton(btnReportes);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/reportes.fxml"));
            Parent view = loader.load();
            ReportesController controller = loader.getController();
            controller.initData(username, password, viewModel);
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (Exception e) {
            logger.error("[DASHBOARD] Error loading reportes view: {}", e.getMessage(), e);
        }
    }

    void showFactura(ComprobanteDTO comp) {
        logger.info("[DASHBOARD] Loading factura view - facturaId: {}", comp.getFacturaId());
        setActiveButton(null);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/factura.fxml"));
            Parent view = loader.load();
            FacturaController controller = loader.getController();
            controller.initData(username, password, viewModel, comp, this);
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (Exception e) {
            logger.error("[DASHBOARD] Error loading factura view: {}", e.getMessage(), e);
        }
    }

    @FXML
    private void onLogout() {
        logger.info("[DASHBOARD] Logging out");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) lblUser.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Ticket Premium - Login");
            stage.setMinWidth(900);
            stage.setMinHeight(600);
            stage.setWidth(1100);
            stage.setHeight(700);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            logger.error("[DASHBOARD] Error during logout: {}", e.getMessage(), e);
        }
    }

    private void setActiveButton(Button active) {
        btnPartidos.getStyleClass().remove("active");
        btnReportes.getStyleClass().remove("active");
        if (active != null) {
            active.getStyleClass().add("active");
        }
    }
}
