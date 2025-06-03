package br.com.marconardes.viewmodel

// Expected functions for platform-specific file I/O
expect fun saveProjectsToFile(jsonString: String)
expect fun loadProjectsFromFile(): String?
