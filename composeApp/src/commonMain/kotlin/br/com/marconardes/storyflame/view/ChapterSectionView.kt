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
    projectViewModel: ProjectViewModel,
    onShowEditChapterDialog: (Chapter) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            "Chapters for: ${selectedProject.name}",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Add Chapter UI
        var newChapterTitle by remember { mutableStateOf("") }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()
        ) {
            OutlinedTextField(
                value = newChapterTitle,
                onValueChange = { newChapterTitle = it },
                label = { Text("New Chapter Title") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (newChapterTitle.isNotBlank()) {
                    projectViewModel.addChapter(selectedProject, newChapterTitle)
                    newChapterTitle = "" // Clear text field
                }
            }) {
                Text("Add")
            }
        }

        if (chapters.isEmpty()) {
            Text(
                "No chapters in this project yet. Add one above!",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()) {
                items(chapters, key = { chapter -> chapter.id }) { chapter ->
                    // Pass project, chapter, viewModel, and the onShowEditChapterDialog lambda
                    ChapterItemView(
                        project = selectedProject,
                        chapter = chapter,
                        chaptersListSize = chapters.size, // Pass the size of the chapters list
                        projectViewModel = projectViewModel,
                        onShowEditChapterDialog = onShowEditChapterDialog
                        // Modifier can be added if needed, e.g., Modifier.fillParentMaxWidth()
                    )
                    // Add a small spacer or divider between chapter items if desired
                    // Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
