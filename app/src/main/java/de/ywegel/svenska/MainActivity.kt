package de.ywegel.svenska

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import de.ywegel.svenska.navigation.AppNavigation
import de.ywegel.svenska.ui.theme.SvenskaTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SvenskaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SvenskaTheme.colors.background,
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
