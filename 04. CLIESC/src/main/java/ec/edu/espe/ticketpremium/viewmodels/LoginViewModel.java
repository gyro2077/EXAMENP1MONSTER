package ec.edu.espe.ticketpremium.viewmodels;

import ec.edu.espe.ticketpremium.models.SesionDTO;
import ec.edu.espe.ticketpremium.services.SoapClient;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginViewModel {
    private static final Logger logger = LoggerFactory.getLogger(LoginViewModel.class);
    
    private final SoapClient soapClient;
    public SimpleStringProperty username = new SimpleStringProperty("");
    public SimpleStringProperty password = new SimpleStringProperty("");
    public SimpleStringProperty errorMessage = new SimpleStringProperty("");
    public SimpleBooleanProperty isLoading = new SimpleBooleanProperty(false);
    public SimpleBooleanProperty loginSuccessful = new SimpleBooleanProperty(false);
    private SesionDTO lastSession;

    public LoginViewModel() {
        logger.info("[LOGIN-VM] Inicializando LoginViewModel");
        this.soapClient = new SoapClient();
    }

    public void login() {
        logger.info("[LOGIN-VM] ========================================");
        logger.info("[LOGIN-VM]Solicitud de login iniciada");
        logger.info("[LOGIN-VM] Username en propiedad: '{}'", username.get());
        
        if (username.get() == null || username.get().trim().isEmpty()) {
            logger.warn("[LOGIN-VM] Username vacio");
            errorMessage.set("Por favor ingrese el usuario");
            return;
        }
        
        if (password.get() == null || password.get().trim().isEmpty()) {
            logger.warn("[LOGIN-VM] Password vacio");
            errorMessage.set("Por favor ingrese la contrasena");
            return;
        }
        
        logger.info("[LOGIN-VM] Credenciales validas, iniciando proceso SOAP...");
        isLoading.set(true);
        errorMessage.set("");
        loginSuccessful.set(false);

        Task<SesionDTO> task = new Task<>() {
            @Override
            protected SesionDTO call() {
                logger.info("[LOGIN-VM-TASK] Ejecutando login en thread separado");
                logger.info("[LOGIN-VM-TASK] Llamando soapClient.login('{}', '***')", username.get());
                SesionDTO result = soapClient.login(username.get(), password.get());
                logger.info("[LOGIN-VM-TASK] Resultado recibido - exitoso: {}, mensaje: {}", 
                    result.isExitoso(), result.getMensaje());
                return result;
            }
        };

        task.setOnSucceeded(e -> {
            logger.info("[LOGIN-VM] Task completada exitosamente");
            Platform.runLater(() -> {
                isLoading.set(false);
                SesionDTO result = task.getValue();
                
                logger.info("[LOGIN-VM] Procesando resultado...");
                logger.info("[LOGIN-VM] result.isExitoso() = {}", result.isExitoso());
                logger.info("[LOGIN-VM] result.getMensaje() = '{}'", result.getMensaje());
                
                if (result.isExitoso()) {
                    logger.info("[LOGIN-VM] LOGIN EXITOSO!");
                    lastSession = result;
                    loginSuccessful.set(true);
                    errorMessage.set("");
                } else {
                    logger.warn("[LOGIN-VM] LOGIN FALLIDO - {}", result.getMensaje());
                    loginSuccessful.set(false);
                    errorMessage.set(result.getMensaje());
                }
            });
        });

        task.setOnFailed(e -> {
            logger.error("[LOGIN-VM] Task fallo con exception");
            logger.error("[LOGIN-VM] Exception: {}", task.getException().getClass().getName());
            logger.error("[LOGIN-VM] Mensaje: {}", task.getException().getMessage());
            task.getException().printStackTrace();
            
            Platform.runLater(() -> {
                isLoading.set(false);
                loginSuccessful.set(false);
                errorMessage.set("Error de conexion con el servidor. Verifique su conexion a internet.");
                logger.error("[LOGIN-VM] Mensaje de error establecido en UI");
            });
        });

        logger.info("[LOGIN-VM] Iniciando thread para login");
        new Thread(task, "LoginThread").start();
    }

    public SesionDTO getLastSession() {
        logger.info("[LOGIN-VM] Obteniendo lastSession: {}", lastSession);
        return lastSession;
    }
    
    public void clearSession() {
        logger.info("[LOGIN-VM] Limpiando sesion");
        this.lastSession = null;
        this.loginSuccessful.set(false);
    }
}