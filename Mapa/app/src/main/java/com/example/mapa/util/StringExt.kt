package com.example.mapa.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

/**
 * Adiciona um asterisco vermelho ao final de uma string para indicar que é um campo obrigatório.
 */
@Composable
fun String.requiredLabel(): AnnotatedString {
    return buildAnnotatedString {
        append(this@requiredLabel)
        withStyle(SpanStyle(color = MaterialTheme.colorScheme.error)) {
            append(" *")
        }
    }
}