package ec.edu.espe.ticketpremium.controllers;

import ec.edu.espe.ticketpremium.models.PartidoDTO;
import ec.edu.espe.ticketpremium.models.ReporteDTO;
import ec.edu.espe.ticketpremium.viewmodels.MainViewModel;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class ReportesController {
    private static final Logger logger = LoggerFactory.getLogger(ReportesController.class);

    @FXML private ComboBox<String> cmbPartidos;
    @FXML private Button btnGenerar;
    @FXML private TableView<ReporteDTO> tablaReportes;
    @FXML private TableColumn<ReporteDTO, String> colLocalidad;
    @FXML private TableColumn<ReporteDTO, Integer> colVendidos;
    @FXML private TableColumn<ReporteDTO, BigDecimal> colPrecio;
    @FXML private TableColumn<ReporteDTO, BigDecimal> colTotal;
    @FXML private Label lblReportMatch;
    @FXML private Label lblReportTotal;
    @FXML private Label lblLoading;
    @FXML private Label lblStatus;

    private MainViewModel viewModel;
    private String username;
    private String password;

    public void initData(String username, String password, MainViewModel viewModel) {
        this.username = username;
        this.password = password;
        this.viewModel = viewModel;

        logger.info("[REPORTES] initData called - user: {}", username);

        colLocalidad.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().getCodigoLocalidad()));
        colVendidos.setCellValueFactory(data ->
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getCantidadTotalVendida()));
        colPrecio.setCellValueFactory(data ->
            new javafx.beans.property.SimpleObjectProperty<>(calcPrecioUnitario(data.getValue())));
        colTotal.setCellValueFactory(data ->
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getTotalRecaudado()));

        colLocalidad.setCellFactory(col -> new TableCell<ReporteDTO, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setTextFill(empty || item == null ? null : javafx.scene.paint.Color.WHITE);
            }
        });
        colVendidos.setCellFactory(col -> new TableCell<ReporteDTO, Integer>() {
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.valueOf(item));
                setTextFill(empty || item == null ? null : javafx.scene.paint.Color.web("#CBD5E1"));
            }
        });
        colPrecio.setCellFactory(col -> new TableCell<ReporteDTO, BigDecimal>() {
            @Override protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : "$" + String.format("%.2f", item));
                setTextFill(empty || item == null ? null : javafx.scene.paint.Color.web("#94A3B8"));
            }
        });
        colTotal.setCellFactory(col -> new TableCell<ReporteDTO, BigDecimal>() {
            @Override protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : "$" + String.format("%.2f", item));
                setTextFill(empty || item == null ? null : javafx.scene.paint.Color.web("#94de2d"));
            }
        });

        viewModel.partidos.addListener((javafx.collections.ListChangeListener<PartidoDTO>) change -> {
            logger.info("[REPORTES] Partidos list changed, size: {}", viewModel.partidos.size());
            fillCombo();
        });

        if (!viewModel.partidos.isEmpty()) {
            logger.info("[REPORTES] Partidos ya cargados: {}", viewModel.partidos.size());
            fillCombo();
        } else {
            logger.info("[REPORTES] Partidos no cargados, solicitando carga...");
            viewModel.loadPartidos();
        }
    }

    private void fillCombo() {
        logger.info("[REPORTES] fillCombo called");
        cmbPartidos.getItems().clear();
        for (PartidoDTO p : viewModel.partidos) {
            String display = p.getNombrePartido() + " (Cod: " + p.getCodigo() + ")";
            cmbPartidos.getItems().add(display);
            logger.info("[REPORTES] Added to combo: {}", display);
        }
        if (!cmbPartidos.getItems().isEmpty()) {
            cmbPartidos.getSelectionModel().select(0);
            logger.info("[REPORTES] Selected first item: {}", cmbPartidos.getValue());
        }
    }

    @FXML
    private void onGenerarReporte() {
        logger.info("[REPORTES] ========== onGenerarReporte CLICKED ==========");
        String selected = cmbPartidos.getValue();
        logger.info("[REPORTES] Selected combo value: {}", selected);

        if (selected == null || selected.isEmpty()) {
            lblStatus.setText("Seleccione un partido primero");
            return;
        }

        int codigo = extractCodigo(selected);
        logger.info("[REPORTES] Extracted codigo: {}", codigo);

        lblReportMatch.setText(selected);
        lblReportTotal.setText("$0.00");
        tablaReportes.getItems().clear();
        lblStatus.setText("");

        lblLoading.setVisible(true);
        lblLoading.setManaged(true);

        Task<java.util.List<ReporteDTO>> task = new Task<>() {
            @Override
            protected java.util.List<ReporteDTO> call() {
                logger.info("[REPORTES] Calling SOAP generarReporteVentas({})", codigo);
                return viewModel.getSoapClient().generarReporteVentas(codigo);
            }
        };

        task.setOnSucceeded(e -> {
            lblLoading.setVisible(false);
            lblLoading.setManaged(false);

            java.util.List<ReporteDTO> reportes = task.getValue();
            logger.info("[REPORTES] SOAP returned {} rows", reportes != null ? reportes.size() : "null");

            if (reportes != null && !reportes.isEmpty()) {
                tablaReportes.getItems().setAll(reportes);
                BigDecimal totalGeneral = BigDecimal.ZERO;
                for (ReporteDTO r : reportes) {
                    logger.info("[REPORTES] Row: {} | vendidos: {} | total: {}",
                        r.getCodigoLocalidad(), r.getCantidadTotalVendida(), r.getTotalRecaudado());
                    if (r.getTotalRecaudado() != null) {
                        totalGeneral = totalGeneral.add(r.getTotalRecaudado());
                    }
                }
                lblReportTotal.setText("$" + String.format("%.2f", totalGeneral));
                lblStatus.setText("");
            } else {
                lblStatus.setText("No hay datos de ventas para este partido");
            }
        });

        task.setOnFailed(e -> {
            lblLoading.setVisible(false);
            lblLoading.setManaged(false);
            logger.error("[REPORTES] Task failed: {}", task.getException().getMessage());
            lblStatus.setText("Error: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    private int extractCodigo(String display) {
        try {
            int start = display.indexOf("(Cod: ") + 6;
            int end = display.indexOf(")");
            return Integer.parseInt(display.substring(start, end));
        } catch (Exception e) {
            return 1;
        }
    }

    private BigDecimal calcPrecioUnitario(ReporteDTO r) {
        if (r.getTotalRecaudado() == null || r.getCantidadTotalVendida() == null || r.getCantidadTotalVendida() == 0) {
            return BigDecimal.ZERO;
        }
        return r.getTotalRecaudado().divide(new BigDecimal(r.getCantidadTotalVendida()), 2, java.math.RoundingMode.HALF_UP);
    }
}
