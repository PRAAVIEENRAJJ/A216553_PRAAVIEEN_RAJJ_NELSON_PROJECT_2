package com.example.a216553_praavieenrajj_mrnelson_project_2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.a216553_praavieenrajj_mrnelson_project_2.Navigation.AppNavigator
import com.example.a216553_praavieenrajj_mrnelson_project_2.ui.theme.A216553_PRAAVIEEN_RAJJ_NELSON_LAB04Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            A216553_PRAAVIEEN_RAJJ_NELSON_LAB04Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigator()
                }
            }
        }
    }
}