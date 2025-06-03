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
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .heightIn(max = 200.dp) // Limit height of project list
    ) {
        items(projects) { project ->
            Column(
                modifier = Modifier
                    .padding(vertical = 8.dp)
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
