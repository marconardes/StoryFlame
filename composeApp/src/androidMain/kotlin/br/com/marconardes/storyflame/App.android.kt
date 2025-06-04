package br.com.marconardes.storyflame

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.marconardes.storyflame.navigation.Routes // Assuming Routes.kt is in this package
import br.com.marconardes.storyflame.navigation.ChapterActionChoiceScreenParams
import br.com.marconardes.storyflame.navigation.ChapterActionChoiceScreenView
import br.com.marconardes.storyflame.navigation.ChapterEditorScreenParams
import br.com.marconardes.storyflame.navigation.ChapterEditorScreenView
import br.com.marconardes.storyflame.navigation.ChapterListScreenParams
import br.com.marconardes.storyflame.navigation.ChapterListScreenView
import br.com.marconardes.storyflame.navigation.ProjectListScreenView
import br.com.marconardes.viewmodel.ProjectViewModel // Assuming ProjectViewModel is accessible

@Composable
internal actual fun AppNavigationHost() {
    val navController = rememberNavController()
    // TODO: Review ViewModel instantiation strategy for Android.
    // Consider using hiltViewModel() or viewModel() with graph-scoped ViewModels if complex sharing is needed.
    val projectViewModel: ProjectViewModel = viewModel()

    NavHost(navController = navController, startDestination = Routes.PROJECT_LIST) {
        composable(Routes.PROJECT_LIST) {
            ProjectListScreenView(
                projectViewModel = projectViewModel,
                onNavigateToChapterList = { projectId -> navController.navigate(Routes.chapterList(projectId)) }
            )
        }

        composable(
            route = Routes.CHAPTER_LIST_ROUTE_PATTERN,
            arguments = listOf(navArgument(Routes.CHAPTER_LIST_ARG_PROJECT_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString(Routes.CHAPTER_LIST_ARG_PROJECT_ID)
            if (projectId != null) {
                ChapterListScreenView(
                    params = ChapterListScreenParams(projectId = projectId),
                    projectViewModel = projectViewModel,
                    onNavigateToChapterActionChoice = { projId, chapId -> navController.navigate(Routes.chapterActionChoice(projId, chapId)) },
                    onNavigateBack = { navController.popBackStack() }
                )
            } else {
                // Handle missing argument, e.g., navigate back or show error
                navController.popBackStack()
            }
        }

        composable(
            route = Routes.CHAPTER_ACTION_CHOICE_ROUTE_PATTERN,
            arguments = listOf(
                navArgument(Routes.CHAPTER_ACTION_CHOICE_ARG_PROJECT_ID) { type = NavType.StringType },
                navArgument(Routes.CHAPTER_ACTION_CHOICE_ARG_CHAPTER_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString(Routes.CHAPTER_ACTION_CHOICE_ARG_PROJECT_ID)
            val chapterId = backStackEntry.arguments?.getString(Routes.CHAPTER_ACTION_CHOICE_ARG_CHAPTER_ID)
            if (projectId != null && chapterId != null) {
                ChapterActionChoiceScreenView(
                    params = ChapterActionChoiceScreenParams(projectId = projectId, chapterId = chapterId),
                    projectViewModel = projectViewModel,
                    onNavigateToChapterEditor = { projId, chapId, focus -> navController.navigate(Routes.chapterEditor(projId, chapId, focus)) },
                    onNavigateBack = { navController.popBackStack() }
                )
            } else {
                navController.popBackStack()
            }
        }

        composable(
            route = Routes.CHAPTER_EDITOR_ROUTE_PATTERN,
            arguments = listOf(
                navArgument(Routes.CHAPTER_EDITOR_ARG_PROJECT_ID) { type = NavType.StringType },
                navArgument(Routes.CHAPTER_EDITOR_ARG_CHAPTER_ID) { type = NavType.StringType },
                navArgument(Routes.CHAPTER_EDITOR_ARG_INITIAL_FOCUS) { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString(Routes.CHAPTER_EDITOR_ARG_PROJECT_ID)
            val chapterId = backStackEntry.arguments?.getString(Routes.CHAPTER_EDITOR_ARG_CHAPTER_ID)
            val initialFocus = backStackEntry.arguments?.getString(Routes.CHAPTER_EDITOR_ARG_INITIAL_FOCUS)
            if (projectId != null && chapterId != null) {
                ChapterEditorScreenView(
                    params = ChapterEditorScreenParams(projectId = projectId, chapterId = chapterId, initialFocus = initialFocus),
                    projectViewModel = projectViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            } else {
                navController.popBackStack()
            }
        }
    }
}

// Helper function placeholder for ViewModel provision - to be refined in ViewModel step // Removed as ViewModel is now directly instantiated
// @Composable
// fun provideProjectViewModel(): ProjectViewModel {
//     // return androidx.lifecycle.viewmodel.compose.viewModel() // Example using lifecycle-viewmodel-compose
//     return ProjectViewModel() // Current simple instantiation
// }
