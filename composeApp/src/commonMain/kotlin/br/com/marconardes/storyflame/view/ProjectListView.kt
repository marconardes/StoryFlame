package br.com.marconardes.storyflame.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.marconardes.model.Project

@Composable
fun ProjectListView(
    projects: List<Project>,
    selectedProject: Project?,
    onProjectSelected: (Project) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .padding(horizontal = 24.dp, vertical = 16.dp) // Adjusted padding
            .fillMaxWidth()
            .fillMaxHeight() // Fill available height
    ) {
        items(projects) { project ->
            Column(
                modifier = Modifier
                    .padding(vertical = 12.dp) // Adjusted padding
                    .fillMaxWidth()
                    .clickable { onProjectSelected(project) }
            ) {
                Text(
                    text = "Project Name: ${project.name}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (project.id == selectedProject?.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text("Creation Date: ${project.creationDate}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
