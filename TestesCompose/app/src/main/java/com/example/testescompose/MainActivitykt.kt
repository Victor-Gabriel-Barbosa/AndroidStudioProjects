package com.example.testescompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ComponentTestScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentTestScreen() {
    var sliderValue by remember { mutableFloatStateOf(0.5f) }
    var switchChecked by remember { mutableStateOf(true) }
    var checkboxChecked by remember { mutableStateOf(false) }
    var radioSelected by remember { mutableStateOf("Option 1") }
    var textFieldValue by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var selectedChip by remember { mutableStateOf("Chip 1") }
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Teste de Componentes") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = { /* Ação */ }) {
                        Icon(Icons.Filled.Search, contentDescription = "Buscar")
                    }
                    IconButton(onClick = { /* Ação */ }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Mais")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Adicionar")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Seção: Botões
            Text("Botões", style = MaterialTheme.typography.headlineSmall)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { }) {
                    Text("Button")
                }
                FilledTonalButton(onClick = { }) {
                    Text("Tonal")
                }
                OutlinedButton(onClick = { }) {
                    Text("Outlined")
                }
            }

            TextButton(onClick = { }) {
                Icon(Icons.Filled.Favorite, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Text Button")
            }

            HorizontalDivider()

            // Seção: Campos de Texto
            Text("Campos de Texto", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { textFieldValue = it },
                label = { Text("Digite algo") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            // Seção: Seleção
            Text("Seleção", style = MaterialTheme.typography.headlineSmall)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = checkboxChecked,
                    onCheckedChange = { checkboxChecked = it }
                )
                Text("Checkbox")

                Spacer(Modifier.width(16.dp))

                Switch(
                    checked = switchChecked,
                    onCheckedChange = { switchChecked = it }
                )
                Text("Switch")
            }

            Column {
                Text("Radio Buttons:", style = MaterialTheme.typography.bodyLarge)
                listOf("Option 1", "Option 2", "Option 3").forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = radioSelected == option,
                            onClick = { radioSelected = option }
                        )
                        Text(option)
                    }
                }
            }

            HorizontalDivider()

            // Seção: Slider
            Text("Slider", style = MaterialTheme.typography.headlineSmall)

            Column {
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Valor: ${(sliderValue * 100).toInt()}%")
            }

            HorizontalDivider()

            // Seção: Chips
            Text("Chips", style = MaterialTheme.typography.headlineSmall)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Chip 1", "Chip 2", "Chip 3").forEach { chip ->
                    FilterChip(
                        selected = selectedChip == chip,
                        onClick = { selectedChip = chip },
                        label = { Text(chip) },
                        leadingIcon = if (selectedChip == chip) {
                            { Icon(Icons.Filled.Check, contentDescription = null) }
                        } else null
                    )
                }
            }

            HorizontalDivider()

            // Seção: Cards
            Text("Cards", style = MaterialTheme.typography.headlineSmall)

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Info, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Card com conteúdo", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Este é um exemplo de card com ícone e texto.")
                }
            }

            HorizontalDivider()

            // Seção: Progress Indicators
            Text("Progress Indicators", style = MaterialTheme.typography.headlineSmall)

            LinearProgressIndicator(
                progress = { sliderValue },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator()
                Text("Carregando...")
            }

            HorizontalDivider()

            // Seção: Menu Dropdown
            Text("Dropdown Menu", style = MaterialTheme.typography.headlineSmall)

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = "Selecione uma opção",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    listOf("Item 1", "Item 2", "Item 3").forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Título do Dialog") },
            text = { Text("Este é um exemplo de AlertDialog no Material 3.") },
            icon = { Icon(Icons.Filled.Warning, contentDescription = null) },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}