package br.com.marconardes.storyflame.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import cafe.adriel.voyager.core.model.rememberScreenModel // Added for ScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
// Keep br.com.marconardes.storyflame.view.EditChapterTitleDialog fully qualified in usage or add import here

object ProjectListScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class) // For Scaffold and TopAppBar
    @Composable
    override fun Content() {
        val projectViewModel: ProjectViewModel = rememberScreenModel { ProjectViewModel() }
        val projects by projectViewModel.projects.collectAsState()
        val selectedProject by projectViewModel.selectedProject.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

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
                        navigator.push(ChapterListScreen(projectId = project.id))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

data class ChapterListScreen(val projectId: String) : Screen {
    @OptIn(ExperimentalMaterial3Api::class) // For Scaffold, TopAppBar
    @Composable
    override fun Content() {
        val projectViewModel: ProjectViewModel = rememberScreenModel { ProjectViewModel() }
        val navigator = LocalNavigator.currentOrThrow

        var showEditChapterDialog by remember { mutableStateOf(false) }
        var editingChapter by remember { mutableStateOf<Chapter?>(null) } // Corrected type
        var editChapterTitleInput by remember { mutableStateOf("") }

        val projects by projectViewModel.projects.collectAsState()
        val currentProject = remember(projects, projectId) {
            projects.find { it.id == projectId }
        }

        LaunchedEffect(currentProject) {
            currentProject?.let { projectViewModel.selectProject(it) }
        }

        val selectedProjectDetails by projectViewModel.selectedProject.collectAsState()
        val chapters by projectViewModel.selectedProjectChapters.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(selectedProjectDetails?.name ?: "Chapters") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
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
                                            navigator.push(ChapterEditorScreen(projectId = projectId, chapterId = chapter.id))
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(chapter.title, modifier = Modifier.weight(1f))
                                    IconButton(onClick = {
                                        editingChapter = chapter
                                        editChapterTitleInput = chapter.title
                                        showEditChapterDialog = true
                                    }) {
                                        Icon(
                                            imageVector = Icons.Filled.Edit,
                                            contentDescription = "Edit Title"
                                        )
                                    }
                                    IconButton(onClick = {
                                        selectedProjectDetails?.let { proj ->
                                            projectViewModel.deleteChapter(proj, chapter.id)
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Delete Chapter"
                                        )
                                    }
                                    IconButton(
                                        onClick = { projectViewModel.moveChapter(selectedProjectDetails!!, chapter.id, true) },
                                        enabled = chapter.order > 0
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.KeyboardArrowUp,
                                            contentDescription = "Move Up"
                                        )
                                    }
                                    IconButton(
                                        onClick = { projectViewModel.moveChapter(selectedProjectDetails!!, chapter.id, false) },
                                        enabled = chapter.order < chapters.size - 1
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.KeyboardArrowDown,
                                            contentDescription = "Move Down"
                                        )
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
    }
}

data class ChapterEditorScreen(val projectId: String, val chapterId: String) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val projectViewModel: ProjectViewModel = rememberScreenModel { ProjectViewModel() }
        val navigator = LocalNavigator.currentOrThrow

        var summaryInput by remember { mutableStateOf("") }

        val projects by projectViewModel.projects.collectAsState()
        val project = remember(projects, projectId) { projects.find { it.id == projectId } }

        LaunchedEffect(project) {
            project?.let { projectViewModel.selectProject(it) }
        }

        val selectedProject by projectViewModel.selectedProject.collectAsState()
        val chapter = remember(selectedProject, chapterId) {
            selectedProject?.chapters?.find { it.id == chapterId }
        }

        LaunchedEffect(chapter) {
            chapter?.let { summaryInput = it.summary }
        }

        val editorState = com.mohamedrejeb.richeditor.model.rememberRichTextState() // Correct way to get the state

        LaunchedEffect(chapter?.content) {
            // Ensure not to reset if editor already has user's untrimmed input or if content is same
            val currentEditorHtml = editorState.toHtml()
            if (chapter?.content != null && chapter.content != currentEditorHtml) {
                 editorState.setHtml(chapter.content!!)
            } else if (chapter?.content == null && currentEditorHtml.isNotEmpty() && currentEditorHtml != "<p></p>") {
                // If chapter content becomes null (e.g. error), but editor had content, clear it.
                // editorState.setHtml("") // Or decide if you want to keep stale content
            }
        }

        LaunchedEffect(editorState, project, chapter) {
            snapshotFlow { editorState.toHtml() }
                .debounce(1000L)
                .collectLatest { contentHtml ->
                    if (project != null && chapter != null && chapter.content != contentHtml) {
                         // Avoid saving if contentHtml is just the default empty state from the editor
                        if (contentHtml.isNotBlank() && contentHtml != "<p></p>" || chapter.content?.isNotEmpty() == true) {
                            projectViewModel.updateChapterContent(project, chapter.id, contentHtml)
                        }
                    }
                }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(chapter?.title ?: "Edit Chapter") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Summary:", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = summaryInput,
                        onValueChange = { summaryInput = it },
                        label = { Text("Chapter Summary") },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        maxLines = 5
                    )
                    Button(
                        onClick = {
                            projectViewModel.updateChapterSummary(project, chapter.id, summaryInput)
                        },
                        modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                    ) {
                        Text("Save Summary")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Content:", style = MaterialTheme.typography.titleMedium)
                    br.com.marconardes.storyflame.view.RichTextEditorView( // Using FQN for clarity
                        state = editorState,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 400.dp) // Use heightIn for min height
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Loading chapter details...")
                }
            }
        }
    }
}
