package com.example.aula07_compose

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MeuComposable()
        }
    }
}

@Preview
@Composable
fun MeuComposable() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.string01))
        Text(stringResource(R.string.meu_texto_02))

        Button(
            onClick = {
                Log.d("TAG01","Botão clicado")
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Gray
            )
        ) {
            Image(
                painter = painterResource(id = R.drawable.ufu_logo),
                contentDescription = stringResource(R.string.logo_ufu)
            )
        }
    }
}