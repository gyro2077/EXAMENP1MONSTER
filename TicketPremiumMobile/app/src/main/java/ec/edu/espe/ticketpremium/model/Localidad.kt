package ec.edu.espe.ticketpremium.model

data class Localidad(
    val codigoLocalidad: String,
    val disponibilidad: Int,
    val precio: Double
) {
    val precioFormateado: String
        get() = "$%.2f".format(precio)
}
