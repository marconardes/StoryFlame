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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.marconardes.viewmodel.ProjectViewModel
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

            // Project Creation UI
            var newProjectName by remember { mutableStateOf("") }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
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

            Spacer(modifier = Modifier.height(16.dp))

            // Displaying the list of projects
            Text("Projects:", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 16.dp))
            LazyColumn(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .heightIn(max = 200.dp) // Limit height of project list
            ) {
                items(projects) { project ->
                    Column(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth()
                            .clickable { projectViewModel.selectProject(project) }
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

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Displaying Chapters of Selected Project
            selectedProject?.let { proj ->
                Text("Chapters for: ${proj.name}", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 8.dp))

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
                            projectViewModel.addChapter(proj, newChapterTitle)
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
                        items(chapters) { chapter ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(chapter.title, style = MaterialTheme.typography.bodyLarge)
                                    Text("Order: ${chapter.order}", style = MaterialTheme.typography.bodySmall)
                                }
                                Button(onClick = {
                                    projectViewModel.deleteChapter(proj, chapter.id)
                                }) {
                                    Text("Del")
                                }
                            }
                        }
                    }
                }
            } ?: run {
                Text(
                    "Select a project to view its chapters.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}