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
    chaptersListSize: Int,
    projectViewModel: ProjectViewModel,
    onShowEditChapterDialog: (chapter: Chapter) -> Unit,
    onShowChapterActionsDialog: (chapter: Chapter) -> Unit, // New callback
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            // Make the whole item clickable for actions, alternative to a dedicated "Actions" button
            // .clickable { onShowChapterActionsDialog(chapter) } // This might be too broad if there are text fields
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // If the whole column is clickable, this specific click might be redundant or for a different action.
                // For now, let's make the Row clickable for actions as it's less likely to interfere with text fields.
                .clickable { onShowChapterActionsDialog(chapter) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) { // Added padding to prevent text touching buttons
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
                Spacer(modifier = Modifier.width(8.dp)) // Increased spacer
                TextButton(onClick = {
                    onShowEditChapterDialog(chapter)
                }) {
                    Text("Edit Title")
                }
                Spacer(modifier = Modifier.width(8.dp)) // Increased spacer
                Button(onClick = {
                    projectViewModel.deleteChapter(project, chapter.id)
                }) {
                    Text("Del")
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp)) // Increased space between title/buttons and summary field

        var summaryInput by remember(chapter.id, chapter.summary) { mutableStateOf(chapter.summary) }
        OutlinedTextField(
            value = summaryInput,
            onValueChange = { summaryInput = it },
            label = { Text("Summary") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 75.dp, max = 250.dp), // Adjusted height
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
