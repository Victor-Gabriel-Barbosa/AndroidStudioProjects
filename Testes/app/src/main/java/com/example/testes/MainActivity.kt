package com.example.testes

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.testes.ui.theme.TestesTheme
import kotlinx.coroutines.launch
import com.example.testes.R

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
        CarrosselExemplo()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaComTresEstados() {
    val scope = rememberCoroutineScope()

    // 1. Configura o estado para permitir que ela suma (Hidden)
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            skipHiddenState = false,
            initialValue = SheetValue.PartiallyExpanded // Começa minimizada
        )
    )

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        // 2. Define a altura da "Barra Minimizada".
        // A sheet vai travar aqui antes de fechar totalmente.
        sheetPeekHeight = 100.dp,

        // Dica visual: Adicione uma sombra/elevação para separar do mapa
        sheetShadowElevation = 12.dp,
        sheetContent = {
            // Conteúdo da Sheet
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Um "Handle" (barrinha cinza) ajuda o usuário a entender que pode arrastar
                Box(
                    modifier = Modifier
                        .width(50.dp)
                        .height(4.dp)
                        .background(Color.Gray, shape = RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Item Selecionado: Chaves de Casa", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Detalhes extras aparecem ao expandir...")
                // Adicione altura aqui para simular conteúdo longo
                Spacer(modifier = Modifier.height(200.dp))
            }
        }
    ) { paddingValues ->
        // Conteúdo de fundo (Mapa)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text("Mapa aqui")

            // Botão para reabrir caso o usuário feche tudo
            if (scaffoldState.bottomSheetState.currentValue == SheetValue.Hidden) {
                Button(
                    onClick = {
                        scope.launch {
                            // Traz de volta para o estado minimizado
                            scaffoldState.bottomSheetState.partialExpand()
                        }
                    },
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    Text("Reabrir Sheet")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarrosselExemplo() {
    data class CarouselItem(
        val id: Int,
        @DrawableRes val imageResId: Int,
        val contentDescription: String
    )

    val carouselItems = remember {
        listOf(
            CarouselItem(0, R.drawable.wallpaper1, "wallpaper"),
            CarouselItem(1, R.drawable.wallpaper2, "wallpaper"),
            CarouselItem(2, R.drawable.wallpaper3, "wallpaper"),
            CarouselItem(3, R.drawable.wallpaper4, "wallpaper"),
            CarouselItem(4, R.drawable.wallpaper5, "wallpaper"),
        )
    }

    HorizontalUncontainedCarousel(
        state = rememberCarouselState { carouselItems.count() },
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 16.dp, bottom = 16.dp),
        itemWidth = 186.dp,
        itemSpacing = 8.dp,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) { i ->
        val item = carouselItems[i]
        Image(
            modifier = Modifier
                .height(205.dp)
                .maskClip(MaterialTheme.shapes.extraLarge),
            painter = painterResource(id = item.imageResId),
            contentDescription = item.contentDescription,
            contentScale = ContentScale.Crop
        )
    }
}
