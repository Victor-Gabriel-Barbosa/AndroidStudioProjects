package com.example.mapa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.mapa.ui.navigation.Mapa
import com.example.mapa.ui.theme.MapaTheme
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import org.koin.compose.KoinContext

class MainActivity : ComponentActivity() {
    /**
     * Analytics para rastrear eventos no Firebase
     */
    private lateinit var analytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        analytics = Firebase.analytics

        setContent {
            KoinContext {
                MapaTheme {
                    Mapa()
                }
            }
        }
    }
}