package ec.edu.espe.ticketpremium.controllers;

import ec.edu.espe.ticketpremium.models.*;
import ec.edu.espe.ticketpremium.viewmodels.MainViewModel;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class PartidosController {
    private static final Logger logger = LoggerFactory.getLogger(PartidosController.class);

    @FXML private FlowPane partidosContainer;
    @FXML private VBox localidadesContainer;
    @FXML private VBox orderDetails;
    @FXML private Label lblMatchSelected;
    @FXML private Label lblSubtotal;
    @FXML private Label lblIva;
    @FXML private Label lblTotal;
    @FXML private Label lblStatus;
    @FXML private Label lblLoading;
    @FXML private Label lblQuantity;
    @FXML private Button btnConfirmPurchase;
    @FXML private Button btnMinus;
    @FXML private Button btnPlus;
    @FXML private HBox purchaseSuccess;
    @FXML private HBox quantitySelector;

    private MainViewModel viewModel;
    private DashboardController dashboard;
    private String username;
    private String password;
    private PartidoDTO selectedPartido;
    private LocalidadDTO selectedLocalidad;
    private int selectedQty = 1;
    private volatile boolean isProcessing = false;

    public void initData(String username, String password, MainViewModel viewModel, DashboardController dashboard) {
        this.username = username;
        this.password = password;
        this.viewModel = viewModel;
        this.dashboard = dashboard;

        lblStatus.textProperty().bind(viewModel.statusMessage);
        lblLoading.visibleProperty().bind(viewModel.isLoading);
        lblLoading.managedProperty().bind(viewModel.isLoading);

        viewModel.loadPartidos();

        viewModel.partidos.addListener((javafx.collections.ListChangeListener<PartidoDTO>) change -> {
            updatePartidosList();
        });

        viewModel.localidades.addListener((javafx.collections.ListChangeListener<LocalidadDTO>) change -> {
            updateLocalidadesList();
        });
    }

    private void updatePartidosList() {
        partidosContainer.getChildren().clear();

        for (PartidoDTO partido : viewModel.partidos) {
            VBox card = createMatchCard(partido);
            partidosContainer.getChildren().add(card);
        }
    }

    private VBox createMatchCard(PartidoDTO partido) {
        VBox card = new VBox();
        card.getStyleClass().add("match-card");
        card.setSpacing(16);

        HBox header = new HBox();
        header.setSpacing(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label tag = new Label("MATCH");
        tag.getStyleClass().add("match-tag");

        String timeStr = partido.getFecha() != null ?
            partido.getFecha().format(DateTimeFormatter.ofPattern("HH:mm")) : "TBD";
        Label time = new Label(timeStr);
        time.getStyleClass().add("match-time");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        header.getChildren().addAll(tag, spacer, time);

        HBox teams = new HBox();
        teams.setSpacing(16);
        teams.setAlignment(javafx.geometry.Pos.CENTER);

        Label local = new Label(partido.getEquipoLocal());
        local.getStyleClass().add("team-name");

        Label vs = new Label("VS");
        vs.getStyleClass().add("vs-text");

        Label visita = new Label(partido.getEquipoVisita());
        visita.getStyleClass().add("team-name");

        teams.getChildren().addAll(local, vs, visita);

        HBox info = new HBox();
        info.setSpacing(20);
        info.setAlignment(javafx.geometry.Pos.CENTER);

        VBox dateBox = new VBox();
        Label dateLabel = new Label("DATE");
        dateLabel.getStyleClass().add("match-info-label");
        Label dateValue = new Label(partido.getFecha() != null ?
            partido.getFecha().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) : "TBD");
        dateValue.getStyleClass().add("match-info-value");
        dateBox.getChildren().addAll(dateLabel, dateValue);

        VBox stadiumBox = new VBox();
        Label stadiumLabel = new Label("STADIUM");
        stadiumLabel.getStyleClass().add("match-info-label");
        Label stadiumValue = new Label(partido.getLugar() != null ? partido.getLugar() : "TBD");
        stadiumValue.getStyleClass().add("match-info-value");
        stadiumBox.getChildren().addAll(stadiumLabel, stadiumValue);

        info.getChildren().addAll(dateBox, stadiumBox);

        card.getChildren().addAll(header, teams, info);

        card.setOnMouseClicked(e -> selectPartido(partido, card));

        return card;
    }

    private void selectPartido(PartidoDTO partido, VBox card) {
        selectedPartido = partido;
        selectedLocalidad = null;
        selectedQty = 1;

        lblMatchSelected.setText(partido.getNombrePartido());

        for (javafx.scene.Node node : partidosContainer.getChildren()) {
            node.getStyleClass().remove("match-card-selected");
        }
        card.getStyleClass().add("match-card-selected");

        resetCart();

        viewModel.selectedPartido.set(partido);
        viewModel.loadLocalidades();
    }

    private void updateLocalidadesList() {
        localidadesContainer.getChildren().clear();

        for (LocalidadDTO loc : viewModel.localidades) {
            VBox btn = createLocalidadButton(loc);
            localidadesContainer.getChildren().add(btn);
        }
    }

    private VBox createLocalidadButton(LocalidadDTO loc) {
        VBox btn = new VBox();
        btn.getStyleClass().add("localidad-btn");
        btn.setSpacing(8);
        btn.setCursor(javafx.scene.Cursor.HAND);

        Label type = new Label(loc.getCodigoLocalidad());
        type.getStyleClass().add("localidad-type");

        Label price = new Label("$" + String.format("%.2f", loc.getPrecio()));
        price.getStyleClass().add("localidad-price");

        Label avail = new Label(loc.getDisponibilidad() + " disponibles");
        avail.getStyleClass().add("localidad-avail");

        btn.getChildren().addAll(type, price, avail);

        btn.setOnMouseClicked(e -> selectLocalidad(loc, btn));

        return btn;
    }

    private void selectLocalidad(LocalidadDTO loc, VBox btn) {
        selectedLocalidad = loc;
        selectedQty = 1;

        for (javafx.scene.Node node : localidadesContainer.getChildren()) {
            node.getStyleClass().remove("selected");
        }
        btn.getStyleClass().add("selected");

        quantitySelector.setVisible(true);
        lblQuantity.setText("1");

        updateOrderDetails();
        recalculateTotals();

        btnConfirmPurchase.setDisable(false);
    }

    @FXML
    private void onIncreaseQty() {
        if (selectedLocalidad == null) return;
        int maxQty = selectedLocalidad.getDisponibilidad();
        if (selectedQty < maxQty) {
            selectedQty++;
            lblQuantity.setText(String.valueOf(selectedQty));
            recalculateTotals();
        }
    }

    @FXML
    private void onDecreaseQty() {
        if (selectedQty > 1) {
            selectedQty--;
            lblQuantity.setText(String.valueOf(selectedQty));
            recalculateTotals();
        }
    }

    private void recalculateTotals() {
        if (selectedLocalidad == null) return;
        BigDecimal unitPrice = selectedLocalidad.getPrecio();
        BigDecimal subtotal = unitPrice.multiply(new BigDecimal(selectedQty));
        BigDecimal iva = subtotal.multiply(new BigDecimal("0.15"));
        BigDecimal total = subtotal.add(iva);

        lblSubtotal.setText("$" + String.format("%.2f", subtotal));
        lblIva.setText("$" + String.format("%.2f", iva));
        lblTotal.setText("$" + String.format("%.2f", total));
    }

    private void updateOrderDetails() {
        orderDetails.getChildren().clear();

        if (selectedPartido == null || selectedLocalidad == null) {
            Label empty = new Label("Seleccione un partido y localidad");
            empty.getStyleClass().add("empty-cart");
            orderDetails.getChildren().add(empty);
            return;
        }

        VBox item = new VBox();
        item.getStyleClass().add("order-item");
        item.setSpacing(5);

        HBox topRow = new HBox();
        topRow.setSpacing(10);

        Label matchName = new Label(selectedPartido.getNombrePartido());
        matchName.getStyleClass().add("order-match");

        Label priceLabel = new Label("$" + String.format("%.2f", selectedLocalidad.getPrecio()));
        priceLabel.getStyleClass().add("order-price");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        topRow.getChildren().addAll(matchName, spacer, priceLabel);

        Label seatLabel = new Label("Zona: " + selectedLocalidad.getCodigoLocalidad());
        seatLabel.getStyleClass().add("order-seat");

        item.getChildren().addAll(topRow, seatLabel);
        orderDetails.getChildren().add(item);
    }

    @FXML
    private void onConfirmPurchase() {
        logger.info("[COMPRA] ========== onConfirmPurchase CLICKED ==========");

        if (isProcessing) {
            logger.warn("[COMPRA] Compra ya en progreso, ignorando click");
            return;
        }

        if (selectedPartido == null || selectedLocalidad == null) {
            logger.warn("[COMPRA] selectedPartido o selectedLocalidad es null");
            viewModel.statusMessage.set("Seleccione un partido y localidad primero");
            return;
        }

        logger.info("[COMPRA] Partido: {} (cod: {}), Localidad: {}, Cantidad: {}",
            selectedPartido.getNombrePartido(), selectedPartido.getCodigo(),
            selectedLocalidad.getCodigoLocalidad(), selectedQty);

        isProcessing = true;
        btnConfirmPurchase.setDisable(true);
        viewModel.statusMessage.set("Procesando compra...");
        viewModel.isLoading.set(true);

        final PartidoDTO partidoRef = selectedPartido;
        final LocalidadDTO localidadRef = selectedLocalidad;
        final int qtyRef = selectedQty;

        Task<ComprobanteDTO> task = new Task<>() {
            @Override
            protected ComprobanteDTO call() {
                logger.info("[COMPRA-TASK] Llamando SOAP comprarBoletos...");
                return viewModel.getSoapClient().comprarBoletos(
                    username, password,
                    partidoRef.getCodigo(),
                    localidadRef.getCodigoLocalidad(),
                    qtyRef
                );
            }
        };

        task.setOnSucceeded(e -> {
            isProcessing = false;
            viewModel.isLoading.set(false);
            ComprobanteDTO comp = task.getValue();
            logger.info("[COMPRA] Resultado - exitoso: {}, mensaje: {}",
                comp != null && comp.isExitoso(), comp != null ? comp.getMensaje() : "null");

            if (comp != null && comp.isExitoso()) {
                viewModel.statusMessage.set("¡Compra exitosa! Redirigiendo a factura...");
                navigateToFactura(comp);
            } else {
                viewModel.statusMessage.set(comp != null ? comp.getMensaje() : "Error en la compra");
                btnConfirmPurchase.setDisable(selectedLocalidad == null);
            }
        });

        task.setOnFailed(e -> {
            isProcessing = false;
            viewModel.isLoading.set(false);
            logger.error("[COMPRA] Task failed: {}", task.getException().getMessage(), task.getException());
            viewModel.statusMessage.set("Error de conexion al realizar la compra: " + task.getException().getMessage());
            btnConfirmPurchase.setDisable(selectedLocalidad == null);
        });

        new Thread(task).start();
    }

    private void showPurchaseSuccess() {
        logger.info("[COMPRA] showPurchaseSuccess - navigating to factura view");
        resetCart();
    }

    private void navigateToFactura(ComprobanteDTO comp) {
        logger.info("[COMPRA] Navigating to factura view - facturaId: {}", comp.getFacturaId());
        if (dashboard != null) {
            dashboard.showFactura(comp);
        } else {
            logger.error("[COMPRA] Dashboard reference is null, cannot navigate");
            viewModel.statusMessage.set("Error al mostrar la factura");
            btnConfirmPurchase.setDisable(false);
        }
    }

    private void resetCart() {
        selectedLocalidad = null;
        selectedQty = 1;

        lblSubtotal.setText("$0.00");
        lblIva.setText("$0.00");
        lblTotal.setText("$0.00");
        btnConfirmPurchase.setDisable(true);
        quantitySelector.setVisible(false);

        for (javafx.scene.Node node : localidadesContainer.getChildren()) {
            node.getStyleClass().remove("selected");
        }

        orderDetails.getChildren().clear();
        Label empty = new Label("Seleccione una localidad para continuar");
        empty.getStyleClass().add("empty-cart");
        orderDetails.getChildren().add(empty);
    }
}
