package ec.edu.espe.ticketpremium.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ec.edu.espe.ticketpremium.model.Partido
import ec.edu.espe.ticketpremium.model.ReporteVenta
import ec.edu.espe.ticketpremium.viewmodel.TicketViewModel
import ec.edu.espe.ticketpremium.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun ReporteScreen(
    partido: Partido,
    onBack: () -> Unit,
    viewModel: TicketViewModel = viewModel()
) {
    val reporteState by viewModel.reporteState.collectAsState()

    LaunchedEffect(partido.codigo) {
        viewModel.generarReporte(partido.codigo)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Reporte de Ventas") },
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
                text = partido.fecha,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (val state = reporteState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is UiState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.generarReporte(partido.codigo) }) {
                            Text("Reintentar")
                        }
                    }
                }
                is UiState.Success -> {
                    ReporteTable(reportes = state.data)
                }
                else -> {}
            }
        }
    }
}

@Composable
fun ReporteTable(reportes: List<ReporteVenta>) {
    var totalVendidos = 0
    var totalRecaudado = 0.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Localidad",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(2f)
                )
                Text(
                    text = "Vendidos",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Total Recaudado",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1.5f),
                    textAlign = TextAlign.End
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            reportes.forEach { reporte ->
                totalVendidos += reporte.cantidadTotalVendida
                totalRecaudado += reporte.totalRecaudado

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = reporte.codigoLocalidad,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(2f)
                    )
                    Text(
                        text = "${reporte.cantidadTotalVendida}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = reporte.totalRecaudadoFormateado,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1.5f),
                        textAlign = TextAlign.End
                    )
                }
                Divider()
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "TOTAL",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(2f)
                )
                Text(
                    text = "$totalVendidos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "$${"%.2f".format(totalRecaudado)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1.5f),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}
