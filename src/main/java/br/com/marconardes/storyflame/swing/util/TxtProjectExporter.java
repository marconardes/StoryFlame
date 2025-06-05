package br.com.marconardes.storyflame.swing.util;

import br.com.marconardes.storyflame.swing.model.Chapter;
import br.com.marconardes.storyflame.swing.model.Project;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TxtProjectExporter {

    public void exportProject(Project project, Path filePath) throws IOException {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null.");
        }
        if (filePath == null) {
            throw new IllegalArgumentException("File path cannot be null.");
        }

        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            // Write Project Title
            writer.write("Project Title: " + (project.getName() != null ? project.getName() : "Untitled Project"));
            writer.newLine();
            writer.newLine();

            List<Chapter> chapters = project.getChapters();
            if (chapters == null || chapters.isEmpty()) {
                writer.write("This project has no chapters.");
                writer.newLine();
            } else {
                for (int i = 0; i < chapters.size(); i++) {
                    Chapter chapter = chapters.get(i);
                    writer.write("----------------------------------------");
                    writer.newLine();
                    writer.write("Chapter " + (i + 1) + ": " + (chapter.getTitle() != null ? chapter.getTitle() : "Untitled Chapter"));
                    writer.newLine();
                    writer.newLine();

                    writer.write("Summary:");
                    writer.newLine();
                    writer.write(chapter.getSummary() != null && !chapter.getSummary().isEmpty() ? chapter.getSummary() : "No summary provided.");
                    writer.newLine();
                    writer.newLine();

                    writer.write("Content:");
                    writer.newLine();
                    writer.write(chapter.getContent() != null && !chapter.getContent().isEmpty() ? chapter.getContent() : "No content provided.");
                    writer.newLine();
                    writer.newLine();
                }
            }
            // Add a final separator for clarity if there were chapters
            if (chapters != null && !chapters.isEmpty()) {
                 writer.write("----------------------------------------");
                 writer.newLine();
                 writer.write("End of Project Export");
                 writer.newLine();
            }

        } catch (IOException e) {
            // Log error or wrap in a custom exception if needed for UI handling
            System.err.println("Error exporting project to TXT: " + e.getMessage());
            throw e; // Re-throw for the caller to handle (e.g., show a dialog)
        }
    }
}
