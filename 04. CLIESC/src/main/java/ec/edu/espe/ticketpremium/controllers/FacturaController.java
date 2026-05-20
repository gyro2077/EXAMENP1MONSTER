package ec.edu.espe.ticketpremium.controllers;

import ec.edu.espe.ticketpremium.models.ComprobanteDTO;
import ec.edu.espe.ticketpremium.viewmodels.MainViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;

public class FacturaController {
    private static final Logger logger = LoggerFactory.getLogger(FacturaController.class);

    @FXML private Label lblCliente;
    @FXML private Label lblCedula;
    @FXML private Label lblFacturaId;
    @FXML private Label lblFecha;
    @FXML private Label lblPartido;
    @FXML private Label lblLocalidad;
    @FXML private Label lblCantidad;
    @FXML private Label lblPrecioUnitario;
    @FXML private Label lblSubtotal;
    @FXML private Label lblIva;
    @FXML private Label lblTotal;
    @FXML private Button btnComprarMas;
    @FXML private Button btnVerReportes;
    @FXML private Button btnVolver;

    private MainViewModel viewModel;
    private DashboardController dashboard;
    private ComprobanteDTO comprobante;

    public void initData(String username, String password, MainViewModel viewModel, ComprobanteDTO comprobante, DashboardController dashboard) {
        this.viewModel = viewModel;
        this.dashboard = dashboard;
        this.comprobante = comprobante;

        logger.info("[FACTURA] Initializing with comprobante - facturaId: {}, cliente: {}",
            comprobante != null ? comprobante.getFacturaId() : "null",
            comprobante != null ? comprobante.getClienteNombre() : "null");

        if (comprobante == null) {
            logger.error("[FACTURA] Comprobante is null!");
            return;
        }

        populateData();
    }

    private void populateData() {
        lblCliente.setText(comprobante.getClienteNombre() != null ? comprobante.getClienteNombre() : "N/A");
        lblCedula.setText(comprobante.getClienteCedula() != null ? comprobante.getClienteCedula() : "N/A");

        if (comprobante.getFacturaId() != null) {
            lblFacturaId.setText(String.format("#%06d", comprobante.getFacturaId()));
        } else {
            lblFacturaId.setText("#N/A");
        }

        if (comprobante.getFechaCompra() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm");
            lblFecha.setText(comprobante.getFechaCompra().format(formatter));
        } else {
            lblFecha.setText("N/A");
        }

        lblPartido.setText(comprobante.getNombrePartido() != null ? comprobante.getNombrePartido() : "N/A");
        lblLocalidad.setText(comprobante.getCodigoLocalidad() != null ? comprobante.getCodigoLocalidad() : "N/A");

        if (comprobante.getCantidad() > 0) {
            lblCantidad.setText(comprobante.getCantidad() + " boleto(s)");
        } else {
            lblCantidad.setText("N/A");
        }

        if (comprobante.getPrecioUnitario() != null) {
            lblPrecioUnitario.setText("$ " + String.format("%.2f", comprobante.getPrecioUnitario()));
        } else {
            lblPrecioUnitario.setText("$ 0.00");
        }

        if (comprobante.getSubtotal() != null) {
            lblSubtotal.setText("$ " + String.format("%.2f", comprobante.getSubtotal()));
        } else {
            lblSubtotal.setText("$ 0.00");
        }

        if (comprobante.getIva() != null) {
            lblIva.setText("$ " + String.format("%.2f", comprobante.getIva()));
        } else {
            lblIva.setText("$ 0.00");
        }

        if (comprobante.getTotal() != null) {
            lblTotal.setText("$ " + String.format("%.2f", comprobante.getTotal()));
        } else {
            lblTotal.setText("$ 0.00");
        }
    }

    @FXML
    private void onVolver() {
        logger.info("[FACTURA] Volver a partidos");
        if (dashboard != null) {
            dashboard.onShowPartidos();
        }
    }

    @FXML
    private void onComprarMas() {
        logger.info("[FACTURA] Comprar mas boletos");
        if (dashboard != null) {
            dashboard.onShowPartidos();
        }
    }

    @FXML
    private void onVerReportes() {
        logger.info("[FACTURA] Ver reportes");
        if (dashboard != null) {
            dashboard.onShowReportes();
        }
    }
}
