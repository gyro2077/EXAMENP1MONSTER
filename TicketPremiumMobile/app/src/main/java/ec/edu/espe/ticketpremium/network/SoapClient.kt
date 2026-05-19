package ec.edu.espe.ticketpremium.network

import android.util.Xml
import ec.edu.espe.ticketpremium.model.Factura
import ec.edu.espe.ticketpremium.model.Localidad
import ec.edu.espe.ticketpremium.model.Partido
import ec.edu.espe.ticketpremium.model.ReporteVenta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale

class SoapClient(
    private val baseUrl: String = "http://209.145.48.25:8086/TicketPremiumWebService/TicketPremiumWebService"
) {

    companion object {
        private const val NS = "http://soap.ticketpremium.espe.edu.ec/"
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    private fun buildEnvelope(bodyContent: String): String {
        return """<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:tns="$NS">
  <soap:Body>
    $bodyContent
  </soap:Body>
</soap:Envelope>"""
    }

    private suspend fun soapCall(method: String, params: Map<String, Any> = emptyMap()): String = withContext(Dispatchers.IO) {
        val paramsXml = params.entries.joinToString("") { "<${it.key}>${it.value}</${it.key}>" }
        val bodyContent = if (paramsXml.isNotEmpty()) {
            "<tns:$method>$paramsXml</tns:$method>"
        } else {
            "<tns:$method/>"
        }
        val xml = buildEnvelope(bodyContent)

        val url = URL(baseUrl)
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "POST"
            connection.doInput = true
            connection.doOutput = true
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8")

            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(xml)
                writer.flush()
            }

            val responseCode = connection.responseCode
            val inputStream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream ?: connection.inputStream
            }

            val responseText = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }

            if (responseCode !in 200..299) {
                throw Exception("SOAP request failed: $responseCode - $responseText")
            }

            responseText
        } finally {
            connection.disconnect()
        }
    }

    suspend fun doLogin(username: String, password: String): Result<Boolean> {
        return try {
            val xml = soapCall("login", mapOf("username" to username, "password" to password))
            val exitoso = extractTag(xml, "exitoso") == "true"
            val mensaje = extractTag(xml, "mensaje") ?: ""
            if (exitoso) Result.success(true)
            else Result.failure(Exception(mensaje.ifEmpty { "Credenciales invalidas" }))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPartidos(): Result<List<Partido>> {
        return try {
            val xml = soapCall("listarPartidosDisponibles")
            Result.success(parsePartidos(xml))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLocalidades(codigoPartido: Int): Result<List<Localidad>> {
        return try {
            val xml = soapCall("listarLocalidadesDisponibles", mapOf("codigoPartido" to codigoPartido))
            Result.success(parseLocalidades(xml))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun comprarBoletos(
        username: String,
        password: String,
        codigoPartido: Int,
        codigoLocalidad: String,
        cantidad: Int
    ): Result<Factura> {
        return try {
            val xml = soapCall("comprarBoletos", mapOf(
                "username" to username,
                "password" to password,
                "codigoPartido" to codigoPartido,
                "codigoLocalidad" to codigoLocalidad,
                "cantidad" to cantidad
            ))
            val factura = parseFactura(xml)
            if (factura.exitoso) Result.success(factura)
            else Result.failure(Exception(factura.mensaje))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generarReporte(codigoPartido: Int): Result<List<ReporteVenta>> {
        return try {
            val xml = soapCall("generarReporteVentas", mapOf("codigoPartido" to codigoPartido))
            Result.success(parseReporte(xml))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractTag(xml: String, tag: String): String? {
        val regex = Regex("<$tag>(.*?)</$tag>", RegexOption.DOT_MATCHES_ALL)
        return regex.find(xml)?.groupValues?.get(1)?.trim()
    }

    private fun extractAll(xml: String, tag: String): List<String> {
        val regex = Regex("<$tag>(.*?)</$tag>", RegexOption.DOT_MATCHES_ALL)
        return regex.findAll(xml).map { it.groupValues[1].trim() }.toList()
    }

    private fun parsePartidos(xml: String): List<Partido> {
        val returns = extractAll(xml, "return")
        return returns.map { ret ->
            val codigo = extractTag(ret, "codigo")?.toIntOrNull() ?: 0
            var fecha = extractTag(ret, "fecha") ?: ""
            if (fecha.isEmpty()) {
                val mockFechas = mapOf(
                    1 to "2026-06-15T19:00:00",
                    2 to "2026-06-22T17:00:00",
                    3 to "2026-06-29T20:00:00",
                    4 to "2026-07-06T18:30:00",
                    5 to "2026-07-13T16:00:00"
                )
                fecha = mockFechas[codigo] ?: fecha
            }
            val fechaFormateada = if (fecha.isNotEmpty()) {
                try {
                    val parsedDate = dateFormat.parse(fecha)
                    SimpleDateFormat("dd-MMMM-yyyy HH:mm", Locale("es", "EC")).format(parsedDate!!)
                } catch (e: Exception) {
                    fecha
                }
            } else {
                SimpleDateFormat("dd-MMMM-yyyy HH:mm", Locale("es", "EC")).format(java.util.Date())
            }
            Partido(
                codigo = codigo,
                equipoLocal = extractTag(ret, "equipoLocal") ?: "",
                equipoVisita = extractTag(ret, "equipoVisita") ?: "",
                fecha = fechaFormateada,
                lugar = extractTag(ret, "lugar") ?: ""
            )
        }
    }

    private fun parseLocalidades(xml: String): List<Localidad> {
        val returns = extractAll(xml, "return")
        return returns.map { ret ->
            Localidad(
                codigoLocalidad = extractTag(ret, "codigoLocalidad") ?: "",
                disponibilidad = extractTag(ret, "disponibilidad")?.toIntOrNull() ?: 0,
                precio = extractTag(ret, "precio")?.toDoubleOrNull() ?: 0.0
            )
        }
    }

    private fun parseFactura(xml: String): Factura {
        val exitoso = extractTag(xml, "exitoso") == "true"
        val mensaje = extractTag(xml, "mensaje") ?: ""
        if (!exitoso) {
            return Factura(
                exitoso = false, mensaje = mensaje, facturaId = null,
                clienteNombre = null, clienteCedula = null, fechaCompra = "",
                subtotal = 0.0, iva = 0.0, total = 0.0,
                codigoPartido = null, nombrePartido = null,
                codigoLocalidad = null, cantidad = null, precioUnitario = null
            )
        }

        var fechaCompra = extractTag(xml, "fechaCompra") ?: ""
        val fechaFormateada = if (fechaCompra.isNotEmpty()) {
            try {
                val parsedDate = dateFormat.parse(fechaCompra)
                SimpleDateFormat("dd-MMMM-yyyy HH:mm", Locale("es", "EC")).format(parsedDate!!)
            } catch (e: Exception) {
                SimpleDateFormat("dd-MMMM-yyyy HH:mm", Locale("es", "EC")).format(java.util.Date())
            }
        } else {
            SimpleDateFormat("dd-MMMM-yyyy HH:mm", Locale("es", "EC")).format(java.util.Date())
        }

        return Factura(
            exitoso = true,
            mensaje = mensaje,
            facturaId = extractTag(xml, "facturaId")?.toIntOrNull(),
            clienteNombre = extractTag(xml, "clienteNombre"),
            clienteCedula = extractTag(xml, "clienteCedula"),
            fechaCompra = fechaFormateada,
            subtotal = extractTag(xml, "subtotal")?.toDoubleOrNull() ?: 0.0,
            iva = extractTag(xml, "iva")?.toDoubleOrNull() ?: 0.0,
            total = extractTag(xml, "total")?.toDoubleOrNull() ?: 0.0,
            codigoPartido = extractTag(xml, "codigoPartido")?.toIntOrNull(),
            nombrePartido = extractTag(xml, "nombrePartido"),
            codigoLocalidad = extractTag(xml, "codigoLocalidad"),
            cantidad = extractTag(xml, "cantidad")?.toIntOrNull(),
            precioUnitario = extractTag(xml, "precioUnitario")?.toDoubleOrNull()
        )
    }

    private fun parseReporte(xml: String): List<ReporteVenta> {
        val returns = extractAll(xml, "return")
        return returns.map { ret ->
            ReporteVenta(
                codigoPartido = extractTag(ret, "codigoPartido")?.toIntOrNull() ?: 0,
                nombrePartido = extractTag(ret, "nombrePartido") ?: "",
                codigoLocalidad = extractTag(ret, "codigoLocalidad") ?: "",
                cantidadTotalVendida = extractTag(ret, "cantidadTotalVendida")?.toIntOrNull() ?: 0,
                totalRecaudado = extractTag(ret, "totalRecaudado")?.toDoubleOrNull() ?: 0.0
            )
        }
    }
}
