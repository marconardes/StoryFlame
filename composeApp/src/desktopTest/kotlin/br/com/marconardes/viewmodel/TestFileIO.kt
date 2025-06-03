package br.com.marconardes.viewmodel

// Mock implementations for desktopTest, using variables defined in ProjectViewModelTest.kt (commonTest)

actual fun saveProjectsToFile(jsonString: String) {
    // These refer to the top-level vars in ProjectViewModelTest.kt in the same package (br.com.marconardes.viewmodel)
    // commonTest sources are available to desktopTest
    mockSavedJsonDataForTest = jsonString
}

actual fun loadProjectsFromFile(): String? {
    return mockLoadedJsonDataForTest
}
