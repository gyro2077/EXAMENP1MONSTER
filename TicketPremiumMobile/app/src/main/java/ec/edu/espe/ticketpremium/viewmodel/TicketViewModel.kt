package ec.edu.espe.ticketpremium.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ec.edu.espe.ticketpremium.model.Factura
import ec.edu.espe.ticketpremium.model.Localidad
import ec.edu.espe.ticketpremium.model.Partido
import ec.edu.espe.ticketpremium.model.ReporteVenta
import ec.edu.espe.ticketpremium.network.SoapClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class TicketViewModel : ViewModel() {

    private val soapClient = SoapClient()

    private val _loginState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val loginState: StateFlow<UiState<Boolean>> = _loginState.asStateFlow()

    private val _partidosState = MutableStateFlow<UiState<List<Partido>>>(UiState.Idle)
    val partidosState: StateFlow<UiState<List<Partido>>> = _partidosState.asStateFlow()

    private val _localidadesState = MutableStateFlow<UiState<List<Localidad>>>(UiState.Idle)
    val localidadesState: StateFlow<UiState<List<Localidad>>> = _localidadesState.asStateFlow()

    private val _compraState = MutableStateFlow<UiState<Factura>>(UiState.Idle)
    val compraState: StateFlow<UiState<Factura>> = _compraState.asStateFlow()

    private val _reporteState = MutableStateFlow<UiState<List<ReporteVenta>>>(UiState.Idle)
    val reporteState: StateFlow<UiState<List<ReporteVenta>>> = _reporteState.asStateFlow()

    var username: String = ""
    var password: String = ""

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            this@TicketViewModel.username = username
            this@TicketViewModel.password = password
            soapClient.doLogin(username, password)
                .onSuccess { _loginState.value = UiState.Success(true) }
                .onFailure { _loginState.value = UiState.Error(it.message ?: "Error de conexion") }
        }
    }

    fun cargarPartidos() {
        viewModelScope.launch {
            _partidosState.value = UiState.Loading
            soapClient.getPartidos()
                .onSuccess { _partidosState.value = UiState.Success(it) }
                .onFailure { _partidosState.value = UiState.Error(it.message ?: "Error al cargar partidos") }
        }
    }

    fun cargarLocalidades(codigoPartido: Int) {
        viewModelScope.launch {
            _localidadesState.value = UiState.Loading
            soapClient.getLocalidades(codigoPartido)
                .onSuccess { _localidadesState.value = UiState.Success(it) }
                .onFailure { _localidadesState.value = UiState.Error(it.message ?: "Error al cargar localidades") }
        }
    }

    fun comprarBoletos(codigoPartido: Int, codigoLocalidad: String, cantidad: Int) {
        viewModelScope.launch {
            _compraState.value = UiState.Loading
            soapClient.comprarBoletos(username, password, codigoPartido, codigoLocalidad, cantidad)
                .onSuccess { _compraState.value = UiState.Success(it) }
                .onFailure { _compraState.value = UiState.Error(it.message ?: "Error al comprar boletos") }
        }
    }

    fun generarReporte(codigoPartido: Int) {
        viewModelScope.launch {
            _reporteState.value = UiState.Loading
            soapClient.generarReporte(codigoPartido)
                .onSuccess { _reporteState.value = UiState.Success(it) }
                .onFailure { _reporteState.value = UiState.Error(it.message ?: "Error al generar reporte") }
        }
    }

    fun resetLoginState() {
        _loginState.value = UiState.Idle
    }

    fun resetCompraState() {
        _compraState.value = UiState.Idle
    }

    fun resetLocalidadesState() {
        _localidadesState.value = UiState.Idle
    }

    fun resetReporteState() {
        _reporteState.value = UiState.Idle
    }
}
