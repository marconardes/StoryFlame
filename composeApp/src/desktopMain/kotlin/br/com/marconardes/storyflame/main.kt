package br.com.marconardes.storyflame

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import br.com.marconardes.storyflame.navigation.ChapterActionChoiceScreenParams
import br.com.marconardes.storyflame.navigation.ChapterActionChoiceScreenView
import br.com.marconardes.storyflame.navigation.ChapterEditorScreenParams
import br.com.marconardes.storyflame.navigation.ChapterEditorScreenView
import br.com.marconardes.storyflame.navigation.CustomNavigator
import br.com.marconardes.storyflame.navigation.Routes
import br.com.marconardes.storyflame.view.ChapterActionChoiceDialog
import br.com.marconardes.storyflame.view.ChapterSectionView
import br.com.marconardes.storyflame.view.EditChapterTitleDialog
import br.com.marconardes.storyflame.view.ProjectListView
import br.com.marconardes.model.Chapter // Required for dialog states
import br.com.marconardes.viewmodel.ProjectViewModel

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "StoryFlame",
    ) {
        // App() // App Composable is in commonMain
        // For desktop, we directly use AppNavigationHost or a desktop-specific root.
        // Here, we'll use a simplified App calling AppNavigationHost.
        // This is to keep main.kt clean and App.kt as the common entry point.
        App()
    }
}

@Composable
fun MainDesktopScreen(
    projectViewModel: ProjectViewModel,
    onNavigateToChapterActionChoice: (projectId: String, chapterId: String) -> Unit,
    onNavigateToChapterEditor: (projectId: String, chapterId: String, initialFocus: String?) -> Unit,
    // onNavigateBack: () -> Unit // Will be handled by navigator if these become separate screens
) {
    val selectedProject by projectViewModel.selectedProject.collectAsState()
    val selectedProjectChapters by projectViewModel.selectedProjectChapters.collectAsState()
    val projects by projectViewModel.projects.collectAsState()

    // State for EditChapterTitleDialog
    var showEditChapterTitleDialog by remember { mutableStateOf(false) }
    var chapterForTitleEdit by remember { mutableStateOf<Chapter?>(null) }
    var editChapterTitleInput by remember { mutableStateOf("") }

    // State for ChapterActionChoiceDialog
    var showChapterActionChoiceDialog by remember { mutableStateOf(false) }
    var chapterForActionChoice by remember { mutableStateOf<Chapter?>(null) }

    // State for ChapterEditorScreenView in detail pane
    var editingChapterId by remember { mutableStateOf<String?>(null) }
    var editingFocus by remember { mutableStateOf<String?>(null) } // "summary", "content", or "title"
    val isEditorActive by remember { derivedStateOf { selectedProject != null && editingChapterId != null && editingFocus != null } }


    Scaffold(
        topBar = {
            TopAppBar(title = { Text("StoryFlame - Desktop") })
        }
    ) { paddingValues ->
        Row(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Box(modifier = Modifier.weight(0.3f)) {
                ProjectListView(
                    projects = projects,
                    selectedProject = selectedProject,
                    onProjectSelected = { project ->
                        editingChapterId = null // Clear editor when project changes
                        editingFocus = null
                        projectViewModel.selectProject(project)
                    },
                    onAddNewProject = { projectViewModel.addNewProject() },
                    onDeleteProject = { project ->
                        projectViewModel.deleteProject(project.id)
                        if (selectedProject?.id == project.id) { // If deleted project was selected
                            editingChapterId = null // Clear editor
                            editingFocus = null
                        }
                    }
                )
            }
            Box(modifier = Modifier.weight(0.7f)) {
                if (isEditorActive) {
                    ChapterEditorScreenView(
                        params = ChapterEditorScreenParams(
                            projectId = selectedProject!!.id, // isEditorActive ensures selectedProject is not null
                            chapterId = editingChapterId!!,    // isEditorActive ensures editingChapterId is not null
                            initialFocus = editingFocus
                        ),
                        projectViewModel = projectViewModel,
                        onNavigateBack = {
                            editingChapterId = null
                            editingFocus = null
                        }
                    )
                } else if (selectedProject != null) {
                    ChapterSectionView(
                        selectedProject = selectedProject!!,
                        chapters = selectedProjectChapters,
                        projectViewModel = projectViewModel,
                        onShowEditChapterDialog = { chapter ->
                            chapterForTitleEdit = chapter
                            editChapterTitleInput = chapter.title
                            showEditChapterTitleDialog = true
                        },
                        onShowChapterActionsDialog = { chapter ->
                            chapterForActionChoice = chapter
                            showChapterActionChoiceDialog = true
                        },
                        onAddNewChapter = { project, title ->
                            val newChapter = projectViewModel.addChapter(project, title)
                            if (newChapter != null) {
                                editingChapterId = newChapter.id
                                editingFocus = Routes.CHAPTER_EDITOR_ARG_FOCUS_SUMMARY // Default to summary for new chapters
                            }
                        }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Select a project to see details.")
                    }
                }
            }
        }
    }

    if (showEditChapterTitleDialog) {
        EditChapterTitleDialog(
            editingChapter = chapterForTitleEdit,
            editChapterTitleInput = editChapterTitleInput,
            onTitleChange = { editChapterTitleInput = it },
            onDismiss = { showEditChapterTitleDialog = false },
            onSave = {
                chapterForTitleEdit?.let { chapter ->
                    selectedProject?.let { project ->
                        projectViewModel.updateChapterTitle(project, chapter.id, editChapterTitleInput)
                    }
                }
                showEditChapterTitleDialog = false
            }
        )
    }

    if (showChapterActionChoiceDialog) {
        ChapterActionChoiceDialog(
            chapter = chapterForActionChoice,
            onDismiss = { showChapterActionChoiceDialog = false },
            onEditSummary = {
                editingChapterId = chapterForActionChoice?.id
                editingFocus = Routes.CHAPTER_EDITOR_ARG_FOCUS_SUMMARY
                showChapterActionChoiceDialog = false
            },
            onEditContent = {
                editingChapterId = chapterForActionChoice?.id
                editingFocus = Routes.CHAPTER_EDITOR_ARG_FOCUS_CONTENT
                showChapterActionChoiceDialog = false
            }
        )
    }
}

@Composable
internal actual fun AppNavigationHost() {
    val navigator = remember { CustomNavigator(initialRoute = Routes.PROJECT_LIST) }
    val currentRoute = navigator.currentRoute.value
    val projectViewModel: ProjectViewModel = remember { ProjectViewModel() }

    // MainDesktopScreen will be shown for PROJECT_LIST and act as the base layout.
    // Other routes will navigate "away" from it or appear on top (e.g. dialogs now handled within MainDesktopScreen).
    // For now, other routes replace MainDesktopScreen.

    if (currentRoute == Routes.PROJECT_LIST || currentRoute?.startsWith(Routes.CHAPTER_LIST_PREFIX) == true) {
        // The MainDesktopScreen now handles project selection, chapter display, and dialogs for chapter actions.
        // So, CHAPTER_LIST_PREFIX routes effectively show MainDesktopScreen,
        // with the project selected based on the old logic if we wanted to deep link.
        // For now, selecting a project is manual via ProjectListView.
        // If a projectId is part of the route (e.g. from a deep link for CHAPTER_LIST_PREFIX),
        // we could potentially use it to pre-select a project.
        // However, the current MainDesktopScreen design relies on user interaction.
        // We will simplify and assume PROJECT_LIST is the entry to MainDesktopScreen.
        // If currentRoute contains a projectId (e.g. from an old CHAPTER_LIST route),
        // we could extract it and use it to select the project initially.
        // For now, let's keep it simple: PROJECT_LIST shows MainDesktopScreen.
        // If we want to handle deep links to projects, that's an enhancement.

        val routeParts = currentRoute?.split("/") ?: emptyList()
        val projectIdFromRoute = if (routeParts.isNotEmpty() && routeParts[0] == Routes.CHAPTER_LIST_PREFIX) {
            routeParts.getOrNull(1)
        } else {
            null
        }

        LaunchedEffect(projectIdFromRoute, projectViewModel) {
            if (projectIdFromRoute != null) {
                // TODO: This needs access to the full project list to find the project by ID.
                // For now, this selection logic will be manual in ProjectListView.
                // Consider adding a method to ProjectViewModel to select project by ID.
                // projectViewModel.selectProjectById(projectIdFromRoute)
            }
        }

        MainDesktopScreen(
            projectViewModel = projectViewModel,
            onNavigateToChapterActionChoice = { projId, chapId -> navigator.push(Routes.chapterActionChoice(projId, chapId)) },
            onNavigateToChapterEditor = { projId, chapId, focus -> navigator.push(Routes.chapterEditor(projId, chapId, focus)) }
        )
    } else {
        // Handle other routes (CHAPTER_ACTION_CHOICE, CHAPTER_EDITOR) as full-screen navigations for now
        val routeParts = currentRoute?.split("?")?.first()?.split("/") ?: emptyList()
        val queryParams = currentRoute?.split("?")?.getOrNull(1)?.split("&")
            ?.mapNotNull { it.split("=").let { if (it.size == 2) it[0] to it[1] else null } }
            ?.toMap() ?: emptyMap()

        if (routeParts.isNotEmpty()) {
            when (routeParts[0]) {
                // CHAPTER_LIST_PREFIX is handled by MainDesktopScreen now
                // No need for ChapterListScreenView anymore for desktop if MainDesktopScreen is used.

                Routes.CHAPTER_ACTION_CHOICE_PREFIX -> {
                    val projectId = routeParts.getOrNull(1)
                    val chapterId = routeParts.getOrNull(2)
                    if (projectId != null && chapterId != null) {
                        ChapterActionChoiceScreenView(
                            params = ChapterActionChoiceScreenParams(projectId = projectId, chapterId = chapterId),
                            projectViewModel = projectViewModel,
                            onNavigateToChapterEditor = { projId, chapId, focus -> navigator.push(Routes.chapterEditor(projId, chapId, focus)) },
                            onNavigateBack = { navigator.pop() }
                        )
                    } else {
                        LaunchedEffect(currentRoute) { navigator.replace(Routes.PROJECT_LIST) }
                    }
                }
                Routes.CHAPTER_EDITOR_PREFIX -> {
                    val projectId = routeParts.getOrNull(1)
                    val chapterId = routeParts.getOrNull(2)
                    val initialFocus = queryParams[Routes.CHAPTER_EDITOR_ARG_INITIAL_FOCUS]
                    if (projectId != null && chapterId != null) {
                        ChapterEditorScreenView(
                            params = ChapterEditorScreenParams(projectId = projectId, chapterId = chapterId, initialFocus = initialFocus),
                            projectViewModel = projectViewModel,
                            onNavigateBack = { navigator.pop() }
                        )
                    } else {
                        LaunchedEffect(currentRoute) { navigator.replace(Routes.PROJECT_LIST) }
                    }
                }
                else -> {
                    LaunchedEffect(currentRoute) { navigator.replace(Routes.PROJECT_LIST) }
                }
            }
        } else if (currentRoute == null && navigator.getBackStack().isEmpty()) {
            LaunchedEffect(currentRoute) { navigator.replace(Routes.PROJECT_LIST) }
        }
        // If currentRoute is null but stack is not empty, CustomNavigator should handle it.
    }
}