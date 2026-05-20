package ec.edu.espe.ticketpremium.services;

import ec.edu.espe.ticketpremium.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SoapClient {
    private static final Logger logger = LoggerFactory.getLogger(SoapClient.class);
    
    private static final String ENDPOINT = "http://209.145.48.25:8086/TicketPremiumWebService/TicketPremiumWebService";
    private static final String NAMESPACE = "http://soap.ticketpremium.espe.edu.ec/";

    private static final Map<Integer, String> FALLBACK_FECHAS = new HashMap<>();
    static {
        FALLBACK_FECHAS.put(1, "2026-06-15T19:00:00");
        FALLBACK_FECHAS.put(2, "2026-06-22T16:30:00");
        FALLBACK_FECHAS.put(3, "2026-07-01T20:00:00");
        FALLBACK_FECHAS.put(4, "2026-07-08T18:00:00");
        FALLBACK_FECHAS.put(5, "2026-07-15T19:30:00");
    }

    public SesionDTO login(String username, String password) {
        logger.info("========================================");
        logger.info("[LOGIN] Iniciando proceso de login...");
        logger.info("[LOGIN] Username: {}", username);
        
        String xml = buildLoginRequest(username, password);
        logger.info("[LOGIN] XML Request construido");
        
        String response = sendSoapRequest(xml, "login");
        logger.info("[LOGIN] Respuesta SOAP recibida, longitud: {} caracteres", response.length());
        
        if (response.isEmpty()) {
            logger.error("[LOGIN] Respuesta vacia - servidor no respondio");
            SesionDTO sesion = new SesionDTO();
            sesion.setExitoso(false);
            sesion.setMensaje("No se pudo conectar con el servidor. Verifique que el servicio este corriendo.");
            return sesion;
        }
        
        logger.info("[LOGIN] Intentando parsear respuesta XML...");
        SesionDTO sesion = parseLoginResponse(response);
        logger.info("[LOGIN] Sesion parseada - Exitoso: {}, Mensaje: {}", sesion.isExitoso(), sesion.getMensaje());
        
        return sesion;
    }

    public List<PartidoDTO> listarPartidosDisponibles() {
        String xml = buildRequest("listarPartidosDisponibles", "");
        String response = sendSoapRequest(xml, "listarPartidosDisponibles");
        return parsePartidosResponse(response);
    }

    public List<LocalidadDTO> listarLocalidadesDisponibles(int codigoPartido) {
        String xml = buildLocalidadRequest(codigoPartido);
        String response = sendSoapRequest(xml, "listarLocalidadesDisponibles");
        return parseLocalidadResponse(response);
    }

    public ComprobanteDTO comprarBoletos(String username, String password, int codigoPartido, String codigoLocalidad, int cantidad) {
        String xml = buildCompraRequest(username, password, codigoPartido, codigoLocalidad, cantidad);
        String response = sendSoapRequest(xml, "comprarBoletos");
        return parseComprobanteResponse(response);
    }

    public List<ReporteDTO> generarReporteVentas(int codigoPartido) {
        String xml = buildReporteRequest(codigoPartido);
        String response = sendSoapRequest(xml, "generarReporteVentas");
        return parseReporteResponse(response);
    }

    private String buildLoginRequest(String username, String password) {
        logger.info("[BUILDER] Construyendo request de login");
        logger.info("[BUILDER] Username: '{}'", username);
        logger.info("[BUILDER] Password length: {} caracteres", password != null ? password.length() : 0);
        
        String body = "<tns:login>" +
                "<username>" + escapeXml(username) + "</username>" +
                "<password>" + escapeXml(password) + "</password>" +
                "</tns:login>";
        
        String envelope = buildEnvelope(body);
        logger.info("[BUILDER] Envelope construido, longitud: {}", envelope.length());
        return envelope;
    }

    private String buildRequest(String operation, String body) {
        logger.info("[BUILDER] Construyendo request para operacion: {}", operation);
        String envelope = buildEnvelope("<tns:" + operation + "/>");
        return envelope;
    }

    private String buildLocalidadRequest(int codigoPartido) {
        logger.info("[BUILDER] Construyendo request de localidades - Partido: {}", codigoPartido);
        String body = "<tns:listarLocalidadesDisponibles>" +
                "<codigoPartido>" + codigoPartido + "</codigoPartido>" +
                "</tns:listarLocalidadesDisponibles>";
        return buildEnvelope(body);
    }

    private String buildCompraRequest(String username, String password, int codigoPartido, String codigoLocalidad, int cantidad) {
        logger.info("[BUILDER] Construyendo request de compra");
        logger.info("[BUILDER] Username: {}, Partido: {}, Localidad: {}, Cantidad: {}", 
            username, codigoPartido, codigoLocalidad, cantidad);
        String body = "<tns:comprarBoletos>" +
                "<username>" + escapeXml(username) + "</username>" +
                "<password>" + escapeXml(password) + "</password>" +
                "<codigoPartido>" + codigoPartido + "</codigoPartido>" +
                "<codigoLocalidad>" + escapeXml(codigoLocalidad) + "</codigoLocalidad>" +
                "<cantidad>" + cantidad + "</cantidad>" +
                "</tns:comprarBoletos>";
        return buildEnvelope(body);
    }

    private String buildReporteRequest(int codigoPartido) {
        logger.info("[BUILDER] Construyendo request de reporte - Partido: {}", codigoPartido);
        String body = "<tns:generarReporteVentas>" +
                "<codigoPartido>" + codigoPartido + "</codigoPartido>" +
                "</tns:generarReporteVentas>";
        return buildEnvelope(body);
    }

    private String buildEnvelope(String body) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                "xmlns:tns=\"" + NAMESPACE + "\">" +
                "<soap:Body>" + body + "</soap:Body>" +
                "</soap:Envelope>";
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private String sendSoapRequest(String xml, String operation) {
        logger.info("========================================");
        logger.info("[SOAP] Enviando request - Operation: {}", operation);
        logger.info("[SOAP] Endpoint: {}", ENDPOINT);
        logger.info("[SOAP] XML Request:\n{}", xml);
        
        StringBuilder fullResponse = new StringBuilder();
        
        try {
            URL url = new URL(ENDPOINT);
            logger.info("[SOAP] URL creada: {}", url);
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            logger.info("[SOAP] Conexion HTTP abierta");
            
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            conn.setRequestProperty("SOAPAction", "\"" + NAMESPACE + operation + "\"");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            
            logger.info("[SOAP] Headers configurados");
            logger.info("[SOAP] Content-Type: text/xml; charset=utf-8");
            logger.info("[SOAP] SOAPAction: {}", NAMESPACE + operation);

            try (OutputStream os = conn.getOutputStream()) {
                logger.info("[SOAP] Escribiendo XML al OutputStream ({} bytes)", xml.getBytes(StandardCharsets.UTF_8).length);
                os.write(xml.getBytes(StandardCharsets.UTF_8));
                os.flush();
                logger.info("[SOAP] XML enviado exitosamente");
            }
            
            int responseCode = conn.getResponseCode();
            String responseMessage = conn.getResponseMessage();
            logger.info("[SOAP] HTTP Response Code: {} - {}", responseCode, responseMessage);
            logger.info("[SOAP] Response Headers: {}", conn.getHeaderFields());

            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        fullResponse.append(line).append("\n");
                    }
                }
                String responseStr = fullResponse.toString();
                logger.info("[SOAP] Respuesta recibida ({} bytes, {} lineas)", responseStr.length(), responseStr.split("\n").length);
                logger.info("[SOAP] Respuesta XML (primeros 500 chars): {}", responseStr.substring(0, Math.min(500, responseStr.length())));
                
                if (responseStr.length() > 500) {
                    logger.info("[SOAP] Respuesta XML (ultimos 500 chars): {}", responseStr.substring(Math.max(0, responseStr.length() - 500)));
                }
                
                return responseStr;
            } else {
                logger.error("[SOAP] HTTP Error {} - {}", responseCode, responseMessage);
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        fullResponse.append(line).append("\n");
                    }
                }
                logger.error("[SOAP] Error Response Body: {}", fullResponse.toString());
                return "";
            }
        } catch (java.net.ConnectException e) {
            logger.error("[SOAP] CONNECTION REFUSED - El servidor拒绝了 la conexion");
            logger.error("[SOAP] Posibles causas:");
            logger.error("[SOAP] 1. El servidor Tomcat no esta corriendo");
            logger.error("[SOAP] 2. El servicio TicketPremiumWebService no esta desplegado");
            logger.error("[SOAP] 3. El puerto 8086 no esta abierto");
            logger.error("[SOAP] 4. Hay un firewall bloqueando la conexion");
            logger.error("[SOAP] Detalle del error: {}", e.getMessage());
            return "";
        } catch (java.net.SocketTimeoutException e) {
            logger.error("[SOAP] TIMEOUT - El servidor tardo demasiado en responder");
            logger.error("[SOAP] El servidor puede estar saturado o no responder");
            logger.error("[SOAP] Detalle: {}", e.getMessage());
            return "";
        } catch (Exception e) {
            logger.error("[SOAP] ERROR GENERAL - {}", e.getClass().getSimpleName());
            logger.error("[SOAP] Mensaje: {}", e.getMessage());
            logger.error("[SOAP] Stack trace:", e);
            return "";
        }
    }

    private SesionDTO parseLoginResponse(String xml) {
        logger.info("[PARSER] Iniciando parseo de respuesta login");
        SesionDTO sesion = new SesionDTO();
        
        if (xml == null || xml.trim().isEmpty()) {
            logger.error("[PARSER] XML es nulo o vacio, no se puede parsear");
            sesion.setExitoso(false);
            sesion.setMensaje("Respuesta vacia del servidor");
            return sesion;
        }
        
        try {
            logger.info("[PARSER] Creando DocumentBuilder...");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setIgnoringComments(true);
            factory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            logger.info("[PARSER] Parseando XML ({} caracteres)", xml.length());
            InputStream is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
            Document doc = builder.parse(is);
            doc.getDocumentElement().normalize();
            
            logger.info("[PARSER] Documento parseado, elemento raiz: {}", doc.getDocumentElement().getNodeName());
            
            NodeList allElements = doc.getElementsByTagName("*");
            logger.info("[PARSER] Total de elementos en el documento: {}", allElements.getLength());
            for (int i = 0; i < Math.min(allElements.getLength(), 30); i++) {
                Node node = allElements.item(i);
                logger.info("[PARSER] Elemento[{}]: {} = '{}'", i, node.getNodeName(), node.getTextContent().trim());
            }
            
            NodeList returnNodes = doc.getElementsByTagName("return");
            logger.info("[PARSER] Encontrados {} elementos 'return'", returnNodes.getLength());
            
            if (returnNodes.getLength() > 0) {
                Element returnEl = (Element) returnNodes.item(0);
                logger.info("[PARSER] Parseando elemento return...");
                
                String exitosoStr = getElementValue(returnEl, "exitoso");
                logger.info("[PARSER] Valor 'exitoso': '{}'", exitosoStr);
                sesion.setExitoso("true".equalsIgnoreCase(exitosoStr));
                
                String mensaje = getElementValue(returnEl, "mensaje");
                logger.info("[PARSER] Valor 'mensaje': '{}'", mensaje);
                sesion.setMensaje(mensaje);
                
                String usernameVal = getElementValue(returnEl, "username");
                logger.info("[PARSER] Valor 'username': '{}'", usernameVal);
                sesion.setUsername(usernameVal);
                
                String clienteIdStr = getElementValue(returnEl, "clienteId");
                logger.info("[PARSER] Valor 'clienteId': '{}'", clienteIdStr);
                if (clienteIdStr != null && !clienteIdStr.isEmpty()) {
                    try {
                        sesion.setClienteId(Integer.parseInt(clienteIdStr));
                        logger.info("[PARSER] clienteId parseado correctamente: {}", sesion.getClienteId());
                    } catch (NumberFormatException e) {
                        logger.warn("[PARSER] No se pudo parsear clienteId: '{}'", clienteIdStr);
                        sesion.setClienteId(null);
                    }
                } else {
                    sesion.setClienteId(null);
                }
            } else {
                logger.warn("[PARSER] No se encontro elemento 'return' en la respuesta");
                logger.info("[PARSER] Buscando alternativas para parseo...");
                
                NodeList successNodes = doc.getElementsByTagName("success");
                NodeList resultNodes = doc.getElementsByTagName("result");
                NodeList bodyNodes = doc.getElementsByTagName("Body");
                
                logger.info("[PARSER] success elements: {}, result elements: {}, body elements: {}", 
                    successNodes.getLength(), resultNodes.getLength(), bodyNodes.getLength());
                
                sesion.setExitoso(false);
                sesion.setMensaje("Estructura de respuesta inesperada");
            }
            
            logger.info("[PARSER] Sesion final - exitoso:{}, mensaje:{}, username:{}, clienteId:{}", 
                sesion.isExitoso(), sesion.getMensaje(), sesion.getUsername(), sesion.getClienteId());
            
        } catch (Exception e) {
            logger.error("[PARSER] Error al parsear respuesta: {}", e.getClass().getSimpleName());
            logger.error("[PARSER] Mensaje: {}", e.getMessage());
            logger.error("[PARSER] Stack trace:", e);
            sesion.setExitoso(false);
            sesion.setMensaje("Error al procesar respuesta del servidor");
        }
        
        return sesion;
    }

    private List<PartidoDTO> parsePartidosResponse(String xml) {
        logger.info("[PARSER-PARTIDOS] Iniciando parseo de partidos");
        List<PartidoDTO> partidos = new ArrayList<>();
        
        if (xml == null || xml.trim().isEmpty()) {
            logger.error("[PARSER-PARTIDOS] XML nulo o vacio");
            return partidos;
        }
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
            Document doc = builder.parse(is);
            doc.getDocumentElement().normalize();
            
            NodeList returnNodes = doc.getElementsByTagName("return");
            logger.info("[PARSER-PARTIDOS] Encontrados {} elementos return", returnNodes.getLength());
            
            for (int i = 0; i < returnNodes.getLength(); i++) {
                Element el = (Element) returnNodes.item(i);
                PartidoDTO partido = new PartidoDTO();
                
                String codigoStr = getElementValue(el, "codigo");
                logger.info("[PARSER-PARTIDOS] Partido[{}] codigo: {}", i, codigoStr);
                try {
                    partido.setCodigo(Integer.parseInt(codigoStr));
                } catch (Exception e) {
                    logger.error("[PARSER-PARTIDOS] Error parseando codigo: {}", codigoStr);
                }
                
                partido.setEquipoLocal(getElementValue(el, "equipoLocal"));
                partido.setEquipoVisita(getElementValue(el, "equipoVisita"));
                
                String fechaStr = getElementValue(el, "fecha");
                logger.info("[PARSER-PARTIDOS] Partido[{}] fecha: '{}'", i, fechaStr);
                if (!fechaStr.isEmpty()) {
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
                        partido.setFecha(LocalDateTime.parse(fechaStr, formatter));
                    } catch (DateTimeParseException e) {
                        try {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                            partido.setFecha(LocalDateTime.parse(fechaStr, formatter));
                        } catch (Exception ex) {
                            logger.warn("[PARSER-PARTIDOS] No se pudo parsear fecha: {}", fechaStr);
                        }
                    }
                } else {
                    Integer codigo = partido.getCodigo();
                    String fallbackFecha = FALLBACK_FECHAS.get(codigo);
                    if (fallbackFecha != null) {
                        logger.info("[PARSER-PARTIDOS] Fecha vacia para partido {}, aplicando fallback: {}", codigo, fallbackFecha);
                        partido.setFecha(LocalDateTime.parse(fallbackFecha));
                    } else {
                        logger.warn("[PARSER-PARTIDOS] Fecha vacia y sin fallback para partido {}", codigo);
                    }
                }
                
                partido.setLugar(getElementValue(el, "lugar"));
                partidos.add(partido);
                logger.info("[PARSER-PARTIDOS] Partido agregado: {} vs {} ({})", 
                    partido.getEquipoLocal(), partido.getEquipoVisita(), partido.getCodigo());
            }
            logger.info("[PARSER-PARTIDOS] Total partidos parseados: {}", partidos.size());
        } catch (Exception e) {
            logger.error("[PARSER-PARTIDOS] Error: {}", e.getMessage(), e);
        }
        return partidos;
    }

    private List<LocalidadDTO> parseLocalidadResponse(String xml) {
        logger.info("[PARSER-LOCALIDADES] Iniciando parseo de localidades");
        List<LocalidadDTO> localidades = new ArrayList<>();
        
        if (xml == null || xml.trim().isEmpty()) {
            logger.error("[PARSER-LOCALIDADES] XML nulo o vacio");
            return localidades;
        }
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
            Document doc = builder.parse(is);
            doc.getDocumentElement().normalize();
            
            NodeList returnNodes = doc.getElementsByTagName("return");
            logger.info("[PARSER-LOCALIDADES] Encontrados {} elementos return", returnNodes.getLength());
            
            for (int i = 0; i < returnNodes.getLength(); i++) {
                Element el = (Element) returnNodes.item(i);
                LocalidadDTO localidad = new LocalidadDTO();
                
                String codigo = getElementValue(el, "codigoLocalidad");
                logger.info("[PARSER-LOCALIDADES] Localidad[{}] codigo: {}", i, codigo);
                localidad.setCodigoLocalidad(codigo);
                
                String disp = getElementValue(el, "disponibilidad");
                try {
                    localidad.setDisponibilidad(disp.isEmpty() ? 0 : Integer.parseInt(disp));
                } catch (Exception e) {
                    logger.error("[PARSER-LOCALIDADES] Error parseando disponibilidad: {}", disp);
                    localidad.setDisponibilidad(0);
                }
                
                String precio = getElementValue(el, "precio");
                try {
                    localidad.setPrecio(precio.isEmpty() ? BigDecimal.ZERO : new BigDecimal(precio));
                } catch (Exception e) {
                    logger.error("[PARSER-LOCALIDADES] Error parseando precio: {}", precio);
                    localidad.setPrecio(BigDecimal.ZERO);
                }
                
                localidades.add(localidad);
                logger.info("[PARSER-LOCALIDADES] Localidad agregada: {} - disponible: {} - precio: {}", 
                    localidad.getCodigoLocalidad(), localidad.getDisponibilidad(), localidad.getPrecio());
            }
            logger.info("[PARSER-LOCALIDADES] Total localidades parseadas: {}", localidades.size());
        } catch (Exception e) {
            logger.error("[PARSER-LOCALIDADES] Error: {}", e.getMessage(), e);
        }
        return localidades;
    }

    private ComprobanteDTO parseComprobanteResponse(String xml) {
        logger.info("[PARSER-COMP] Iniciando parseo de comprobante");
        ComprobanteDTO comprobante = new ComprobanteDTO();
        
        if (xml == null || xml.trim().isEmpty()) {
            logger.error("[PARSER-COMP] XML nulo o vacio");
            comprobante.setExitoso(false);
            comprobante.setMensaje("Respuesta vacia del servidor");
            return comprobante;
        }
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
            Document doc = builder.parse(is);
            doc.getDocumentElement().normalize();
            
            NodeList returnNodes = doc.getElementsByTagName("return");
            logger.info("[PARSER-COMP] Encontrados {} elementos return", returnNodes.getLength());
            
            if (returnNodes.getLength() > 0) {
                Element el = (Element) returnNodes.item(0);
                
                String exitosoStr = getElementValue(el, "exitoso");
                logger.info("[PARSER-COMP] exitoso: {}", exitosoStr);
                comprobante.setExitoso("true".equalsIgnoreCase(exitosoStr));
                
                String mensaje = getElementValue(el, "mensaje");
                logger.info("[PARSER-COMP] mensaje: {}", mensaje);
                comprobante.setMensaje(mensaje);
                
                String fid = getElementValue(el, "facturaId");
                if (fid != null && !fid.isEmpty()) {
                    try {
                        comprobante.setFacturaId(Integer.parseInt(fid));
                        logger.info("[PARSER-COMP] facturaId: {}", comprobante.getFacturaId());
                    } catch (Exception e) {
                        logger.warn("[PARSER-COMP] No se pudo parsear facturaId: {}", fid);
                    }
                }
                
                comprobante.setClienteNombre(getElementValue(el, "clienteNombre"));
                comprobante.setClienteCedula(getElementValue(el, "clienteCedula"));
                
                String fechaStr = getElementValue(el, "fechaCompra");
                if (!fechaStr.isEmpty()) {
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
                        comprobante.setFechaCompra(LocalDateTime.parse(fechaStr, formatter));
                    } catch (DateTimeParseException e) {
                        try {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                            comprobante.setFechaCompra(LocalDateTime.parse(fechaStr, formatter));
                        } catch (Exception ex) {
                            logger.warn("[PARSER-COMP] No se pudo parsear fecha: {}", fechaStr);
                        }
                    }
                }
                
                String st = getElementValue(el, "subtotal");
                try {
                    comprobante.setSubtotal(st.isEmpty() ? BigDecimal.ZERO : new BigDecimal(st));
                } catch (Exception e) {
                    comprobante.setSubtotal(BigDecimal.ZERO);
                }
                
                String iva = getElementValue(el, "iva");
                try {
                    comprobante.setIva(iva.isEmpty() ? BigDecimal.ZERO : new BigDecimal(iva));
                } catch (Exception e) {
                    comprobante.setIva(BigDecimal.ZERO);
                }
                
                String tot = getElementValue(el, "total");
                try {
                    comprobante.setTotal(tot.isEmpty() ? BigDecimal.ZERO : new BigDecimal(tot));
                } catch (Exception e) {
                    comprobante.setTotal(BigDecimal.ZERO);
                }
                
                String cp = getElementValue(el, "codigoPartido");
                if (cp != null && !cp.isEmpty()) {
                    try {
                        comprobante.setCodigoPartido(Integer.parseInt(cp));
                    } catch (Exception e) {}
                }
                
                comprobante.setNombrePartido(getElementValue(el, "nombrePartido"));
                comprobante.setCodigoLocalidad(getElementValue(el, "codigoLocalidad"));
                
                String cant = getElementValue(el, "cantidad");
                try {
                    comprobante.setCantidad(cant.isEmpty() ? 0 : Integer.parseInt(cant));
                } catch (Exception e) {
                    comprobante.setCantidad(0);
                }
                
                String pu = getElementValue(el, "precioUnitario");
                try {
                    comprobante.setPrecioUnitario(pu.isEmpty() ? BigDecimal.ZERO : new BigDecimal(pu));
                } catch (Exception e) {
                    comprobante.setPrecioUnitario(BigDecimal.ZERO);
                }
            }
            logger.info("[PARSER-COMP] Comprobante parseado - exitoso: {}, mensaje: {}", 
                comprobante.isExitoso(), comprobante.getMensaje());
        } catch (Exception e) {
            logger.error("[PARSER-COMP] Error: {}", e.getMessage(), e);
            comprobante.setExitoso(false);
            comprobante.setMensaje("Error al procesar respuesta");
        }
        return comprobante;
    }

    private List<ReporteDTO> parseReporteResponse(String xml) {
        logger.info("[PARSER-REPORTE] Iniciando parseo de reportes");
        List<ReporteDTO> reportes = new ArrayList<>();
        
        if (xml == null || xml.trim().isEmpty()) {
            logger.error("[PARSER-REPORTE] XML nulo o vacio");
            return reportes;
        }
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
            Document doc = builder.parse(is);
            doc.getDocumentElement().normalize();
            
            NodeList returnNodes = doc.getElementsByTagName("return");
            logger.info("[PARSER-REPORTE] Encontrados {} elementos return", returnNodes.getLength());
            
            for (int i = 0; i < returnNodes.getLength(); i++) {
                Element el = (Element) returnNodes.item(i);
                ReporteDTO reporte = new ReporteDTO();
                
                String cp = getElementValue(el, "codigoPartido");
                if (cp != null && !cp.isEmpty()) {
                    try {
                        reporte.setCodigoPartido(Integer.parseInt(cp));
                    } catch (Exception e) {
                        reporte.setCodigoPartido(null);
                    }
                }
                
                reporte.setNombrePartido(getElementValue(el, "nombrePartido"));
                reporte.setCodigoLocalidad(getElementValue(el, "codigoLocalidad"));
                
                String ct = getElementValue(el, "cantidadTotalVendida");
                try {
                    reporte.setCantidadTotalVendida(ct.isEmpty() ? 0 : Integer.parseInt(ct));
                } catch (Exception e) {
                    reporte.setCantidadTotalVendida(0);
                }
                
                String tr = getElementValue(el, "totalRecaudado");
                try {
                    reporte.setTotalRecaudado(tr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(tr));
                } catch (Exception e) {
                    reporte.setTotalRecaudado(BigDecimal.ZERO);
                }
                
                reportes.add(reporte);
                logger.info("[PARSER-REPORTE] Reporte[{}]: {} - {} vendidos - ${}", 
                    i, reporte.getCodigoLocalidad(), reporte.getCantidadTotalVendida(), reporte.getTotalRecaudado());
            }
            logger.info("[PARSER-REPORTE] Total reportes: {}", reportes.size());
        } catch (Exception e) {
            logger.error("[PARSER-REPORTE] Error: {}", e.getMessage(), e);
        }
        return reportes;
    }

    private String getElementValue(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return "";
    }
}