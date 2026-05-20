package ec.edu.espe.ticketpremium.controllers;

import ec.edu.espe.ticketpremium.models.*;
import ec.edu.espe.ticketpremium.viewmodels.MainViewModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

public class MainController {
    @FXML private Label lblMatchSelected;
    @FXML private Label lblSubtotal;
    @FXML private Label lblIva;
    @FXML private Label lblTotal;
    @FXML private Label lblActiveMatch;
    @FXML private Label lblReportMatch;
    @FXML private Label lblReportTotal;
    @FXML private Label lblLoading;
    @FXML private Label lblStatus;

    @FXML private HBox partidosContainer;
    @FXML private HBox localidadesContainer;
    @FXML private VBox orderDetails;
    @FXML private VBox reportContent;

    @FXML private Button btnConfirmPurchase;

    @FXML private HBox purchaseSuccess;
    @FXML private VBox reportModal;

    private MainViewModel viewModel;
    private LocalidadDTO selectedLocalidad;
    private PartidoDTO selectedPartido;
    private BigDecimal currentSubtotal = BigDecimal.ZERO;

    public void initData(String username, String password) {
        viewModel = new MainViewModel(username, password);

        lblStatus.textProperty().bind(viewModel.statusMessage);
        lblLoading.visibleProperty().bind(viewModel.isLoading);
        purchaseSuccess.visibleProperty().bind(viewModel.showPurchaseConfirmation);
        reportModal.visibleProperty().bind(viewModel.showReport);

        viewModel.loadPartidos();

        viewModel.partidos.addListener((javafx.collections.ListChangeListener<PartidoDTO>) change -> {
            updatePartidosList();
        });

        viewModel.localidades.addListener((javafx.collections.ListChangeListener<LocalidadDTO>) change -> {
            updateLocalidadesList();
        });

        viewModel.reportes.addListener((javafx.collections.ListChangeListener<ReporteDTO>) change -> {
            updateReportTable();
        });

        viewModel.lastComprobante.addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showPurchaseSuccess(newVal);
            }
        });
    }

    private void updatePartidosList() {
        partidosContainer.getChildren().clear();
        LocalDateTime now = LocalDateTime.now();

        for (PartidoDTO partido : viewModel.partidos) {
            VBox card = createMatchCard(partido);
            partidosContainer.getChildren().add(card);
        }
    }

    private VBox createMatchCard(PartidoDTO partido) {
        VBox card = new VBox();
        card.getStyleClass().add("match-card");
        card.setSpacing(15);
        card.setPadding(new Insets(20));

        HBox header = new HBox();
        header.setSpacing(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label tag = new Label("MATCH");
        tag.getStyleClass().add("match-tag");

        String timeStr = partido.getFecha() != null ?
            partido.getFecha().toLocalTime().toString() : "TBD";
        Label time = new Label(timeStr);
        time.getStyleClass().add("match-time");

        header.getChildren().addAll(tag, new Region(), time);
        card.getChildren().add(header);

        HBox teams = new HBox();
        teams.setSpacing(20);
        teams.setAlignment(javafx.geometry.Pos.CENTER);

        Label local = new Label(partido.getEquipoLocal().toUpperCase());
        local.getStyleClass().add("team-name");

        Label vs = new Label("VS");
        vs.getStyleClass().add("vs-text");

        Label visita = new Label(partido.getEquipoVisita().toUpperCase());
        visita.getStyleClass().add("team-name");

        teams.getChildren().addAll(local, vs, visita);
        card.getChildren().add(teams);

        HBox info = new HBox();
        info.setSpacing(20);
        info.setAlignment(javafx.geometry.Pos.CENTER);

        VBox dateBox = new VBox();
        Label dateLabel = new Label("DATE");
        dateLabel.getStyleClass().add("match-info-label");
        Label dateValue = new Label(partido.getFecha() != null ?
            partido.getFecha().toLocalDate().toString() : "TBD");
        dateValue.getStyleClass().add("match-info-value");
        dateBox.getChildren().addAll(dateLabel, dateValue);

        VBox stadiumBox = new VBox();
        Label stadiumLabel = new Label("STADIUM");
        stadiumLabel.getStyleClass().add("match-info-label");
        Label stadiumValue = new Label(partido.getLugar() != null ?
            partido.getLugar() : "TBD");
        stadiumValue.getStyleClass().add("match-info-value");
        stadiumBox.getChildren().addAll(stadiumLabel, stadiumValue);

        info.getChildren().addAll(dateBox, stadiumBox);
        card.getChildren().add(info);

        card.setOnMouseClicked(e -> {
            selectPartido(partido, card);
        });

        return card;
    }

    private void selectPartido(PartidoDTO partido, VBox card) {
        selectedPartido = partido;
        lblMatchSelected.setText(partido.getNombrePartido());
        lblActiveMatch.setText(partido.getNombrePartido());

        for (javafx.scene.Node node : partidosContainer.getChildren()) {
            node.getStyleClass().remove("match-card-selected");
        }
        card.getStyleClass().add("match-card-selected");

        viewModel.selectedPartido.set(partido);
        viewModel.loadLocalidades();
    }

    private void updateLocalidadesList() {
        localidadesContainer.getChildren().clear();

        for (LocalidadDTO loc : viewModel.localidades) {
            Button seatBtn = createSeatButton(loc);
            localidadesContainer.getChildren().add(seatBtn);
        }
    }

    private Button createSeatButton(LocalidadDTO loc) {
        Button btn = new Button();
        btn.getStyleClass().add("seat-button");

        VBox content = new VBox();
        content.setSpacing(5);

        Label zone = new Label("ZONE");
        zone.getStyleClass().add("seat-zone");

        Label type = new Label(loc.getCodigoLocalidad().toUpperCase());
        type.getStyleClass().add("seat-type");

        HBox priceRow = new HBox();
        priceRow.setSpacing(10);
        priceRow.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Label price = new Label("$" + loc.getPrecio().toString() + ".00");
        price.getStyleClass().add("seat-price");

        Label icon = new Label("+");
        icon.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 18px;");

        priceRow.getChildren().addAll(price, icon);
        content.getChildren().addAll(zone, type, priceRow);
        btn.setGraphic(content);

        btn.setOnAction(e -> selectLocalidad(loc));

        return btn;
    }

    private void selectLocalidad(LocalidadDTO loc) {
        selectedLocalidad = loc;

        updateOrderDetails();

        lblSubtotal.setText("$" + formatCurrency(currentSubtotal));

        BigDecimal iva = currentSubtotal.multiply(new BigDecimal("0.15"));
        lblIva.setText("$" + formatCurrency(iva));

        BigDecimal total = currentSubtotal.add(iva);
        lblTotal.setText("$" + formatCurrency(total));

        btnConfirmPurchase.setDisable(false);
    }

    private void updateOrderDetails() {
        orderDetails.getChildren().clear();

        if (selectedPartido == null || selectedLocalidad == null) {
            Label empty = new Label("Select a match and seat to continue checkout");
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

        BigDecimal price = selectedLocalidad.getPrecio();
        Label priceLabel = new Label("$" + price.toString() + ".00");
        priceLabel.getStyleClass().add("order-price");

        topRow.getChildren().addAll(matchName, new Region(), priceLabel);

        Label seatLabel = new Label("Section: " + selectedLocalidad.getCodigoLocalidad().toUpperCase());
        seatLabel.getStyleClass().add("order-seat");

        item.getChildren().addAll(topRow, seatLabel);
        orderDetails.getChildren().add(item);

        currentSubtotal = price;
    }

    @FXML
    private void onConfirmPurchase() {
        viewModel.comprarBoletos();
    }

    @FXML
    private void onViewReport() {
        if (selectedPartido != null) {
            viewModel.selectedPartido.set(selectedPartido);
            viewModel.loadReportes();
        }
    }

    @FXML
    private void closeReport() {
        viewModel.closeReport();
    }

    private void showPurchaseSuccess(ComprobanteDTO comp) {
        purchaseSuccess.setVisible(true);

        resetCart();

        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> {
                    purchaseSuccess.setVisible(false);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void resetCart() {
        selectedLocalidad = null;
        currentSubtotal = BigDecimal.ZERO;

        lblSubtotal.setText("$0.00");
        lblIva.setText("$0.00");
        lblTotal.setText("$0.00");
        lblReportTotal.setText("$0.00");
        btnConfirmPurchase.setDisable(true);

        orderDetails.getChildren().clear();
        Label empty = new Label("Select a match and seat to continue checkout");
        empty.getStyleClass().add("empty-cart");
        orderDetails.getChildren().add(empty);
    }
    
    private String formatCurrency(BigDecimal value) {
        if (value == null) return "0.00";
        return String.format("%.2f", value);
    }

    private void updateReportTable() {
        reportContent.getChildren().clear();
        lblReportMatch.setText(selectedPartido != null ?
            selectedPartido.getNombrePartido() : "Select a match");

        if (viewModel.reportes.isEmpty()) {
            Label noData = new Label("No sales data available for this match");
            noData.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 14px;");
            reportContent.getChildren().add(noData);
            return;
        }

        BigDecimal totalGeneral = BigDecimal.ZERO;
        int totalVendidos = 0;
        
        for (int i = 0; i < viewModel.reportes.size(); i++) {
            ReporteDTO r = viewModel.reportes.get(i);
            totalGeneral = totalGeneral.add(r.getTotalRecaudado() != null ? r.getTotalRecaudado() : BigDecimal.ZERO);

            HBox row = new HBox();
            row.setSpacing(10);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            row.setPadding(new Insets(12, 15, 12, 15));
            row.setStyle(i % 2 == 0 ?
                "-fx-background-color: #1E293B;" :
                "-fx-background-color: #222a3d;");

            Label localidad = new Label(r.getCodigoLocalidad());
            localidad.setPrefWidth(150);
            localidad.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 14px;");

            Label vendidos = new Label(String.valueOf(r.getCantidadTotalVendida()));
            vendidos.setPrefWidth(100);
            vendidos.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 14px;");

            Label total = new Label("$" + formatCurrency(r.getTotalRecaudado()));
            total.setPrefWidth(150);
            total.setStyle("-fx-text-fill: #94de2d; -fx-font-size: 14px; -fx-font-weight: bold;");

            row.getChildren().addAll(localidad, vendidos, total);
            reportContent.getChildren().add(row);

            totalVendidos += r.getCantidadTotalVendida();
        }

        lblReportTotal.setText("$" + formatCurrency(totalGeneral));
    }

    @FXML
    private void onLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) lblStatus.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Ticket Premium - Login");
            stage.setMaximized(false);
            stage.setWidth(1000);
            stage.setHeight(700);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}