La empresa intermediadora “TicketPremium” se encarga de comercializar boletos para espectáculos deportivos, principalmente partidos de fútbol.
La federación de futbol ha contratado a la empresa “TicketPremium” para qué sea la encargada de manejar la venta de boletos de todos los partidos de fútbol del campeonato nacional.
La empresa “TicketPremium” para la comercialización de los boletos, se conecta mediante un servicio web con la federación de fútbol; para obtener información referente a los encuentros deportivos, precios y disponibilidad de cada una de las diferentes localidades.
Se debe tener en cuenta que los partidos de fútbol de acuerdo al estadio donde se realizan y a los equipos que juegan ofertan diferentes tipos de localidades.
Como parte de este examen complexivo, se pide realizar las siguientes funcionalidades:

### Aplicación “Federación de Fútbol”

Implementar la tabla “PARTIDO_FUTBOL” la cual contendrá los siguientes campos:
* **CODIGO:** Código identificador de un espectáculo.
* **EQUIPO_LOCAL:** Nombre del equipo local
* **EQUIPO_VISTITA:** Nombre del equipo visitante.
* **FECHA:** Fecha y Hora de realización del partido de fútbol.
* **LUGAR:** Descripción del lugar donde se realizará el partido

Implementar la tabla “LOCALIDAD_PARTIDO”, esta tabla se utiliza para definir la disponibilidad y costo de cada una de las diferentes localidades para un partido determinado; esta tabla podría tener las siguientes columnas principales.
* **CODIGO_LOCALIDAD:** Código identificador de la localidad (PALCO, TRIBUNA, GENERAL, GENERAL VISITA, etc.)
* **DISPONIBILIDAD:** Número de boletos disponibles para esa localidad.
* **PRECIO:** Precio unitario de cada localidad.

*Nota: La tabla PARTIDO_FUTBOL Y LOCALIDAD_PARTIDO deben estar relacionadas.*

1. **Web Service que despliega los partidos de fútbol disponibles.** Los partidos de fútbol disponibles son los que cumplen con la siguiente condición: FECHA mayor o igual que la fecha actual.
2. **Web Service que despliega las diferentes localidades con su precio.** Solo se deberán desplegar las localidades cuya DISPONIBILIDAD sea mayor a cero. Este web service debe recibir como parámetro el código del partido para el cual se desea obtener las localidades.
3. **Web Service que decrementa el valor de la DISPONIBILIDAD.** Este Web Service será invocado cuando el cliente compre un boleto de determinada localidad.

### Aplicación Web “TicketPremium”

1. **Funcionalidad para el despliegue de partidos de futbol disponibles.** Para esto deberá crear un cliente que invoque el Web Service especificado en el punto 3 de la aplicación anterior.
2. **Funcionalidad que despliegue las localidades con su respectivo precio** para el espectáculo seleccionado en la funcionalidad 1.
3. **Funcionalidad que registre la compra de boletos y genere la factura final** de acuerdo a la selección del usuario en la funcionalidad 2. Recuerde que debe incluir el valor de IVA al valor de la factura.
4. **Reporte “Resumen de Ventas de un Partido”**, se debe generar un reporte que presente de manera resumida la información de las ventas realizadas, como se muestra a continuación:

**Partido:** Equipo A vs Equipo B  
**Fecha:** 11-Abril-2015  

| Localidad | Vendidos | Total recaudado |
| :--- | :---: | :---: |
| GENERAL | 1456 | 8500 |
| TRIBUNA | 300 | 6000 |
| PALCO | 50 | 1500 |

*Nota: Para el diseño del reporte puede hacerlo como una simple interfaz web que presenta la tabla de resultados. No es necesario que programe la impresión o exportación del mismo, ni el uso de una herramienta especializada de generación de reportes.*

### Restricciones

* Se debe evidenciar que el sistema de TICKET PREMIUM y el de la Federación de Fútbol son dos diferentes sistemas.
* La integración de los sistemas se la debe realizar utilizando Web Services que utilicen el protocolo SOAP.

### Rúbrica

| Ítem | Puntaje |
| :--- | :---: |
| Implementación de tabla PARTIDO_FUTBOL con al menos 5 registros para pruebas | 0.5 |
| Implementación de tabla LOCALIDAD_PARTIDO con al menos 20 registros para pruebas | 0.5 |
| Web Service que retorna los partidos de fútbol disponibles | 1.0 |
| Web Service que retorna las localidades disponibles con su valor. | 1.0 |
| Funcionalidad que despliega los partidos de fútbol disponibles | 1.0 |
| Funcionalidad que despliega las localidades disponibles para un determinado partido de fútbol. | 1.0 |
| Diseño e Implementación de tablas para registro de la factura de venta de los boletos | 0.5 |
| Web Service que decrementa la disponibilidad de una localidad cuando se realiza una compra. | 0.5 |
| Interfaz Consola, Escritorio, Web y Móvil que permite la compra de boletos y registra los valores calculados de impuestos y total final del pedido. | 3.0 |
| Reporte: “Resumen de Ventas de un Partido” | 1.0 |
| **Total** | **10.0** |

### Importante:
* El evaluador solo tomará en cuenta para la calificación lo que se especifica en la rúbrica del examen.
* Si el estudiante realiza funcionalidades adicionales a las solicitadas, estas no serán tomadas en cuenta y no constituirán un valor adicional a la nota final.
* El estudiante es libre de seleccionar la plataforma, “frameworks” o lenguaje que considere necesarios para resolver el examen. Adicionalmente el estudiante puede utilizar el motor de base de datos con el que se sienta más cómodo trabajando.
