package br.com.marconardes.storyflame

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
// import br.com.marconardes.viewmodel.ProjectViewModel // ViewModel will be handled by Screens or ScreenModels
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import br.com.marconardes.storyflame.navigation.ProjectListScreen // Import your initial screen
// Removed Preview import as it might conflict with Navigator, can be re-added if needed for specific screen previews
// import org.jetbrains.compose.ui.tooling.preview.Preview

// @Preview // Preview might not work well with Navigator directly.
@Composable
fun App() {
    MaterialTheme {
        // val projectViewModel = remember { ProjectViewModel() } // ViewModel will be handled by Screens/ScreenModels

        // State for Edit Chapter Dialog - To be refactored and moved into appropriate screen/viewmodel later
        // var showEditChapterDialog by remember { mutableStateOf(false) }
        // var editingChapter by remember { mutableStateOf<Chapter?>(null) }
        // var editChapterTitleInput by remember { mutableStateOf("") }

        Navigator(screen = ProjectListScreen) { navigator ->
            SlideTransition(navigator)
            // If content doesn't show, try:
            // SlideTransition(navigator) { screen ->
            //     screen.Content()
            // }
        }
    }
}