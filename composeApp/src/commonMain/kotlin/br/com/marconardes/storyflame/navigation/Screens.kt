package br.com.marconardes.storyflame.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
// Removed unused Material Icon imports:
// import androidx.compose.material.icons.Icons
// import androidx.compose.material.icons.automirrored.filled.ArrowBack
// import androidx.compose.material.icons.filled.Delete
// import androidx.compose.material.icons.filled.Edit
// import androidx.compose.material.icons.filled.KeyboardArrowDown
// import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.marconardes.model.Chapter
import br.com.marconardes.storyflame.view.ProjectCreationView
import br.com.marconardes.storyflame.view.ProjectListView
// import br.com.marconardes.storyflame.view.RichTextEditorView // Used FQN below
import br.com.marconardes.viewmodel.ProjectViewModel
// import cafe.adriel.voyager.core.model.rememberScreenModel // Removed: Voyager
// import cafe.adriel.voyager.core.screen.Screen // Removed: Voyager
// import cafe.adriel.voyager.navigator.LocalNavigator // Removed: Voyager
// import cafe.adriel.voyager.navigator.currentOrThrow // Removed: Voyager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import androidx.compose.runtime.snapshotFlow // Added for Markdown auto-save
// Keep br.com.marconardes.storyflame.view.EditChapterTitleDialog fully qualified in usage or add import here

// Removed object ProjectListScreen as its Content is now ProjectListScreenView
// object ProjectListScreen // Removed : Screen
// No longer an override

@OptIn(ExperimentalMaterial3Api::class) // For Scaffold and TopAppBar
@Composable
fun ProjectListScreenView(projectViewModel: ProjectViewModel = ProjectViewModel() /* TODO: Review ViewModel instantiation */) {
    val projects by projectViewModel.projects.collectAsState()
    val selectedProject by projectViewModel.selectedProject.collectAsState()
    // val navigator = LocalNavigator.currentOrThrow // Removed: Voyager

    Scaffold(
        topBar = { TopAppBar(title = { Text("StoryFlame Projects") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            ProjectCreationView(
                projectViewModel = projectViewModel,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            ProjectListView(
                projects = projects,
                selectedProject = selectedProject,
                onProjectSelected = { project ->
                    // TODO: Navigate to ChapterListScreen(projectId = project.id)
                    // navigator.push(ChapterListScreen(projectId = project.id)) // Removed: Voyager
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
    // Removed one extra closing brace that was here for ProjectListScreenView
}

data class ChapterActionChoiceScreenParams(val projectId: String, val chapterId: String) // Renamed from ChapterActionChoiceScreen, removed : Screen
// No longer an override

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterActionChoiceScreenView(params: ChapterActionChoiceScreenParams, projectViewModel: ProjectViewModel = ProjectViewModel() /* TODO: Review ViewModel instantiation */) {
    // val projectViewModel: ProjectViewModel = rememberScreenModel { ProjectViewModel() } // Removed: Voyager
    // val navigator = LocalNavigator.currentOrThrow // Removed: Voyager

    val projects by projectViewModel.projects.collectAsState()
    val currentProject = remember(projects, params.projectId) { // Used params
        projects.find { it.id == params.projectId }
    }

    LaunchedEffect(currentProject) {
        currentProject?.let { projectViewModel.selectProject(it) }
    }

    val selectedProjectDetails by projectViewModel.selectedProject.collectAsState()
    val currentChapter = remember(selectedProjectDetails, params.chapterId) { // Used params
        selectedProjectDetails?.chapters?.find { it.id == params.chapterId }
    } // This curly brace is for 'remember'.

    Scaffold( // This Scaffold should be outside the remember block's lambda.
        topBar = {
                TopAppBar(
                    title = { Text("Actions for: ${currentChapter?.title ?: "Chapter"}") },
                    navigationIcon = {
                        TextButton(onClick = { /* TODO: Navigate back */ /* navigator.pop() */ }) { // Removed: Voyager
                            Text("Back") // Already "Back" from previous step, but ensuring it is.
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (currentProject != null && currentChapter != null) {
                    Text("Project: ${currentProject.name}", style = MaterialTheme.typography.titleMedium) // Translated
                    Text("Chapter: ${currentChapter.title}", style = MaterialTheme.typography.titleSmall) // Translated
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { /* TODO: Navigate to ChapterEditorScreen(params.projectId, params.chapterId, initialFocus = "summary") */ /* navigator.replace(ChapterEditorScreen(projectId, chapterId, initialFocus = "summary")) */ }, // Removed: Voyager
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("Edit Summary") // Translated
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { /* TODO: Navigate to ChapterEditorScreen(params.projectId, params.chapterId, initialFocus = "content") */ /* navigator.replace(ChapterEditorScreen(projectId, chapterId, initialFocus = "content")) */ }, // Removed: Voyager
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("Edit Content (Markdown)") // Translated
                    }
                } else {
                    Text("Loading details...") // Translated
                }
            }
        }
    // Removed one extra closing brace that was here for ChapterActionChoiceScreenView
}

data class ChapterListScreenParams(val projectId: String) // Renamed from ChapterListScreen, removed : Screen
// No longer an override

@OptIn(ExperimentalMaterial3Api::class) // For Scaffold, TopAppBar
@Composable
fun ChapterListScreenView(params: ChapterListScreenParams, projectViewModel: ProjectViewModel = ProjectViewModel() /* TODO: Review ViewModel instantiation */) {
    // val projectViewModel: ProjectViewModel = rememberScreenModel { ProjectViewModel() } // Removed: Voyager
    // val navigator = LocalNavigator.currentOrThrow // Removed: Voyager

    var showEditChapterDialog by remember { mutableStateOf(false) }
    var editingChapter by remember { mutableStateOf<Chapter?>(null) } // Corrected type
    var editChapterTitleInput by remember { mutableStateOf("") }

    val projects by projectViewModel.projects.collectAsState()
    val currentProject = remember(projects, params.projectId) { // Used params
        projects.find { it.id == params.projectId }
    }

    LaunchedEffect(currentProject) {
        currentProject?.let { projectViewModel.selectProject(it) }
    }

    val selectedProjectDetails by projectViewModel.selectedProject.collectAsState()
    val chapters by projectViewModel.selectedProjectChapters.collectAsState() // Corrected indentation

    Scaffold(
        topBar = {
                TopAppBar(
                    title = { Text(selectedProjectDetails?.name ?: "Chapters") },
                    navigationIcon = {
                        TextButton(onClick = { /* TODO: Navigate back */ /* navigator.pop() */ }) { // Removed: Voyager
                            Text("Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                if (selectedProjectDetails != null) {
                    var newChapterTitle by remember { mutableStateOf("") }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
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
                                projectViewModel.addChapter(selectedProjectDetails!!, newChapterTitle)
                                newChapterTitle = ""
                            }
                        }) {
                            Text("Add Chapter")
                        }
                    }

                    if (chapters.isEmpty()) {
                        Text("No chapters yet. Add one above.", modifier = Modifier.padding(16.dp))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(chapters, key = { it.id }) { chapter ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            // TODO: Navigate to ChapterActionChoiceScreen(projectId = params.projectId, chapterId = chapter.id)
                                            // navigator.push(ChapterActionChoiceScreen(projectId = projectId, chapterId = chapter.id)) // Removed: Voyager
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(chapter.title, modifier = Modifier.weight(1f))
                                    TextButton(onClick = {
                                        editingChapter = chapter
                                        editChapterTitleInput = chapter.title
                                        showEditChapterDialog = true
                                    }) {
                                        Text("Edit Title")
                                    }
                                    TextButton(onClick = {
                                        selectedProjectDetails?.let { proj ->
                                            projectViewModel.deleteChapter(proj, chapter.id)
                                        }
                                    }) {
                                        Text("Delete") // Translated
                                    }
                                    TextButton(
                                        onClick = { projectViewModel.moveChapter(selectedProjectDetails!!, chapter.id, true) },
                                        enabled = chapter.order > 0
                                    ) {
                                        Text("Up")
                                    }
                                    TextButton(
                                        onClick = { projectViewModel.moveChapter(selectedProjectDetails!!, chapter.id, false) },
                                        enabled = chapter.order < chapters.size - 1
                                    ) {
                                        Text("Down") // Translated
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                } else {
                    Text("Loading project details...")
                }
            }
        }

        if (showEditChapterDialog && editingChapter != null && selectedProjectDetails != null) {
            br.com.marconardes.storyflame.view.EditChapterTitleDialog(
                editingChapter = editingChapter,
                editChapterTitleInput = editChapterTitleInput,
                onTitleChange = { editChapterTitleInput = it },
                onSave = {
                    projectViewModel.updateChapterTitle(selectedProjectDetails!!, editingChapter!!.id, editChapterTitleInput)
                    showEditChapterDialog = false
                    editingChapter = null
                },
                onDismiss = {
                    showEditChapterDialog = false
                    editingChapter = null
                }
            )
        }
    // Removed one extra closing brace that was here for ChapterListScreenView
}

data class ChapterEditorScreenParams(val projectId: String, val chapterId: String, val initialFocus: String? = null) // Renamed from ChapterEditorScreen, removed : Screen
// No longer an override

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterEditorScreenView(params: ChapterEditorScreenParams, projectViewModel: ProjectViewModel = ProjectViewModel() /* TODO: Review ViewModel instantiation */) {
    // val projectViewModel: ProjectViewModel = rememberScreenModel { ProjectViewModel() } // Removed: Voyager
    // val navigator = LocalNavigator.currentOrThrow // Removed: Voyager

    var summaryInput by remember { mutableStateOf("") }

    val projects by projectViewModel.projects.collectAsState()
    val project = remember(projects, params.projectId) { projects.find { it.id == params.projectId } } // Used params

    LaunchedEffect(project) {
        project?.let { projectViewModel.selectProject(it) }
    }

    val selectedProject by projectViewModel.selectedProject.collectAsState()
    val chapter = remember(selectedProject, params.chapterId) { // Used params
        selectedProject?.chapters?.find { it.id == params.chapterId }
    }

    LaunchedEffect(chapter) {
        chapter?.let { summaryInput = it.summary }
    }

    var markdownInput by remember(chapter?.content) { mutableStateOf(chapter?.content ?: "") }

    // Auto-save for Markdown editor
    LaunchedEffect(Unit) { // Runs once, snapshotFlow handles recomposition
        snapshotFlow { markdownInput }
                .debounce(1000L) // 1-second debounce
                .collectLatest { newContent ->
                    if (project != null && chapter != null) {
                        if (newContent != chapter.content) { // Only save if different
                            projectViewModel.updateChapterContent(project, chapter.id, newContent)
                        }
                    }
                }
    } // Closing brace for LaunchedEffect(Unit)

    Scaffold(
        topBar = {
                TopAppBar(
                    title = { Text(chapter?.title ?: "Edit Chapter") },
                    navigationIcon = {
                        TextButton(onClick = { /* TODO: Navigate back */ /* navigator.pop() */ }) { // Removed: Voyager
                            Text("Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            if (chapter != null && project != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                    // .verticalScroll(rememberScrollState()) // Scroll will be handled by individual editors if needed or by weight
                ) {
                    when (params.initialFocus) { // Used params
                        "summary" -> {
                            Text("Summary:", style = MaterialTheme.typography.titleMedium)
                            OutlinedTextField(
                                value = summaryInput,
                                onValueChange = { summaryInput = it },
                                label = { Text("Chapter Summary") },
                                modifier = Modifier.fillMaxWidth().weight(1f), // Use weight
                                maxLines = 10
                            )
                            Button(
                                onClick = {
                                    projectViewModel.updateChapterSummary(project, chapter.id, summaryInput)
                                    // TODO: Navigate back (optional)
                                    // navigator.pop() // Optional: pop back after saving // Removed: Voyager
                                },
                                modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                            ) {
                                Text("Save Summary")
                            }
                        }
                        "content" -> {
                            Text("Content (Markdown):", style = MaterialTheme.typography.titleMedium)
                            OutlinedTextField(
                                value = markdownInput,
                                onValueChange = { markdownInput = it },
                                label = { Text("Markdown Content") },
                                modifier = Modifier.fillMaxWidth().weight(1f), // Use weight
                                singleLine = false
                            )
                            // Auto-save for markdownInput is already handled by a LaunchedEffect
                        }
                        else -> {
                            // Default case if initialFocus is null or unexpected
                            Text("Please select an action from the previous screen (summary or content).", // Translated
                                 modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp))
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Loading chapter details...") // This was already English, fine.
                }
            }
        }
} // Added missing closing brace for ChapterEditorScreenView
