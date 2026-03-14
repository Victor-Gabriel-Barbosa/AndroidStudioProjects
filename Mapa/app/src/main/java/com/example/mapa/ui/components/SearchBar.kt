package com.example.mapa.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mapa.R
import com.example.mapa.ui.theme.MapaTheme

/**
 * Componente de barra de pesquisa reutilizável.
 *
 * @param onSearch Callback que é chamado quando o texto de pesquisa é alterado.
 * @param modifier [Modifier] para customizar o layout, tamanho e comportamento do componente.
 */
@Composable
fun SearchBar(
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Estado para armazenar o texto de pesquisa
    var search by rememberSaveable { mutableStateOf("") }

    OutlinedTextField(
        value = search,
        onValueChange = {
            search = it
            onSearch(it)
        },
        modifier = modifier,
        placeholder = { Text("${stringResource(R.string.pesquisar)}...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.pesquisar)
            )
        },
        trailingIcon = {
            if (search.isNotEmpty()) {
                IconButton(
                    onClick = {
                        search = ""
                        onSearch("")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.limpar_pesquisa)
                    )
                }
            }
        },
        shape = MaterialTheme.shapes.extraLarge,
        singleLine = true,
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Preview
@Composable
private fun SearchBarPreview() {
    MapaTheme {
        SearchBar(
            onSearch = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}