package ec.edu.espe.ticketpremium.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ec.edu.espe.ticketpremium.model.Factura
import ec.edu.espe.ticketpremium.model.Localidad
import ec.edu.espe.ticketpremium.model.Partido
import ec.edu.espe.ticketpremium.viewmodel.TicketViewModel
import ec.edu.espe.ticketpremium.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun CompraScreen(
    partido: Partido,
    onBack: () -> Unit,
    viewModel: TicketViewModel = viewModel()
) {
    val localidadesState by viewModel.localidadesState.collectAsState()
    val compraState by viewModel.compraState.collectAsState()
    var selectedLocalidad by remember { mutableStateOf<Localidad?>(null) }
    var cantidad by remember { mutableIntStateOf(1) }
    var showComprobante by remember { mutableStateOf(false) }
    var ultimaFactura by remember { mutableStateOf<Factura?>(null) }

    LaunchedEffect(partido.codigo) {
        viewModel.cargarLocalidades(partido.codigo)
        viewModel.resetCompraState()
        showComprobante = false
        ultimaFactura = null
        selectedLocalidad = null
        cantidad = 1
    }

    LaunchedEffect(compraState) {
        if (compraState is UiState.Success) {
            ultimaFactura = (compraState as UiState.Success<Factura>).data
            showComprobante = true
            viewModel.resetCompraState()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Comprar Boletos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        if (showComprobante && ultimaFactura != null) {
            ComprobanteScreen(factura = ultimaFactura!!, onBack = onBack)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = partido.nombreCompleto,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${partido.lugar} - ${partido.fecha}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Selecciona una localidad:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                when (val state = localidadesState) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    is UiState.Error -> {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.cargarLocalidades(partido.codigo) }) {
                            Text("Reintentar")
                        }
                    }
                    is UiState.Success -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(state.data) { localidad ->
                                LocalidadItem(
                                    localidad = localidad,
                                    isSelected = selectedLocalidad?.codigoLocalidad == localidad.codigoLocalidad,
                                    onClick = {
                                        selectedLocalidad = localidad
                                        cantidad = 1
                                    }
                                )
                            }
                        }
                    }
                    else -> {}
                }

                if (selectedLocalidad != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cantidad:",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = { if (cantidad > 1) cantidad-- },
                                enabled = cantidad > 1
                            ) {
                                Text("-")
                            }
                            Text(
                                text = "$cantidad",
                                modifier = Modifier.padding(horizontal = 24.dp),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedButton(
                                onClick = {
                                    if (cantidad < (selectedLocalidad?.disponibilidad ?: 1)) cantidad++
                                },
                                enabled = cantidad < (selectedLocalidad?.disponibilidad ?: 1)
                            ) {
                                Text("+")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Disponibles: ${selectedLocalidad?.disponibilidad}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val subtotal = selectedLocalidad!!.precio * cantidad
                    val iva = subtotal * 0.15
                    val total = subtotal + iva

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal:")
                                Text("$${"%.2f".format(subtotal)}")
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("IVA (15%):")
                                Text("$${"%.2f".format(iva)}")
                            }
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total:", fontWeight = FontWeight.Bold)
                                Text("$${"%.2f".format(total)}", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            selectedLocalidad?.let { loc ->
                                viewModel.comprarBoletos(partido.codigo, loc.codigoLocalidad, cantidad)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = compraState !is UiState.Loading
                    ) {
                        if (compraState is UiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Confirmar Compra", modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }

                    if (compraState is UiState.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (compraState as UiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LocalidadItem(
    localidad: Localidad,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = localidad.codigoLocalidad,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${localidad.disponibilidad} disponibles",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = localidad.precioFormateado,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ComprobanteScreen(
    factura: Factura,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Compra Exitosa",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "COMPROBANTE DE COMPRA",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                ComprobanteRow("Factura Nro:", factura.facturaId?.toString() ?: "N/A")
                ComprobanteRow("Cliente:", factura.clienteNombre ?: "N/A")
                ComprobanteRow("Cedula:", factura.clienteCedula ?: "N/A")
                ComprobanteRow("Fecha:", factura.fechaCompra)

                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                ComprobanteRow("Partido:", factura.nombrePartido ?: "N/A")
                ComprobanteRow("Localidad:", factura.codigoLocalidad ?: "N/A")
                ComprobanteRow("Cantidad:", factura.cantidad?.toString() ?: "0")
                ComprobanteRow("Precio unitario:", "$${"%.2f".format(factura.precioUnitario ?: 0.0)}")

                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                ComprobanteRow("Subtotal:", "$${"%.2f".format(factura.subtotal)}")
                ComprobanteRow("IVA (15%):", "$${"%.2f".format(factura.iva)}")

                Spacer(modifier = Modifier.height(8.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("TOTAL:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "$${"%.2f".format(factura.total)}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver a Partidos", modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
fun ComprobanteRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
