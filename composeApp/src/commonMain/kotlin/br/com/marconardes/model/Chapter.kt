package br.com.marconardes.model

import com.benasher44.uuid.uuid4
import kotlinx.serialization.Serializable

@Serializable
data class Chapter(
    val id: String = uuid4().toString(),
    val title: String,
    val order: Int, // Assuming manual order for now, can be creationTimestamp or similar
    val content: String = "", // Actual text content of the chapter
    val summary: String = "" // New property for chapter summary
)
