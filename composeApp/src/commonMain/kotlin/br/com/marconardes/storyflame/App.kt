package br.com.marconardes.storyflame

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
// import br.com.marconardes.viewmodel.ProjectViewModel // ViewModel will be handled by Screens or ScreenModels
import br.com.marconardes.storyflame.navigation.ProjectListScreenView // Import initial screen view
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

        // TODO: Navigation has been removed. Implement a new navigation solution here.
        // ProjectListScreenView() // Or some other initial screen Composable if it can be called directly
        Text("Navigation has been removed. Implement a new navigation solution.")
    }
}