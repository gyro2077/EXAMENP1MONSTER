package ec.edu.espe.ticketpremium

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ec.edu.espe.ticketpremium.model.Partido
import ec.edu.espe.ticketpremium.ui.screens.CompraScreen
import ec.edu.espe.ticketpremium.ui.screens.LoginScreen
import ec.edu.espe.ticketpremium.ui.screens.PartidosScreen
import ec.edu.espe.ticketpremium.ui.screens.ReporteScreen
import ec.edu.espe.ticketpremium.ui.theme.TicketPremiumTheme
import ec.edu.espe.ticketpremium.viewmodel.TicketViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Partidos : Screen("partidos")
    object Compra : Screen("compra/{codigo}/{equipoLocal}/{equipoVisita}/{fecha}/{lugar}") {
        fun createRoute(partido: Partido) =
            "compra/${partido.codigo}/${partido.equipoLocal}/${partido.equipoVisita}/${partido.fecha}/${partido.lugar}"
    }
    object Reporte : Screen("reporte/{codigo}/{equipoLocal}/{equipoVisita}/{fecha}/{lugar}") {
        fun createRoute(partido: Partido) =
            "reporte/${partido.codigo}/${partido.equipoLocal}/${partido.equipoVisita}/${partido.fecha}/${partido.lugar}"
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TicketPremiumTheme {
                val navController = rememberNavController()
                val viewModel: TicketViewModel = viewModel()

                NavHost(
                    navController = navController,
                    startDestination = Screen.Login.route
                ) {
                    composable(Screen.Login.route) {
                        LoginScreen(
                            onLoginSuccess = {
                                navController.navigate(Screen.Partidos.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            },
                            viewModel = viewModel
                        )
                    }

                    composable(Screen.Partidos.route) {
                        PartidosScreen(
                            onPartidoSelected = { partido ->
                                navController.navigate(Screen.Compra.createRoute(partido))
                            },
                            onReporteSelected = { partido ->
                                navController.navigate(Screen.Reporte.createRoute(partido))
                            },
                            viewModel = viewModel
                        )
                    }

                    composable(Screen.Compra.route) { backStackEntry ->
                        val codigo = backStackEntry.arguments?.getString("codigo")?.toIntOrNull() ?: 0
                        val equipoLocal = backStackEntry.arguments?.getString("equipoLocal") ?: ""
                        val equipoVisita = backStackEntry.arguments?.getString("equipoVisita") ?: ""
                        val fecha = backStackEntry.arguments?.getString("fecha") ?: ""
                        val lugar = backStackEntry.arguments?.getString("lugar") ?: ""
                        val partido = Partido(codigo, equipoLocal, equipoVisita, fecha, lugar)

                        CompraScreen(
                            partido = partido,
                            onBack = {
                                navController.popBackStack()
                            },
                            viewModel = viewModel
                        )
                    }

                    composable(Screen.Reporte.route) { backStackEntry ->
                        val codigo = backStackEntry.arguments?.getString("codigo")?.toIntOrNull() ?: 0
                        val equipoLocal = backStackEntry.arguments?.getString("equipoLocal") ?: ""
                        val equipoVisita = backStackEntry.arguments?.getString("equipoVisita") ?: ""
                        val fecha = backStackEntry.arguments?.getString("fecha") ?: ""
                        val lugar = backStackEntry.arguments?.getString("lugar") ?: ""
                        val partido = Partido(codigo, equipoLocal, equipoVisita, fecha, lugar)

                        ReporteScreen(
                            partido = partido,
                            onBack = {
                                navController.popBackStack()
                            },
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}
