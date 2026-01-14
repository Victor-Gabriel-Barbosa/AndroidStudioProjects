package com.example.mapa.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Cria um [Uri] para um novo arquivo de foto no diretório de cache do aplicativo.
 *
 * Esta função gera um nome de arquivo único usando o timestamp atual e utiliza um [FileProvider]
 * para criar um URI de conteúdo seguro para o arquivo. Este URI pode ser usado por outros aplicativos,
 * como a câmera, para salvar uma imagem.
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
