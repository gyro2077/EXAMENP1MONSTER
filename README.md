# TicketPremium Web - SOAP Server

Servidor SOAP para la gestión de compra de boletos de fútbol. Actúa como intermediario entre el cliente web y el servidor de la **Federación de Fútbol** (`http://209.145.48.25:8085`).

## Información del Servicio

| Propiedad | Valor |
|---|---|
| **WSDL** | `http://localhost:8086/TicketPremiumWebService/TicketPremiumWebService?wsdl` |
| **Endpoint SOAP** | `http://localhost:8086/TicketPremiumWebService/TicketPremiumWebService` |
| **Namespace** | `http://soap.ticketpremium.espe.edu.ec/` |
| **Tecnología** | Jakarta EE 10, JAX-WS, Payara Server 6 |
| **Base de datos** | PostgreSQL (`ticketpremium_db`) |

## Operaciones SOAP Disponibles

El servicio expone **5 operaciones**:

---

### 1. `login`

Valida las credenciales del usuario contra la base de datos local.

**Parámetros:**
- `username` (String)
- `password` (String)

**Retorna:** `SesionDTO`
- `exitoso` (Boolean)
- `mensaje` (String)
- `username` (String)
- `clienteId` (Integer) - null si no tiene cliente asociado

**Ejemplo de petición:**
```xml
POST http://localhost:8086/TicketPremiumWebService/TicketPremiumWebService
Content-Type: text/xml; charset=utf-8

<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
               xmlns:tns="http://soap.ticketpremium.espe.edu.ec/">
  <soap:Body>
    <tns:login>
      <username>MONSTER</username>
      <password>MONSTER9</password>
    </tns:login>
  </soap:Body>
</soap:Envelope>
```

**Respuesta exitosa:**
```xml
<ns2:loginResponse xmlns:ns2="http://soap.ticketpremium.espe.edu.ec/">
  <return>
    <exitoso>true</exitoso>
    <mensaje>Autenticacion exitosa</mensaje>
    <username>MONSTER</username>
    <clienteId>1</clienteId>
  </return>
</ns2:loginResponse>
```

**Respuesta fallida:**
```xml
<ns2:loginResponse xmlns:ns2="http://soap.ticketpremium.espe.edu.ec/">
  <return>
    <exitoso>false</exitoso>
    <mensaje>Credenciales invalidas</mensaje>
    <username></username>
    <clienteId></clienteId>
  </return>
</ns2:loginResponse>
```

---

### 2. `listarPartidosDisponibles`

Consulta los partidos disponibles en el servidor de la Federación. No requiere autenticación.

**Parámetros:** Ninguno

**Retorna:** `List<PartidoDTO>`
- `codigo` (Integer)
- `equipoLocal` (String)
- `equipoVisita` (String)
- `fecha` (LocalDateTime)
- `lugar` (String)

**Ejemplo de petición:**
```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
               xmlns:tns="http://soap.ticketpremium.espe.edu.ec/">
  <soap:Body>
    <tns:listarPartidosDisponibles/>
  </soap:Body>
</soap:Envelope>
```

**Respuesta:**
```xml
<ns2:listarPartidosDisponiblesResponse xmlns:ns2="http://soap.ticketpremium.espe.edu.ec/">
  <return>
    <codigo>1</codigo>
    <equipoLocal>Liga de Quito</equipoLocal>
    <equipoVisita>Barcelona SC</equipoVisita>
    <fecha/>
    <lugar>Estadio Rodrigo Paz Delgado</lugar>
  </return>
  <return>
    <codigo>2</codigo>
    <equipoLocal>Emelec</equipoLocal>
    <equipoVisita>Aucas</equipoVisita>
    <fecha/>
    <lugar>Estadio George Capwell</lugar>
  </return>
  <!-- ... más partidos ... -->
</ns2:listarPartidosDisponiblesResponse>
```

---

### 3. `listarLocalidadesDisponibles`

Consulta las localidades (tipos de asiento) disponibles para un partido específico.

**Parámetros:**
- `codigoPartido` (Integer)

**Retorna:** `List<LocalidadDTO>`
- `codigoLocalidad` (String)
- `disponibilidad` (Integer)
- `precio` (BigDecimal)

**Ejemplo de petición:**
```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
               xmlns:tns="http://soap.ticketpremium.espe.edu.ec/">
  <soap:Body>
    <tns:listarLocalidadesDisponibles>
      <codigoPartido>1</codigoPartido>
    </tns:listarLocalidadesDisponibles>
  </soap:Body>
</soap:Envelope>
```

**Respuesta:**
```xml
<ns2:listarLocalidadesDisponiblesResponse xmlns:ns2="http://soap.ticketpremium.espe.edu.ec/">
  <return>
    <codigoLocalidad>GENERAL</codigoLocalidad>
    <disponibilidad>486</disponibilidad>
    <precio>10.00</precio>
  </return>
  <return>
    <codigoLocalidad>PALCO</codigoLocalidad>
    <disponibilidad>100</disponibilidad>
    <precio>35.00</precio>
  </return>
  <return>
    <codigoLocalidad>TRIBUNA</codigoLocalidad>
    <disponibilidad>300</disponibilidad>
    <precio>20.00</precio>
  </return>
  <return>
    <codigoLocalidad>GENERAL VISITA</codigoLocalidad>
    <disponibilidad>150</disponibilidad>
    <precio>10.00</precio>
  </return>
</ns2:listarLocalidadesDisponiblesResponse>
```

---

### 4. `comprarBoletos`

Realiza la compra de boletos. Valida credenciales, calcula subtotal/IVA/total, decrementa disponibilidad en la Federación y guarda la factura en la BD local.

**Parámetros:**
- `username` (String)
- `password` (String)
- `codigoPartido` (Integer)
- `codigoLocalidad` (String)
- `cantidad` (Integer)

**Retorna:** `ComprobanteDTO`
- `exitoso` (Boolean)
- `mensaje` (String)
- `facturaId` (Integer)
- `clienteNombre` (String)
- `clienteCedula` (String)
- `fechaCompra` (LocalDateTime)
- `subtotal` (BigDecimal)
- `iva` (BigDecimal) - 15%
- `total` (BigDecimal)
- `codigoPartido` (Integer)
- `nombrePartido` (String)
- `codigoLocalidad` (String)
- `cantidad` (Integer)
- `precioUnitario` (BigDecimal)

**Ejemplo de petición:**
```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
               xmlns:tns="http://soap.ticketpremium.espe.edu.ec/">
  <soap:Body>
    <tns:comprarBoletos>
      <username>MONSTER</username>
      <password>MONSTER9</password>
      <codigoPartido>1</codigoPartido>
      <codigoLocalidad>GENERAL</codigoLocalidad>
      <cantidad>2</cantidad>
    </tns:comprarBoletos>
  </soap:Body>
</soap:Envelope>
```

**Respuesta exitosa:**
```xml
<ns2:comprarBoletosResponse xmlns:ns2="http://soap.ticketpremium.espe.edu.ec/">
  <return>
    <exitoso>true</exitoso>
    <mensaje>Compra realizada exitosamente</mensaje>
    <facturaId>2</facturaId>
    <clienteNombre>Monster User</clienteNombre>
    <clienteCedula>1712345678</clienteCedula>
    <fechaCompra/>
    <subtotal>20.00</subtotal>
    <iva>3.0000</iva>
    <total>23.0000</total>
    <codigoPartido>1</codigoPartido>
    <nombrePartido>Liga de Quito vs Barcelona SC</nombrePartido>
    <codigoLocalidad>GENERAL</codigoLocalidad>
    <cantidad>2</cantidad>
    <precioUnitario>10.00</precioUnitario>
  </return>
</ns2:comprarBoletosResponse>
```

**Respuestas de error posibles:**

| Escenario | exitoso | mensaje |
|---|---|---|
| Credenciales inválidas | `false` | `Credenciales invalidas` |
| Sin cliente asociado | `false` | `No se encontro un cliente asociado a este usuario` |
| Parámetros inválidos | `false` | `Parametros de compra invalidos` |
| Localidad no encontrada | `false` | `Localidad no encontrada o sin disponibilidad` |
| Sin stock suficiente | `false` | `No existe disponibilidad suficiente.` |

---

### 5. `generarReporteVentas`

Genera un reporte de ventas agrupado por localidad para un partido específico. Consulta la base de datos local de TicketPremium.

**Parámetros:**
- `codigoPartido` (Integer)

**Retorna:** `List<ReporteDTO>`
- `codigoPartido` (Integer)
- `nombrePartido` (String)
- `codigoLocalidad` (String)
- `cantidadTotalVendida` (Integer)
- `totalRecaudado` (BigDecimal)

**Ejemplo de petición:**
```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
               xmlns:tns="http://soap.ticketpremium.espe.edu.ec/">
  <soap:Body>
    <tns:generarReporteVentas>
      <codigoPartido>1</codigoPartido>
    </tns:generarReporteVentas>
  </soap:Body>
</soap:Envelope>
```

**Respuesta:**
```xml
<ns2:generarReporteVentasResponse xmlns:ns2="http://soap.ticketpremium.espe.edu.ec/">
  <return>
    <codigoPartido>1</codigoPartido>
    <nombrePartido>Liga de Quito vs Barcelona SC</nombrePartido>
    <codigoLocalidad>GENERAL</codigoLocalidad>
    <cantidadTotalVendida>2</cantidadTotalVendida>
    <totalRecaudado>20.00</totalRecaudado>
  </return>
</ns2:generarReporteVentasResponse>
```

---

## Guía de Pruebas con curl

### Prueba 1: Login exitoso
```bash
curl -s -X POST "http://localhost:8086/TicketPremiumWebService/TicketPremiumWebService" \
  -H "Content-Type: text/xml; charset=utf-8" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:tns="http://soap.ticketpremium.espe.edu.ec/">
  <soap:Body>
    <tns:login>
      <username>MONSTER</username>
      <password>MONSTER9</password>
    </tns:login>
  </soap:Body>
</soap:Envelope>'
```

### Prueba 2: Login fallido
```bash
curl -s -X POST "http://localhost:8086/TicketPremiumWebService/TicketPremiumWebService" \
  -H "Content-Type: text/xml; charset=utf-8" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:tns="http://soap.ticketpremium.espe.edu.ec/">
  <soap:Body>
    <tns:login>
      <username>INVALIDO</username>
      <password>WRONG</password>
    </tns:login>
  </soap:Body>
</soap:Envelope>'
```

### Prueba 3: Listar partidos
```bash
curl -s -X POST "http://localhost:8086/TicketPremiumWebService/TicketPremiumWebService" \
  -H "Content-Type: text/xml; charset=utf-8" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:tns="http://soap.ticketpremium.espe.edu.ec/">
  <soap:Body>
    <tns:listarPartidosDisponibles/>
  </soap:Body>
</soap:Envelope>'
```

### Prueba 4: Listar localidades del partido 1
```bash
curl -s -X POST "http://localhost:8086/TicketPremiumWebService/TicketPremiumWebService" \
  -H "Content-Type: text/xml; charset=utf-8" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:tns="http://soap.ticketpremium.espe.edu.ec/">
  <soap:Body>
    <tns:listarLocalidadesDisponibles>
      <codigoPartido>1</codigoPartido>
    </tns:listarLocalidadesDisponibles>
  </soap:Body>
</soap:Envelope>'
```

### Prueba 5: Comprar 2 boletos GENERAL para el partido 1
```bash
curl -s -X POST "http://localhost:8086/TicketPremiumWebService/TicketPremiumWebService" \
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
</soap:Envelope>'
```

### Prueba 6: Comprar excediendo disponibilidad (debe fallar)
```bash
curl -s -X POST "http://localhost:8086/TicketPremiumWebService/TicketPremiumWebService" \
  -H "Content-Type: text/xml; charset=utf-8" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:tns="http://soap.ticketpremium.espe.edu.ec/">
  <soap:Body>
    <tns:comprarBoletos>
      <username>MONSTER</username>
      <password>MONSTER9</password>
      <codigoPartido>1</codigoPartido>
      <codigoLocalidad>PALCO</codigoLocalidad>
      <cantidad>9999</cantidad>
    </tns:comprarBoletos>
  </soap:Body>
</soap:Envelope>'
```

### Prueba 7: Generar reporte de ventas del partido 1
```bash
curl -s -X POST "http://localhost:8086/TicketPremiumWebService/TicketPremiumWebService" \
  -H "Content-Type: text/xml; charset=utf-8" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:tns="http://soap.ticketpremium.espe.edu.ec/">
  <soap:Body>
    <tns:generarReporteVentas>
      <codigoPartido>1</codigoPartido>
    </tns:generarReporteVentas>
  </soap:Body>
</soap:Envelope>'
```

---

## Datos de Prueba

### Usuario por defecto
| Campo | Valor |
|---|---|
| **username** | `MONSTER` |
| **password** | `MONSTER9` |
| **clienteId** | `1` |
| **nombre** | `Monster User` |
| **cedula** | `1712345678` |

### Partidos disponibles
| codigo | equipoLocal | equipoVisita | lugar |
|---|---|---|---|
| 1 | Liga de Quito | Barcelona SC | Estadio Rodrigo Paz Delgado |
| 2 | Emelec | Aucas | Estadio George Capwell |
| 3 | Independiente del Valle | El Nacional | Estadio Banco Guayaquil |
| 4 | Deportivo Cuenca | Delfín SC | Estadio Alejandro Serrano Aguilar |
| 5 | Universidad Católica | Mushuc Runa | Estadio Olímpico Atahualpa |

### Localidades (partido 1)
| codigoLocalidad | precio |
|---|---|
| GENERAL | 10.00 |
| GENERAL VISITA | 10.00 |
| TRIBUNA | 20.00 |
| PALCO | 35.00 |

---

## Flujo Típico de un Cliente Web

1. **Login** → El usuario ingresa credenciales, se llama a `login()`. Si `exitoso=true`, se guarda la sesión.
2. **Listar partidos** → Se llama a `listarPartidosDisponibles()` para mostrar los partidos en una tabla/select.
3. **Seleccionar partido** → El usuario elige un partido, se llama a `listarLocalidadesDisponibles(codigoPartido)` para mostrar las localidades con precio y disponibilidad.
4. **Seleccionar localidad y cantidad** → El usuario elige localidad y cantidad de boletos.
5. **Comprar** → Se llama a `comprarBoletos(username, password, codigoPartido, codigoLocalidad, cantidad)`. Si `exitoso=true`, se muestra el comprobante con subtotal, IVA y total.
6. **Ver reportes** → Se llama a `generarReporteVentas(codigoPartido)` para mostrar las ventas agrupadas por localidad.

---

## Despliegue

```bash
# Construir
mvn clean package -DskipTests

# Levantar servicios (PostgreSQL + Payara)
docker compose up -d --build

# El WSDL estará disponible en:
# http://localhost:8086/TicketPremiumWebService/TicketPremiumWebService?wsdl
```

## Puertos

| Servicio | Puerto |
|---|---|
| Payara (app) | 8086 |
| Payara (admin) | 48486 |
| PostgreSQL | 5436 |
