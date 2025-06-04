package br.com.marconardes.storyflame.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.marconardes.model.Chapter
import br.com.marconardes.model.Project
import br.com.marconardes.viewmodel.ProjectViewModel

@Composable
fun ChapterSectionView(
    selectedProject: Project,
    chapters: List<Chapter>,
    projectViewModel: ProjectViewModel, // Still needed for some direct actions like delete, summary update from ChapterItemView
    onShowEditChapterDialog: (chapter: Chapter) -> Unit,
    onShowChapterActionsDialog: (chapter: Chapter) -> Unit, // New: To be passed to ChapterItemView
    onAddNewChapter: (project: Project, title: String) -> Unit, // Modified
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 16.dp)) {
        Text(
            "Project: ${selectedProject.name}",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
        )

        // Add Chapter UI
        var newChapterTitle by remember { mutableStateOf("") }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp).fillMaxWidth()
        ) {
            OutlinedTextField(
                value = newChapterTitle,
                onValueChange = { newChapterTitle = it },
                label = { Text("New Chapter Title") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Button(onClick = {
                if (newChapterTitle.isNotBlank()) {
                    onAddNewChapter(selectedProject, newChapterTitle) // Use the new callback
                    newChapterTitle = "" // Clear text field
                }
            }) {
                Text("Add Chapter")
            }
        }

        if (chapters.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) { // Ensure it fills space
                Text(
                    "No chapters in this project yet. Add one above!",
                    modifier = Modifier.padding(24.dp), // Increased padding
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            // LazyColumn takes remaining space and has adjusted padding
            LazyColumn(modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                .fillMaxWidth()
            ) {
                items(chapters, key = { chapter -> chapter.id }) { chapter ->
                    ChapterItemView(
                        project = selectedProject,
                        chapter = chapter,
                        chaptersListSize = chapters.size,
                        projectViewModel = projectViewModel,
                        onShowEditChapterDialog = onShowEditChapterDialog, // Pass through
                        onShowChapterActionsDialog = onShowChapterActionsDialog // Pass through
                    )
                }
            }
        }
    }
}
