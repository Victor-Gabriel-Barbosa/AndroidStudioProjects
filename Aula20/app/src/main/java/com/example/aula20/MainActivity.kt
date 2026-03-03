package com.example.aula20

import android.app.Application
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.aula20.data.api.ItensApiDataSource
import com.example.aula20.data.local.Item
import com.example.aula20.data.local.MeuAppDatabase
import com.example.aula20.data.repository.ItensRepository
import com.example.aula20.ui.theme.Aula20Theme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MeuUiState(
    val lista: List<Item> = listOf(),
)

class MeuViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(MeuUiState())
    val uiState = _uiState.asStateFlow()

    private val itensRepository: ItensRepository

    init {
        val db = Room.databaseBuilder(
            application.applicationContext,
            MeuAppDatabase::class.java,
            "meu_app_database"
        ).build()

        itensRepository = ItensRepository(
            db.itemDao(),
            ItensApiDataSource()
        )

        viewModelScope.launch {
            itensRepository.allItems.collect { listaDB ->
                _uiState.update {
                    it.copy(lista = listaDB)
                }
            }
        }
    }

    fun insereItemNaLista(novoItem: Item) {
        viewModelScope.launch {
            itensRepository.insert(novoItem)
        }
    }

    fun removeItemNaLista(id: Int) {
        viewModelScope.launch {
            itensRepository.deleteById(id)
        }
    }

    fun limparLista() {
        viewModelScope.launch {
            itensRepository.clearAllLocal()
        }
    }

    fun alteraItemChecked(id: Int, comprado: Boolean) {
        viewModelScope.launch {
            val item = _uiState.value.lista.find { it.id == id }
            if (item != null) itensRepository.update(item.copy(comprado = comprado))
        }
    }

    fun refreshItems() {
        viewModelScope.launch {
            itensRepository.refreshItems()
        }
    }
}

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
fun MinhaTela(viewModel: MeuViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var textoItem by rememberSaveable { mutableStateOf("") }

    Aula20Theme {
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

                EntradaItem(
                    textoItem,
                    onTextoChange = { textoItem = it },
                    onInsere = { viewModel.insereItemNaLista(Item(descricao = it, comprado = false)); textoItem = "" },
                    onLimpa = { viewModel.limparLista() },
                    onRefresh = { viewModel.refreshItems() }
                )

                Spacer(modifier = Modifier.height(20.dp))

                ListaItens(
                    listaItens = uiState.lista,
                    onCheckedChange = { id, checked ->
                        viewModel.alteraItemChecked(id, checked)
                    },
                    onRemover = { id ->
                        viewModel.removeItemNaLista(id)
                    },
                )
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
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = textoItem,
        onValueChange = onTextoChange,
        label = { Text(stringResource(R.string.item)) },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences
        )
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
                if (textoItem.isNotBlank()) onInsere(textoItem.trim())
            }
        ) {
            Text(stringResource(R.string.inserir))
        }

        FilledIconButton(
            onClick = { onRefresh() }
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = stringResource(R.string.recarregar)
            )
        }
    }
}

@Composable
fun ListaItens(
    listaItens: List<Item>,
    onCheckedChange: (Int, Boolean) -> Unit,
    onRemover: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(listaItens) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = it.comprado,
                            onValueChange = { checked ->
                                onCheckedChange(it.id, checked)
                            }
                        )
                        .padding(start = 10.dp, end = 10.dp)
                ) {
                    FilledIconButton(
                        onClick = { onRemover(it.id) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.remover)
                        )
                    }

                    Text(
                        text = it.descricao,
                        modifier = Modifier.padding(12.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Checkbox(
                        checked = it.comprado,
                        onCheckedChange = null
                    )
                }
            }
        }
    }
}