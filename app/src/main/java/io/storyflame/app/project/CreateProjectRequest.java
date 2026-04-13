package io.storyflame.app.project;

public record CreateProjectRequest(String title, String author) {
    public CreateProjectRequest {
        title = title == null || title.isBlank() ? "Novo Projeto" : title;
        author = author == null || author.isBlank() ? "Autor" : author;
    }
}
