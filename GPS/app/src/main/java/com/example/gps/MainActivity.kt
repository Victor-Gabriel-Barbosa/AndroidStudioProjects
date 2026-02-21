package com.example.gps

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.gps.ui.theme.GPSTheme
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GPSTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GPS(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun GPS(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // Estado para armazenar os dados do GPS
    var info by rememberSaveable { mutableStateOf("Aguardando sinal GPS...") }
    var rastreando by rememberSaveable { mutableStateOf(false) }

    // Cliente de localização
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // Configuração da requisição de localização (Alta precisão para teste)
    val locationRequest = remember {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMinUpdateIntervalMillis(500)
            .build()
    }

    // Callback que recebe as atualizações
    val locationCallback = remember {
        val fmt = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        object : LocationCallback() {
            override fun onLocationResult(res: LocationResult) {
                res.lastLocation?.let { location ->
                    info = """
                        Latitude: ${location.latitude}
                        Longitude: ${location.longitude}
                        Altitude: ${location.altitude} m
                        Precisão: ±${location.accuracy} m
                        Velocidade: ${location.speed} m/s
                        Tempo: ${fmt.format(location.time)}
                        Direção: ${location.bearing} graus
                        Provedor: ${location.provider}
                    """.trimIndent()
                }
            }
        }
    }

    // Lançador para pedir permissão
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val concedido = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (concedido) rastreando = true
        else info = "Permissão de localização negada!"
    }

    // Efeito para iniciar/parar as atualizações
    DisposableEffect(rastreando) {
        if (rastreando) {
            // Verifica permissão novamente antes de iniciar (segurança do Android)
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        }

        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    // UI para exibir os dados
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.GpsFixed,
                contentDescription = "GPS",
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "GPS",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = info,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (rastreando) {
                    rastreando = false
                    info = "GPS pausado"
                } else {
                    // Solicita permissão ao clicar em Iniciar
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (rastreando) "Parar GPS" else "Iniciar GPS")
        }
    }
}