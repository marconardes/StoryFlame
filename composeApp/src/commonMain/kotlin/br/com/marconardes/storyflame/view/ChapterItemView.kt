package br.com.marconardes.storyflame.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.marconardes.model.Chapter
import br.com.marconardes.model.Project
import br.com.marconardes.viewmodel.ProjectViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import androidx.compose.runtime.snapshotFlow // Corrected import

@Composable
fun ChapterItemView(
    project: Project,
    chapter: Chapter,
    chaptersListSize: Int, // Added to help with 'move down' button enablement
    projectViewModel: ProjectViewModel,
    onShowEditChapterDialog: (Chapter) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(chapter.title, style = MaterialTheme.typography.bodyLarge)
                Text("Order: ${chapter.order}", style = MaterialTheme.typography.bodySmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = { projectViewModel.moveChapter(project, chapter.id, moveUp = true) },
                    enabled = chapter.order > 0
                ) {
                    Text("Up")
                }
                TextButton(
                    onClick = { projectViewModel.moveChapter(project, chapter.id, moveUp = false) },
                    enabled = chapter.order < chaptersListSize - 1
                ) {
                    Text("Dn") // Down abbreviated
                }
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = {
                    onShowEditChapterDialog(chapter)
                }) {
                    Text("Edit Title")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Button(onClick = {
                    projectViewModel.deleteChapter(project, chapter.id)
                }) {
                    Text("Del")
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp)) // Space between title/buttons and summary field

        var summaryInput by remember(chapter.id, chapter.summary) { mutableStateOf(chapter.summary) }
        OutlinedTextField(
            value = summaryInput,
            onValueChange = { summaryInput = it },
            label = { Text("Summary") },
            modifier = Modifier.height(100.dp).fillMaxWidth(),
            singleLine = false
        )
        TextButton(
            onClick = {
                projectViewModel.updateChapterSummary(project, chapter.id, summaryInput)
            },
            modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
        ) {
            Text("Save Summary")
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Chapter Content:", style = MaterialTheme.typography.titleMedium)

    }
}
