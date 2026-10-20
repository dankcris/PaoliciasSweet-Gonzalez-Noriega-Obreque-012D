package cl.duoc.paoliciassweet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.duoc.paoliciassweet.ui.AppNav
import cl.duoc.paoliciassweet.ui.PaoliciasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PaoliciasTheme {
                AppNav()
            }
        }
    }
}
