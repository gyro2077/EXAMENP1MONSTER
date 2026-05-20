# TicketPremium Desktop - Cliente JavaFX

Aplicacion de escritorio moderna para compra de boletos de futbol. Construida con **JavaFX 21**, arquitectura **MVVM**, y consumo de servicios **SOAP XML**.

## Informacion del Cliente

| Propiedad            | Valor                                              |
| -------------------- | -------------------------------------------------- |
| **Tecnologia**       | Java 21, JavaFX 21, FXML, CSS                      |
| **Arquitectura**     | MVVM (Model-View-ViewModel)                        |
| **Comunicacion**     | SOAP XML (HttpURLConnection + DOM Parser)          |
| **Build Tool**       | Maven                                              |
| **Logging**          | SLF4J + Logback                                    |
| **Endpoint SOAP**    | `http://209.145.48.25:8086/TicketPremiumWebService/TicketPremiumWebService` |

## Requisitos Previos

- **Java 21 LTS** instalado
- **Maven 3.8+** instalado
- **Servidor Payara** corriendo en `localhost:8086` con el servicio `TicketPremiumWebService` desplegado
- **PostgreSQL** corriendo con la base de datos `ticketpremium_db`

## Ejecucion

### Comando principal

```bash
mvn clean compile javafx:run
```

Este comando:
1. Limpia builds anteriores (`clean`)
2. Compila el codigo fuente (`compile`)
3. Ejecuta la aplicacion JavaFX con las dependencias nativas de Linux (`javafx:run`)

### Build sin ejecutar

```bash
mvn clean package -DskipTests
```

## Credenciales de Prueba

| Campo      | Valor          |
| ---------- | -------------- |
| **Usuario**| `MONSTER`      |
| **Password**| `MONSTER9`    |

## Flujo de la Aplicacion

1. **Login** → Ingresa credenciales en la pantalla de inicio de sesion
2. **Catalogo de Partidos** → Selecciona un partido disponible
3. **Seleccion de Localidad** → Elige la zona (GENERAL, TRIBUNA, PALCO, etc.)
4. **Confirmar Compra** → Revisa el resumen y confirma la compra
5. **Reportes** → Visualiza estadisticas de ventas por partido

## Arquitectura del Proyecto

```
src/main/java/ec/edu/espe/ticketpremium/
├── app/                    # Punto de entrada (Main.java)
├── controllers/            # Controladores FXML (View → ViewModel)
├── viewmodels/             # ViewModel con estado observable
├── services/               # Cliente SOAP desacoplado
├── models/                 # DTOs (PartidoDTO, LocalidadDTO, etc.)
└── ...

src/main/resources/
├── views/                  # Archivos FXML (login.fxml, main.fxml)
└── styles/                 # CSS de la interfaz (main.css)
```

## Operaciones SOAP Consumidas

El cliente consume 5 operaciones del servicio SOAP:

| Operacion                      | Descripcion                              |
| ------------------------------ | ---------------------------------------- |
| `login`                        | Autenticacion de usuario                 |
| `listarPartidosDisponibles`    | Obtiene catalogo de partidos             |
| `listarLocalidadesDisponibles` | Obtiene zonas y precios de un partido    |
| `comprarBoletos`               | Realiza la compra de boletos             |
| `generarReporteVentas`         | Genera reporte de ventas por localidad   |

## Pruebas de Consola con curl

Antes de ejecutar la app JavaFX, verifica que el servidor SOAP responde correctamente:

### 1. Login exitoso

```bash
curl -s -X POST "http://209.145.48.25:8086/TicketPremiumWebService/TicketPremiumWebService" \
  -H "Content-Type: text/xml; charset=utf-8" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:tns="http://soap.ticketpremium.espe.edu.ec/">
  <soap:Body>
    <tns:login>
      <username>MONSTER</username>
      <password>MONSTER9</password>
    </tns:login>
  </soap:Body>
</soap:Envelope>' | xmllint --format -
```

### 2. Listar partidos disponibles

```bash
curl -s -X POST "http://209.145.48.25:8086/TicketPremiumWebService/TicketPremiumWebService" \
  -H "Content-Type: text/xml; charset=utf-8" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:tns="http://soap.ticketpremium.espe.edu.ec/">
  <soap:Body>
    <tns:listarPartidosDisponibles/>
  </soap:Body>
</soap:Envelope>' | xmllint --format -
```

### 3. Listar localidades del partido 1

```bash
curl -s -X POST "http://209.145.48.25:8086/TicketPremiumWebService/TicketPremiumWebService" \
  -H "Content-Type: text/xml; charset=utf-8" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:tns="http://soap.ticketpremium.espe.edu.ec/">
  <soap:Body>
    <tns:listarLocalidadesDisponibles>
      <codigoPartido>1</codigoPartido>
    </tns:listarLocalidadesDisponibles>
  </soap:Body>
</soap:Envelope>' | xmllint --format -
```

### 4. Comprar 2 boletos GENERAL

```bash
curl -s -X POST "http://209.145.48.25:8086/TicketPremiumWebService/TicketPremiumWebService" \
  -H "Content-Type: text/xml; charset=utf-8" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:tns="http://soap.ticketpremium.espe.edu.ec/">
  <soap:Body>
    <tns:comprarBoletos>
      <username>MONSTER</username>
      <password>MONSTER9</password>
      <codigoPartido>1</codigoPartido>
      <codigoLocalidad>GENERAL</codigoLocalidad>
      <cantidad>2</cantidad>
    </tns:comprarBoletos>
  </soap:Body>
</soap:Envelope>' | xmllint --format -
```

### 5. Generar reporte de ventas

```bash
curl -s -X POST "http://209.145.48.25:8086/TicketPremiumWebService/TicketPremiumWebService" \
  -H "Content-Type: text/xml; charset=utf-8" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:tns="http://soap.ticketpremium.espe.edu.ec/">
  <soap:Body>
    <tns:generarReporteVentas>
      <codigoPartido>1</codigoPartido>
    </tns:generarReporteVentas>
  </soap:Body>
</soap:Envelope>' | xmllint --format -
```

## Partidos Disponibles

| Codigo | Equipo Local            | Equipo Visita         | Estadio                          |
| ------ | ----------------------- | --------------------- | -------------------------------- |
| 1      | Liga de Quito           | Barcelona SC          | Estadio Rodrigo Paz Delgado      |
| 2      | Emelec                  | Aucas                 | Estadio George Capwell           |
| 3      | Independiente del Valle | El Nacional           | Estadio Banco Guayaquil          |
| 4      | Deportivo Cuenca        | Delfin SC             | Estadio Alejandro Serrano Aguilar|
| 5      | Universidad Catolica    | Mushuc Runa           | Estadio Olimpico Atahualpa       |

## Localidades y Precios

| Codigo Localidad | Precio  |
| ---------------- | ------- |
| GENERAL          | $10.00  |
| GENERAL VISITA   | $10.00  |
| TRIBUNA          | $20.00  |
| PALCO            | $35.00  |

## Puertos del Servidor

| Servicio       | Puerto |
| -------------- | ------ |
| Payara (app)   | 8086   |
| Payara (admin) | 48486  |
| PostgreSQL     | 5436   |

## Solucion de Problemas

### La app no inicia o pantalla en blanco

Verifica que el servidor remoto responda:

```bash
curl -s "http://209.145.48.25:8086/TicketPremiumWebService/TicketPremiumWebService?wsdl" | head -5
```

### Error de conexion SOAP

Los logs de la consola muestran el XML enviado y recibido. Busca:
- `[SOAP] Endpoint` → debe ser `209.145.48.25:8086`
- `[SOAP] HTTP Response Code` → debe ser `200`
- `[SOAP] ERROR` → indica el problema especifico

### Error de compilacion JavaFX

Asegurate de usar Java 21:

```bash
java -version
mvn -version
```
