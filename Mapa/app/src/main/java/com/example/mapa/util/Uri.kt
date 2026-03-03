package com.example.mapa.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Cria um [Uri] para um novo arquivo de foto no diretório de cache do aplicativo.
 *
 * @return Um [Uri] para o arquivo de foto a ser criado.
 */
fun Context.criarUriParaFoto(): Uri {
    val arquivo = File(this.cacheDir, "foto_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        this,
        "${this.packageName}.provider",
        arquivo
    )
}
