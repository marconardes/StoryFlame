package br.com.marconardes.storyflame

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
// import androidx.compose.foundation.clickable // No longer directly used in App.kt
import androidx.compose.foundation.layout.*
// import androidx.compose.foundation.lazy.LazyColumn // Moved to ProjectListView and ChapterSectionView
// import androidx.compose.foundation.lazy.items // Moved
// import androidx.compose.material3.AlertDialog // Moved to EditChapterTitleDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
// import androidx.compose.material3.OutlinedTextField // Moved to specific views
import androidx.compose.material3.Text
// import androidx.compose.material3.TextButton // Moved
import androidx.compose.runtime.*
// import androidx.compose.runtime.LaunchedEffect // Moved to ChapterItemView
// import androidx.compose.runtime.snapshotFlow // Moved to ChapterItemView
import androidx.compose.ui.Alignment
import br.com.marconardes.model.Chapter // Still needed for state variables
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
// import br.com.marconardes.storyflame.view.RichTextEditorView // Not directly used in App.kt
import br.com.marconardes.storyflame.view.ChapterSectionView
import br.com.marconardes.storyflame.view.EditChapterTitleDialog
import br.com.marconardes.storyflame.view.ProjectCreationView
import br.com.marconardes.storyflame.view.ProjectListView
import br.com.marconardes.viewmodel.ProjectViewModel
// import com.mohamedrejeb.richeditor.model.RichTextState // Not directly used in App.kt
// import kotlinx.coroutines.flow.collectLatest // Moved
// import kotlinx.coroutines.flow.debounce // Moved
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import storyflame.composeapp.generated.resources.Res
import storyflame.composeapp.generated.resources.compose_multiplatform

@Composable
@Preview
fun App() {
    MaterialTheme {
        val projectViewModel = remember { ProjectViewModel() }
        val projects by projectViewModel.projects.collectAsState()
        val selectedProject by projectViewModel.selectedProject.collectAsState()
        val chapters by projectViewModel.selectedProjectChapters.collectAsState()
        var showContent by remember { mutableStateOf(false) }

        // State for Edit Chapter Dialog
        var showEditChapterDialog by remember { mutableStateOf(false) }
        var editingChapter by remember { mutableStateOf<Chapter?>(null) }
        var editChapterTitleInput by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = { showContent = !showContent }) {
                Text("Toggle Original Content")
            }
            AnimatedVisibility(showContent) {
                val greeting = remember { Greeting().greet() }
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                    Text("Compose: $greeting")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ProjectCreationView(projectViewModel = projectViewModel, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(16.dp))

            Text("Projects:", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 16.dp))
            ProjectListView(
                projects = projects,
                selectedProject = selectedProject,
                onProjectSelected = projectViewModel::selectProject,
                modifier = Modifier.fillMaxWidth() // Adjust modifier as needed
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            selectedProject?.let { proj ->
                ChapterSectionView(
                    selectedProject = proj,
                    chapters = chapters,
                    projectViewModel = projectViewModel,
                    onShowEditChapterDialog = { chapterToEdit ->
                        editingChapter = chapterToEdit
                        editChapterTitleInput = chapterToEdit.title
                        showEditChapterDialog = true
                    },
                    modifier = Modifier.fillMaxWidth() // Adjust modifier as needed
                )
            } ?: run {
                Text(
                    "Select a project to view its chapters.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            EditChapterTitleDialog(
                editingChapter = editingChapter,
                editChapterTitleInput = editChapterTitleInput,
                onTitleChange = { editChapterTitleInput = it },
                onDismiss = {
                    showEditChapterDialog = false
                    editingChapter = null // Clear editing state
                },
                onSave = {
                    if (editingChapter != null && selectedProject != null) { // Ensure non-null before saving
                        projectViewModel.updateChapterTitle(
                            selectedProject!!, // Already checked selectedProject is not null
                            editingChapter!!.id,
                            editChapterTitleInput
                        )
                    }
                    showEditChapterDialog = false
                    editingChapter = null // Clear editing state
                }
            )
        }
    }
}