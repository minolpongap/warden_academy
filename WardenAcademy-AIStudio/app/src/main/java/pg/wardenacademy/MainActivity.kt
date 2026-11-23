package pg.wardenacademy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import pg.wardenacademy.ui.WardenRoot
import pg.wardenacademy.ui.theme.WardenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as WardenApp
        setContent {
            WardenTheme {
                WardenRoot(app)
            }
        }
    }
}
