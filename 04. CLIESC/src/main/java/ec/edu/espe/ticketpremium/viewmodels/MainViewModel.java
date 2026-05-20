package ec.edu.espe.ticketpremium.viewmodels;

import ec.edu.espe.ticketpremium.models.*;
import ec.edu.espe.ticketpremium.services.SoapClient;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class MainViewModel {
    private final SoapClient soapClient;
    public SimpleStringProperty username = new SimpleStringProperty("");
    public SimpleStringProperty password = new SimpleStringProperty("");

    public ObservableList<PartidoDTO> partidos = FXCollections.observableArrayList();
    public ObservableList<LocalidadDTO> localidades = FXCollections.observableArrayList();
    public ObservableList<ReporteDTO> reportes = FXCollections.observableArrayList();

    public SimpleObjectProperty<PartidoDTO> selectedPartido = new SimpleObjectProperty<>();
    public SimpleObjectProperty<LocalidadDTO> selectedLocalidad = new SimpleObjectProperty<>();

    public SimpleStringProperty cantidad = new SimpleStringProperty("1");
    public SimpleStringProperty statusMessage = new SimpleStringProperty("");
    public SimpleBooleanProperty isLoading = new SimpleBooleanProperty(false);
    public SimpleBooleanProperty showPurchaseConfirmation = new SimpleBooleanProperty(false);
    public SimpleBooleanProperty showReport = new SimpleBooleanProperty(false);

    public SimpleObjectProperty<ComprobanteDTO> lastComprobante = new SimpleObjectProperty<>();

    public MainViewModel(String username, String password) {
        this.soapClient = new SoapClient();
        this.username.set(username);
        this.password.set(password);
    }

    public SoapClient getSoapClient() {
        return soapClient;
    }

    public void loadPartidos() {
        isLoading.set(true);
        statusMessage.set("Cargando partidos...");

        Task<List<PartidoDTO>> task = new Task<>() {
            @Override
            protected List<PartidoDTO> call() {
                return soapClient.listarPartidosDisponibles();
            }
        };

        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                isLoading.set(false);
                List<PartidoDTO> result = task.getValue();
                LocalDateTime now = LocalDateTime.now();
                partidos.clear();
                for (PartidoDTO p : result) {
                    if (p.getFecha() == null || !p.getFecha().isBefore(now)) {
                        partidos.add(p);
                    }
                }
                statusMessage.set(partidos.isEmpty() ? "No hay partidos disponibles" : "");
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                isLoading.set(false);
                statusMessage.set("Error al cargar partidos");
            });
        });

        new Thread(task).start();
    }

    public void loadLocalidades() {
        PartidoDTO partido = selectedPartido.get();
        if (partido == null) return;

        isLoading.set(true);
        statusMessage.set("Cargando localidades...");

        Task<List<LocalidadDTO>> task = new Task<>() {
            @Override
            protected List<LocalidadDTO> call() {
                return soapClient.listarLocalidadesDisponibles(partido.getCodigo());
            }
        };

        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                isLoading.set(false);
                localidades.clear();
                localidades.addAll(task.getValue());
                statusMessage.set("");
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                isLoading.set(false);
                statusMessage.set("Error al cargar localidades");
            });
        });

        new Thread(task).start();
    }

    public void comprarBoletos() {
        if (selectedPartido.get() == null || selectedLocalidad.get() == null) {
            statusMessage.set("Seleccione partido y localidad");
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(cantidad.get());
            if (qty <= 0) throw new NumberFormatException();
        } catch (Exception e) {
            statusMessage.set("Cantidad inválida");
            return;
        }

        if (qty > selectedLocalidad.get().getDisponibilidad()) {
            statusMessage.set("Cantidad excede disponibilidad");
            return;
        }

        isLoading.set(true);
        statusMessage.set("Procesando compra...");

        Task<ComprobanteDTO> task = new Task<>() {
            @Override
            protected ComprobanteDTO call() {
                return soapClient.comprarBoletos(
                        username.get(),
                        password.get(),
                        selectedPartido.get().getCodigo(),
                        selectedLocalidad.get().getCodigoLocalidad(),
                        qty
                );
            }
        };

        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                isLoading.set(false);
                ComprobanteDTO result = task.getValue();
                if (result.isExitoso()) {
                    lastComprobante.set(result);
                    showPurchaseConfirmation.set(true);
                    statusMessage.set("Compra exitosa!");
                    loadPartidos();
                } else {
                    statusMessage.set(result.getMensaje());
                }
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                isLoading.set(false);
                statusMessage.set("Error al procesar compra");
            });
        });

        new Thread(task).start();
    }

    public void loadReportes() {
        if (selectedPartido.get() == null) return;

        isLoading.set(true);
        statusMessage.set("Cargando reporte...");

        Task<List<ReporteDTO>> task = new Task<>() {
            @Override
            protected List<ReporteDTO> call() {
                return soapClient.generarReporteVentas(selectedPartido.get().getCodigo());
            }
        };

        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                isLoading.set(false);
                reportes.clear();
                reportes.addAll(task.getValue());
                showReport.set(true);
                statusMessage.set("");
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                isLoading.set(false);
                statusMessage.set("Error al cargar reporte");
            });
        });

        new Thread(task).start();
    }

    public void closePurchaseConfirmation() {
        showPurchaseConfirmation.set(false);
    }

    public void closeReport() {
        showReport.set(false);
    }
}