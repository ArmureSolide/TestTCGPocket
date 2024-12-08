package com.example.testtcgpocket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.testtcgpocket.ui.CardComponent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.White,
            ) { innerPadding ->
                CardComponent(
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
