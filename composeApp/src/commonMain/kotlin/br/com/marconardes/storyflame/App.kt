package br.com.marconardes.storyflame

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
// import br.com.marconardes.viewmodel.ProjectViewModel // ViewModel will be handled by Screens or ScreenModels
// import br.com.marconardes.storyflame.navigation.ProjectListScreenView // No longer directly calling this from App common
// Removed Preview import as it might conflict with Navigator, can be re-added if needed for specific screen previews
// import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal expect fun AppNavigationHost()

// @Preview // Preview might not work well with Navigator directly.
@Composable
fun App() {
    MaterialTheme {
        // Call the expected Composable that will provide platform-specific navigation
        AppNavigationHost()
    }
}