package com.example.mapa.utils

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.mapa.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SolicitarPermissoes(
    onPermsChange: (Boolean, Boolean) -> Unit
) {
    val context = LocalContext.current

    // Estado para controlar a exibição do Rationale (pausa explicativa)
    var mostrarDialogRationale by rememberSaveable { mutableStateOf(false) }

    // Permissões de localização (em primeiro plano)
    val permsLoc1Plano = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    // Verifica se as permissões de localização (em primeiro plano) já foram concedidas
    val permsLoc1PlanoOk = remember {
        permsLoc1Plano.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Verifica se a permissão de localização (em segundo plano) já foi concedida
    val permsLoc2PlanoOk = remember {
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // Launcher de permissões de segundo plano (O último a ser chamado)
    val reqPermsLoc2Plano = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { ok ->
        onPermsChange(true, ok)
    }

    // Launcher de permissões de primeiro plano (O primeiro a ser chamado)
    val reqPerms1Plano = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { ok ->
        if (!permsLoc1Plano.all { ok[it] == true }) {
            onPermsChange(false, false)
            return@rememberLauncherForActivityResult
        }

        mostrarDialogRationale = true
    }

    // Launcher de permissão de notificações
    val reqPermsNotificacao = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        reqPerms1Plano.launch(permsLoc1Plano)
    }

    // Solicita permissões
    LaunchedEffect(Unit) {
        if (permsLoc1PlanoOk) onPermsChange(true, permsLoc2PlanoOk)
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) reqPermsNotificacao.launch(Manifest.permission.POST_NOTIFICATIONS)
            else reqPerms1Plano.launch(permsLoc1Plano)
        }
    }

    // Diálogo para pedir a permissão de segundo plano
    if (!mostrarDialogRationale) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogRationale = false
                onPermsChange(true, false)
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text(stringResource(R.string.permitir_localizacao_em_segundo_plano)) },
            text = {
                Text(stringResource(R.string.explicacao_permissao_background))
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogRationale = false
                        reqPermsLoc2Plano.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    }
                ) {
                    Text(stringResource(R.string.abrir_configuracoes))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogRationale = false
                        onPermsChange(true, false)
                    }
                ) {
                    Text(stringResource(R.string.agora_nao))
                }
            }
        )
    }
}