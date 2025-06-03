package br.com.marconardes.storyflame.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import br.com.marconardes.model.Chapter

@Composable
fun EditChapterTitleDialog(
    editingChapter: Chapter?, // Nullable, dialog is shown only if not null
    editChapterTitleInput: String,
    onTitleChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (editingChapter != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Edit Chapter Title") },
            text = {
                OutlinedTextField(
                    value = editChapterTitleInput,
                    onValueChange = onTitleChange,
                    label = { Text("Chapter Title") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = onSave) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            },
            modifier = modifier
        )
    }
}
