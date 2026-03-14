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
fun Context.createPhotoUri(): Uri {
    val file = File(this.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        this,
        "${this.packageName}.provider",
        file
    )
}
