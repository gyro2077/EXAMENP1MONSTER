package ec.edu.espe.ticketpremium.model

data class Factura(
    val exitoso: Boolean,
    val mensaje: String,
    val facturaId: Int?,
    val clienteNombre: String?,
    val clienteCedula: String?,
    val fechaCompra: String,
    val subtotal: Double,
    val iva: Double,
    val total: Double,
    val codigoPartido: Int?,
    val nombrePartido: String?,
    val codigoLocalidad: String?,
    val cantidad: Int?,
    val precioUnitario: Double?
)
