package es.uma.seguridad.appbase.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import es.uma.seguridad.appbase.modelo.*

/**
 * Listado de fichajes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLista(
    estado: EstadoUi<List<Fichaje>>,
    onRecargar: () -> Unit,
    onSalir: () -> Unit,
    onAbrir: (Int) -> Unit
) {
    val contexto = LocalContext.current
    var concedido by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
        )
    }
    val lanzador = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        concedido = resultado.values.any { it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis fichajes") },
                actions = {
                    TextButton(onClick = onRecargar) { Text("Recargar") }
                    TextButton(onClick = onSalir) { Text("Salir") }
                }
            )
        }
    ) { relleno ->
        Column(modifier = Modifier.fillMaxSize().padding(relleno)) {

            Button(
                onClick = {
                    lanzador.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(if (concedido) "Permiso concedido" else "Conceder ubicación")
            }

            if (!concedido) {
                Text(
                    "Sin este permiso no se guarda desde dónde fichas. " +
                        "Puedes seguir fichando y viendo tus fichajes.",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when (estado) {
                    is EstadoUi.Reposo -> Text("Pulsa Recargar para cargar los datos")
                    is EstadoUi.Cargando -> CircularProgressIndicator()
                    is EstadoUi.Error -> Text("No se han podido cargar los fichajes")
                    is EstadoUi.Exito -> LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp)
                    ) {
                        items(estado.datos) { fichaje ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable { onAbrir(fichaje.id) }
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(fichaje.tipo, style = MaterialTheme.typography.titleMedium)
                                    Spacer(Modifier.height(4.dp))
                                    Text(fichaje.detalle, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(Modifier.height(4.dp))
                                    Text(fichaje.fecha, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
