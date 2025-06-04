package br.com.marconardes.storyflame

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "StoryFlame",
    ) {
        App()
    }
}

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import br.com.marconardes.storyflame.navigation.CustomNavigator
import br.com.marconardes.storyflame.navigation.Routes
import br.com.marconardes.storyflame.navigation.ChapterActionChoiceScreenParams
import br.com.marconardes.storyflame.navigation.ChapterActionChoiceScreenView
import br.com.marconardes.storyflame.navigation.ChapterEditorScreenParams
import br.com.marconardes.storyflame.navigation.ChapterEditorScreenView
import br.com.marconardes.storyflame.navigation.ChapterListScreenParams
import br.com.marconardes.storyflame.navigation.ChapterListScreenView
import br.com.marconardes.storyflame.navigation.ProjectListScreenView
import br.com.marconardes.viewmodel.ProjectViewModel // Import ProjectViewModel

@Composable
internal actual fun AppNavigationHost() {
    val navigator = remember { CustomNavigator(initialRoute = Routes.PROJECT_LIST) }
    val currentRoute = navigator.currentRoute.value

    // TODO: Review ViewModel instantiation for Desktop.
    val projectViewModel: ProjectViewModel = remember { ProjectViewModel() }

    when (currentRoute) {
        Routes.PROJECT_LIST -> {
            ProjectListScreenView(
                projectViewModel = projectViewModel,
                onNavigateToChapterList = { projectId -> navigator.push(Routes.chapterList(projectId)) }
            )
        }
        else -> {
            // Handling routes with parameters for CustomNavigator
            // This requires parsing the route string.
            // Example: "chapter_list/projectId123"
            // Example: "chapter_editor/projectId123/chapterId456?initialFocus=summary"

            val routeParts = currentRoute?.split("?")?.first()?.split("/") ?: emptyList()
            val queryParams = currentRoute?.split("?")?.getOrNull(1)?.split("&")
                ?.mapNotNull { it.split("=").let { if (it.size == 2) it[0] to it[1] else null } }
                ?.toMap() ?: emptyMap()

            if (routeParts.isNotEmpty()) {
                when (routeParts[0]) {
                    Routes.CHAPTER_LIST_PREFIX -> {
                        val projectId = routeParts.getOrNull(1)
                        if (projectId != null) {
                            ChapterListScreenView(
                                params = ChapterListScreenParams(projectId = projectId),
                                projectViewModel = projectViewModel,
                                onNavigateToChapterActionChoice = { projId, chapId -> navigator.push(Routes.chapterActionChoice(projId, chapId)) },
                                onNavigateBack = { navigator.pop() }
                            )
                        } else {
                             // Fallback or error: Invalid route, go to default
                            LaunchedEffect(currentRoute) { navigator.replace(Routes.PROJECT_LIST) }
                        }
                    }
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
                        // Unknown route, navigate to default
                        // Or display a "Not Found" screen
                        LaunchedEffect(currentRoute) { navigator.replace(Routes.PROJECT_LIST) }
                    }
                }
            } else if (currentRoute == null && navigator.getBackStack().isEmpty()) {
                // This case could occur if pop leads to an empty stack with no initial route defined for null
                // Or if initial route was null and nothing was pushed.
                // Depending on CustomNavigator's pop behavior for empty stack.
                // For safety, if currentRoute is null and stack is empty, redirect to PROJECT_LIST
                 LaunchedEffect(currentRoute) { navigator.replace(Routes.PROJECT_LIST) }
            }
            // If currentRoute is null but stack is not empty, it implies an issue or a state during pop
            // The CustomNavigator's pop should ideally set a valid route or null if stack becomes empty.
        }
    }
}