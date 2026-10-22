package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.CartaApp
import com.example.ui.theme.CartaTheme
import com.example.ui.theme.Zinc950
import com.example.viewmodel.MenuViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CartaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Zinc950
                ) {
                    val viewModel: MenuViewModel = viewModel()
                    CartaApp(viewModel = viewModel)
                }
            }
        }
    }
}
