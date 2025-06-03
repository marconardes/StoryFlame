package br.com.marconardes.model

import com.benasher44.uuid.uuid4
import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val id: String = uuid4().toString(),
    val name: String,
    val creationDate: String,
    val chapters: MutableList<Chapter> = mutableListOf()
)
