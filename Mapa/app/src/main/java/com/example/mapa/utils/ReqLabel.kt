package com.example.mapa.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

/**
 * Adicione um asterisco vermelho ao final de uma string para indicar que é um campo obrigatório.
 */
@Composable
fun String.labelObrigatorio(): AnnotatedString {
    return buildAnnotatedString {
        append(this@labelObrigatorio)
        withStyle(SpanStyle(color = MaterialTheme.colorScheme.error)) {
            append(" *")
        }
    }
}