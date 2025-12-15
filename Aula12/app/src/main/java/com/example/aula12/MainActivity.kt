package com.example.aula12

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aula12.ui.theme.Aula12Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MinhaTela()
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MinhaTela() {
    var textoItem by rememberSaveable { mutableStateOf("") }
    val listaItens = rememberSaveable { mutableStateListOf<String>() }

    Aula12Theme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.lista_de_compras),
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                EntradaItem(textoItem, onTextoChange = { textoItem = it }, onInsere = { listaItens.add(it) }, onLimpa = { listaItens.clear() })

                Spacer(modifier = Modifier.height(20.dp))

                ListaItens(listaItens)
            }
        }
    }
}

@Composable
fun EntradaItem(
    textoItem: String,
    onTextoChange: (String) -> Unit,
    onInsere: (String) -> Unit,
    onLimpa: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = textoItem,
        onValueChange = onTextoChange,
        label = { Text(stringResource(R.string.item)) },
        modifier = modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            onClick = onLimpa
        ) {
            Text(stringResource(R.string.limpar_lista))
        }

        Button(
            onClick = {
                if (textoItem.isNotBlank()) {
                    onInsere(textoItem.trim())
                    onTextoChange("")
                }
            }
        ) {
            Text(stringResource(R.string.inserir))
        }
    }
}

@Composable
fun ListaItens(
    listaItens: List<String>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(listaItens) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = it,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}