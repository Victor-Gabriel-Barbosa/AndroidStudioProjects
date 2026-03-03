package com.example.mapa.util

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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

/**
 * Solicita permissões de localização para o usuário.
 *
 * @param onPermsChange Callback que é chamado quando as permissões são alteradas.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SolicitarPermissoes(
    onPermsChange: (Boolean, Boolean) -> Unit
) {
    val context = LocalContext.current

    // Estado para controlar a exibição do Rationale (pausa explicativa)
    var mostrarDialog by rememberSaveable { mutableStateOf(false) }

    // Permissões de localização (em primeiro plano)
    val permsLocPrimeiroPlano = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    // Verifica se as permissões de localização (em primeiro plano) já foram concedidas
    val permsLocPrimeiroPlanoOk = remember {
        permsLocPrimeiroPlano.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Verifica se a permissão de localização (em segundo plano) já foi concedida
    val permLocSegundoPlanoOk = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    // Launcher de permissões de segundo plano (O último a ser chamado)
    val reqPermLocSegundoPlano = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { ok ->
        onPermsChange(true, ok)
    }

    // Launcher de permissões de primeiro plano (O primeiro a ser chamado)
    val reqPermsLocPrimeiroPlano = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { ok ->
        if (!permsLocPrimeiroPlano.all { ok[it] == true }) {
            onPermsChange(false, false)
            return@rememberLauncherForActivityResult
        }

        mostrarDialog = true
    }

    // Launcher de permissão de notificações
    val reqPermNotificacao = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        reqPermsLocPrimeiroPlano.launch(permsLocPrimeiroPlano)
    }

    // Solicita permissões
    LaunchedEffect(Unit) {
        if (permsLocPrimeiroPlanoOk) onPermsChange(true, permLocSegundoPlanoOk)
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) reqPermNotificacao.launch(Manifest.permission.POST_NOTIFICATIONS)
            else reqPermsLocPrimeiroPlano.launch(permsLocPrimeiroPlano)
        }
    }

    // Diálogo para pedir a permissão de segundo plano
    if (mostrarDialog) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialog = false
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
                        mostrarDialog = false
                        reqPermLocSegundoPlano.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    }
                ) {
                    Text(stringResource(R.string.abrir_configuracoes))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialog = false
                        onPermsChange(true, false)
                    }
                ) {
                    Text(stringResource(R.string.agora_nao))
                }
            }
        )
    }
}