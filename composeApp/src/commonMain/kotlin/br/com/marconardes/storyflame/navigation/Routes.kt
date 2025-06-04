package br.com.marconardes.storyflame.navigation

object Routes {
    const val PROJECT_LIST = "project_list"

    // Route for ChapterListScreen, requires projectId
    const val CHAPTER_LIST_PREFIX = "chapter_list"
    fun chapterList(projectId: String) = "${CHAPTER_LIST_PREFIX}/${projectId}"
    const val CHAPTER_LIST_ARG_PROJECT_ID = "projectId"
    val CHAPTER_LIST_ROUTE_PATTERN = "${CHAPTER_LIST_PREFIX}/{${CHAPTER_LIST_ARG_PROJECT_ID}}"


    // Route for ChapterActionChoiceScreen, requires projectId and chapterId
    const val CHAPTER_ACTION_CHOICE_PREFIX = "chapter_action_choice"
    fun chapterActionChoice(projectId: String, chapterId: String) = "${CHAPTER_ACTION_CHOICE_PREFIX}/${projectId}/${chapterId}"
    const val CHAPTER_ACTION_CHOICE_ARG_PROJECT_ID = "projectId"
    const val CHAPTER_ACTION_CHOICE_ARG_CHAPTER_ID = "chapterId"
    val CHAPTER_ACTION_CHOICE_ROUTE_PATTERN = "${CHAPTER_ACTION_CHOICE_PREFIX}/{${CHAPTER_ACTION_CHOICE_ARG_PROJECT_ID}}/{${CHAPTER_ACTION_CHOICE_ARG_CHAPTER_ID}}"


    // Route for ChapterEditorScreen, requires projectId, chapterId, and optional initialFocus
    const val CHAPTER_EDITOR_PREFIX = "chapter_editor"
    fun chapterEditor(projectId: String, chapterId: String, initialFocus: String? = null): String {
        val baseRoute = "${CHAPTER_EDITOR_PREFIX}/${projectId}/${chapterId}"
        return if (initialFocus != null) "$baseRoute?initialFocus=$initialFocus" else baseRoute
    }
    const val CHAPTER_EDITOR_ARG_PROJECT_ID = "projectId"
    const val CHAPTER_EDITOR_ARG_CHAPTER_ID = "chapterId"
    const val CHAPTER_EDITOR_ARG_INITIAL_FOCUS = "initialFocus" // Query parameter key
    // Specific values for initialFocus
    const val CHAPTER_EDITOR_ARG_FOCUS_TITLE = "title"
    const val CHAPTER_EDITOR_ARG_FOCUS_SUMMARY = "summary"
    const val CHAPTER_EDITOR_ARG_FOCUS_CONTENT = "content"

    // Pattern for Jetpack Navigation (path arguments)
    val CHAPTER_EDITOR_ROUTE_PATTERN = "${CHAPTER_EDITOR_PREFIX}/{${CHAPTER_EDITOR_ARG_PROJECT_ID}}/{${CHAPTER_EDITOR_ARG_CHAPTER_ID}}"
    // Query parameter for initialFocus will be handled separately by Jetpack Navigation
}
