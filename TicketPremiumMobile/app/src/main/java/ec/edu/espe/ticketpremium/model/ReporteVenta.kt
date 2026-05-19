package ec.edu.espe.ticketpremium.model

data class ReporteVenta(
    val codigoPartido: Int,
    val nombrePartido: String,
    val codigoLocalidad: String,
    val cantidadTotalVendida: Int,
    val totalRecaudado: Double
) {
    val totalRecaudadoFormateado: String
        get() = "$%.2f".format(totalRecaudado)
}
