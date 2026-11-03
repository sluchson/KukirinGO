package pl.kukiringo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import pl.kukiringo.app.navigation.KukirinApp
import pl.kukiringo.app.ui.theme.KukirinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KukirinTheme {
                KukirinApp()
            }
        }
    }
}
