package br.com.marconardes.storyflame.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.marconardes.model.Chapter

@Composable
fun ChapterActionChoiceDialog(
    chapter: Chapter?, // Nullable, dialog is shown only if not null
    onDismiss: () -> Unit,
    onEditSummary: () -> Unit,
    onEditContent: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (chapter == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chapter Actions: ${chapter.title}") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("What would you like to do with this chapter?")
            }
        },
        confirmButton = {
            // Using confirm button area for primary actions, dismiss for cancel.
            // Or structure with TextButtons in the Column body.
            // For now, let's use the button slots.
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = modifier,
        // Override buttons to have multiple choices
        // This is not standard AlertDialog behavior, better to put buttons in the content
    )
    // Re-thinking AlertDialog structure for multiple actions.
    // It's better to put action buttons within the `text` composable or use a custom dialog.
    // Let's use a simpler structure with buttons in the content.

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chapter Actions: ${chapter.title}") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text("Choose an action for '${chapter.title}':", modifier = Modifier.padding(bottom = 16.dp))
                Button(
                    onClick = {
                        onEditSummary()
                        onDismiss() // Dismiss after action chosen
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Edit Summary")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        onEditContent()
                        onDismiss() // Dismiss after action chosen
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Edit Content (Markdown)")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        // No dismiss button if confirm acts as close. Or keep both.
        // Let's make confirm button the "Close" action.
        modifier = modifier
    )
}
