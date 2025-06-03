package br.com.marconardes.storyflame.view

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.marconardes.viewmodel.ProjectViewModel

@Composable
fun ProjectCreationView(projectViewModel: ProjectViewModel, modifier: Modifier = Modifier) {
    var newProjectName by remember { mutableStateOf("") }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        OutlinedTextField(
            value = newProjectName,
            onValueChange = { newProjectName = it },
            label = { Text("New Project Name") },
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = {
            if (newProjectName.isNotBlank()) {
                projectViewModel.createProject(newProjectName)
                newProjectName = "" // Clear text field
            }
        }) {
            Text("Create")
        }
    }
}
