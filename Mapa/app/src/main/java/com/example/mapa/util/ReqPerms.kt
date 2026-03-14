package com.example.mapa.util

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Solicita permissões de localização para o usuário.
 *
 * @param onPermsChange Callback que é chamado quando as permissões são alteradas.
 */
@Composable
fun ReqPerms(
    onPermsChange: (Boolean) -> Unit
) {
    val context = LocalContext.current

    // Permissões de localização e notificação (se necessário)
    val perms = remember {
        mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
        }.toTypedArray()
    }

    // Solicita permissões
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { ok ->
        val locationOk = ok[Manifest.permission.ACCESS_FINE_LOCATION] == true || ok[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        onPermsChange(locationOk)
    }

    // Verifica se as permissões já foram concedidas
    LaunchedEffect(Unit) {
        val granted = perms.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        if (granted) onPermsChange(true)
        else launcher.launch(perms)
    }
}