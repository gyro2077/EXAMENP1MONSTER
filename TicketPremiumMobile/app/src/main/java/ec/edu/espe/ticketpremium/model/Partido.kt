package ec.edu.espe.ticketpremium.model

data class Partido(
    val codigo: Int,
    val equipoLocal: String,
    val equipoVisita: String,
    val fecha: String,
    val lugar: String
) {
    val nombreCompleto: String
        get() = "$equipoLocal vs $equipoVisita"
}
