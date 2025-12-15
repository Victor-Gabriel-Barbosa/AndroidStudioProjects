package com.example.testes

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.testes.ui.theme.TestesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MeuCompA(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MeuCompA(modifier: Modifier = Modifier) {
    Log.d("XXX", "MeuCompA chamado!")

    var nome by rememberSaveable {
        Log.d("XXX", "Função lambda chamada!")
        mutableStateOf("")
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MeuCompB(nome = nome, onNomeChange = { nome = it })
    }
}

@Composable
fun MeuCompB(nome: String, onNomeChange: (String) -> Unit) {
    Log.d("XXX", "MeuCompB chamado!")

    TextField(
        value = nome,
        onValueChange = onNomeChange,
        label = { Text("Nome") }
    )

    Button(
        onClick = { onNomeChange("") },
        enabled = nome.isNotEmpty(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Red,
            contentColor = Color.White
        )
    ) {
        Text(text = "Limpar Nome")
    }

    if (nome.isNotEmpty()) {
        Text(text = "Olá, $nome!")
        MeuCompC(nome.lowercase() == "victor")
    } else {
        Text(text = "Digite seu nome!")
    }
}

@Composable
fun MeuCompC(nomeCorreto: Boolean) {
    Log.d("XXX", "MeuCompC chamado!")

    Text(
        text = if (nomeCorreto) "Nome Correto!" else "Nome incorreto",
        color = if (nomeCorreto) Color.Green else Color.Red
    )
}