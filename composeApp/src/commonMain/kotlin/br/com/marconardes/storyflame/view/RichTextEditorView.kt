package br.com.marconardes.storyflame.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun RichTextEditorView(state: RichTextState, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(onClick = { state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)) }) {
                Text("Bold")
            }
            Button(onClick = { state.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic)) }) {
                Text("Italic")
            }
            Button(onClick = { state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline)) }) {
                Text("Underline")
            }
            Button(onClick = { state.toggleSpanStyle(SpanStyle(fontSize = 24.sp)) }) {
                Text("H1")
            }
            Button(onClick = { state.toggleSpanStyle(SpanStyle(fontSize = 20.sp)) }) {
                Text("H2")
            }
            Button(onClick = { state.toggleSpanStyle(SpanStyle(fontSize = 18.sp)) }) {
                Text("H3")
            }
        }
        RichTextEditor(
            state = state,
            modifier = Modifier.fillMaxWidth().weight(1f)
        )
    }
}

@Preview
@Composable
fun RichTextEditorViewPreview() {
    val richTextState = rememberRichTextState()
    RichTextEditorView(state = richTextState)
}
