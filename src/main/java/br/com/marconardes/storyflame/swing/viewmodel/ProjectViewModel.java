package br.com.marconardes.storyflame.swing.viewmodel;

import br.com.marconardes.storyflame.swing.model.Chapter;
import br.com.marconardes.storyflame.swing.model.Project;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
// DateTimeFormatter is already imported
// import java.time.format.DateTimeFormatter;
import java.util.Map; // For Map type in method signature
import br.com.marconardes.storyflame.swing.util.WordCounterUtil;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProjectViewModel {
    public static final String PROJECTS_PROPERTY = "projects";
    public static final String SELECTED_PROJECT_PROPERTY = "selectedProject";
    public static final String SELECTED_PROJECT_CHAPTERS_PROPERTY = "selectedProjectChapters";

    private List<Project> projects;
    private Project selectedProject;
    private final PropertyChangeSupport support;
    private final Gson gson;

    // private static final String FILENAME = "projects.json"; // Keep for reference or default behavior
    // private static final Path APP_DIR_PATH = Paths.get(System.getProperty("user.home"), ".storyflame"); // Keep for reference
    private final Path projectsFilePathInstance; // Instance-specific path

    public ProjectViewModel() {
        this(Paths.get(System.getProperty("user.home"), ".storyflame", "projects.json").toString());
    }

    public ProjectViewModel(String customProjectsPath) {
        this.projects = new ArrayList<>();
        this.support = new PropertyChangeSupport(this);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.projectsFilePathInstance = Paths.get(customProjectsPath);
        loadProjects();
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
    }

    public List<Project> getProjects() {
        return new ArrayList<>(projects); // Return a copy
    }

    public Project getSelectedProject() {
        return selectedProject;
    }

    public List<Chapter> getSelectedProjectChapters() {
        return selectedProject != null ? new ArrayList<>(selectedProject.getChapters()) : new ArrayList<>();
    }

    private void loadProjects() {
        try {
            if (Files.exists(this.projectsFilePathInstance)) {
                String jsonString = Files.readString(this.projectsFilePathInstance);
                if (jsonString != null && !jsonString.isBlank()) {
                    Type projectListType = new TypeToken<ArrayList<Project>>() {}.getType();
                    List<Project> loadedProjects = gson.fromJson(jsonString, projectListType);
                    if (loadedProjects != null) {
                        this.projects = loadedProjects;
                        support.firePropertyChange(PROJECTS_PROPERTY, null, new ArrayList<>(this.projects));
                        System.out.println("Successfully loaded projects from file: " + this.projectsFilePathInstance);
                        if (!this.projects.isEmpty()) {
                            selectProject(this.projects.get(0)); // Auto-select first project
                        }
                    } else {
                         this.projects = new ArrayList<>(); // gson.fromJson returned null
                         support.firePropertyChange(PROJECTS_PROPERTY, null, new ArrayList<>(this.projects));
                    }
                }
            } else {
                System.out.println("No saved projects found or file content is empty. Starting with an empty list.");
                this.projects = new ArrayList<>();
                support.firePropertyChange(PROJECTS_PROPERTY, null, new ArrayList<>(this.projects));
            }
        } catch (IOException e) {
            System.err.println("Error loading projects from file (" + this.projectsFilePathInstance + "): " + e.getMessage());
            this.projects = new ArrayList<>(); // Start with empty list on error
            support.firePropertyChange(PROJECTS_PROPERTY, null, new ArrayList<>(this.projects));
        }
        if (this.selectedProject == null && !this.projects.isEmpty()) {
            selectProject(this.projects.get(0));
        } else if (this.projects.isEmpty()) {
            selectProject(null); // Ensure no project is selected if list is empty
        }
    }

    private void saveProjects() {
        try {
            Path parentDir = this.projectsFilePathInstance.getParent();
            if (parentDir != null && Files.notExists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            String jsonString = gson.toJson(this.projects);
            Files.writeString(this.projectsFilePathInstance, jsonString, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Projects saved to: " + this.projectsFilePathInstance);
        } catch (IOException e) {
            System.err.println("Error saving projects to file (" + this.projectsFilePathInstance + "): " + e.getMessage());
        }
    }

    public void createProject(String projectName) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        Project newProject = new Project(projectName, currentDateTime.format(formatter));

        List<Project> oldProjects = new ArrayList<>(this.projects);
        this.projects.add(newProject);
        support.firePropertyChange(PROJECTS_PROPERTY, oldProjects, new ArrayList<>(this.projects));

        if (this.selectedProject == null && this.projects.size() == 1) {
            selectProject(newProject);
        }
        saveProjects();
    }

    public void selectProject(Project project) {
        Project oldSelectedProject = this.selectedProject;
        this.selectedProject = project;
        support.firePropertyChange(SELECTED_PROJECT_PROPERTY, oldSelectedProject, this.selectedProject);
        if (oldSelectedProject != null && project != null && !oldSelectedProject.getId().equals(project.getId())) {
            support.firePropertyChange(SELECTED_PROJECT_CHAPTERS_PROPERTY, new ArrayList<>(oldSelectedProject.getChapters()), getSelectedProjectChapters());
        } else if (oldSelectedProject == null && project != null) {
             support.firePropertyChange(SELECTED_PROJECT_CHAPTERS_PROPERTY, new ArrayList<>(), getSelectedProjectChapters());
        } else if (oldSelectedProject != null && project == null) {
             support.firePropertyChange(SELECTED_PROJECT_CHAPTERS_PROPERTY, new ArrayList<>(oldSelectedProject.getChapters()), new ArrayList<>());
        }
         // If project is the same, chapters might have changed by other means, but selectProject itself doesn't change them.
    }

    public void addNewProject() {
        createProject("New Project " + (this.projects.size() + 1));
    }

    public void deleteProject(String projectId) {
        Project projectToRemove = this.projects.stream()
                                     .filter(p -> p.getId().equals(projectId))
                                     .findFirst()
                                     .orElse(null);
        if (projectToRemove != null) {
            List<Project> oldProjects = new ArrayList<>(this.projects);
            this.projects.remove(projectToRemove);
            support.firePropertyChange(PROJECTS_PROPERTY, oldProjects, new ArrayList<>(this.projects));

            if (this.selectedProject != null && this.selectedProject.getId().equals(projectId)) {
                selectProject(this.projects.isEmpty() ? null : this.projects.get(0));
            }
            saveProjects();
        }
    }

    // Chapter-related methods
    public Chapter addChapter(Project project, String chapterTitle) {
        if (project == null) return null;

        Project targetProject = this.projects.stream()
                                    .filter(p -> p.getId().equals(project.getId()))
                                    .findFirst()
                                    .orElse(null);
        if (targetProject == null) return null;

        int newOrder = targetProject.getChapters().size();
        Chapter newChapter = new Chapter(chapterTitle, newOrder);

        List<Chapter> oldChapters = new ArrayList<>(targetProject.getChapters());
        targetProject.addChapter(newChapter); // Assumes addChapter modifies the list in place
                                              // and Project.getChapters() returns a mutable list or its copy.
                                              // If Project.chapters is immutable, this needs adjustment.

        // We need to ensure the Project object in the list is updated if it's immutable or if we're working with copies.
        // For now, assuming targetProject.addChapter directly modifies the list held by the Project object in this.projects.

        support.firePropertyChange(SELECTED_PROJECT_CHAPTERS_PROPERTY, oldChapters, new ArrayList<>(targetProject.getChapters()));
        if (this.selectedProject != null && this.selectedProject.getId().equals(targetProject.getId())) {
             support.firePropertyChange(SELECTED_PROJECT_PROPERTY, this.selectedProject, this.selectedProject); // To refresh views relying on project object itself for chapters
        }
        updateDailyWordCountStats(targetProject); // Update stats before saving
        saveProjects();
        return newChapter;
    }

    public Chapter addNewChapterToProject(String projectId, String chapterTitle) {
        Project project = this.projects.stream().filter(p -> p.getId().equals(projectId)).findFirst().orElse(null);
        if (project != null) {
            return addChapter(project, chapterTitle);
        }
        return null;
    }

    public void updateChapterSummary(String projectId, String chapterId, String newSummary) {
        findChapter(projectId, chapterId).ifPresent(chapter -> {
            List<Chapter> oldChapters = getSelectedProjectChapters();
            chapter.setSummary(newSummary);
            support.firePropertyChange(SELECTED_PROJECT_CHAPTERS_PROPERTY, oldChapters, getSelectedProjectChapters());
            if (this.selectedProject != null && this.selectedProject.getId().equals(projectId)) {
                 support.firePropertyChange(SELECTED_PROJECT_PROPERTY, this.selectedProject, this.selectedProject);
            }
            saveProjects();
        });
    }

    public void deleteChapter(String projectId, String chapterId) {
        Project project = findProjectById(projectId);
        if (project != null) {
            Chapter chapterToRemove = project.getChapters().stream()
                                           .filter(c -> c.getId().equals(chapterId))
                                           .findFirst()
                                           .orElse(null);
            if (chapterToRemove != null) {
                List<Chapter> oldChapters = new ArrayList<>(project.getChapters());
                project.removeChapter(chapterToRemove); // Assumes removeChapter is implemented in Project.java

                // Re-order remaining chapters if necessary
                // For simplicity, current Project.removeChapter just removes. If re-ordering is needed:
                // reOrderChapters(project);

                support.firePropertyChange(SELECTED_PROJECT_CHAPTERS_PROPERTY, oldChapters, new ArrayList<>(project.getChapters()));
                if (this.selectedProject != null && this.selectedProject.getId().equals(projectId)) {
                    support.firePropertyChange(SELECTED_PROJECT_PROPERTY, this.selectedProject, this.selectedProject);
                }
                updateDailyWordCountStats(project); // Update stats before saving
                saveProjects();
            }
        }
    }

    public void updateChapterTitle(String projectId, String chapterId, String newTitle) {
        findChapter(projectId, chapterId).ifPresent(chapter -> {
            List<Chapter> oldChapters = getSelectedProjectChapters();
            chapter.setTitle(newTitle);
            support.firePropertyChange(SELECTED_PROJECT_CHAPTERS_PROPERTY, oldChapters, getSelectedProjectChapters());
             if (this.selectedProject != null && this.selectedProject.getId().equals(projectId)) {
                 support.firePropertyChange(SELECTED_PROJECT_PROPERTY, this.selectedProject, this.selectedProject);
            }
            saveProjects();
        });
    }

    public void updateChapterContent(String projectId, String chapterId, String newContent) {
        findChapter(projectId, chapterId).ifPresent(chapter -> {
            List<Chapter> oldChapters = getSelectedProjectChapters();
            chapter.setContent(newContent);
            support.firePropertyChange(SELECTED_PROJECT_CHAPTERS_PROPERTY, oldChapters, getSelectedProjectChapters());
             if (this.selectedProject != null && this.selectedProject.getId().equals(projectId)) {
                 support.firePropertyChange(SELECTED_PROJECT_PROPERTY, this.selectedProject, this.selectedProject);
            }
            Project projectOfChapter = findProjectById(projectId);
            if (projectOfChapter != null) {
                 updateDailyWordCountStats(projectOfChapter); // Update stats before saving
            }
            saveProjects();
            System.out.println("Chapter " + chapterId + " content updated in ViewModel.");
        });
    }

    public void moveChapter(String projectId, String chapterId, boolean moveUp) {
        Project project = findProjectById(projectId);
        if (project == null) return;

        List<Chapter> chapters = project.getChapters(); // Assuming this list is modifiable or we replace it
        int chapterIndex = -1;
        for (int i = 0; i < chapters.size(); i++) {
            if (chapters.get(i).getId().equals(chapterId)) {
                chapterIndex = i;
                break;
            }
        }

        if (chapterIndex == -1) return; // Chapter not found

        int targetIndex = moveUp ? chapterIndex - 1 : chapterIndex + 1;

        if (targetIndex < 0 || targetIndex >= chapters.size()) return; // Cannot move further

        List<Chapter> oldChapters = new ArrayList<>(chapters);

        // Swap order properties
        Chapter chapterToMove = chapters.get(chapterIndex);
        Chapter otherChapter = chapters.get(targetIndex);

        int tempOrder = chapterToMove.getOrder();
        chapterToMove.setOrder(otherChapter.getOrder());
        otherChapter.setOrder(tempOrder);

        // Sort by new order to reflect change, then re-assign dense order
        List<Chapter> sortedChapters = chapters.stream()
                                               .sorted((c1, c2) -> Integer.compare(c1.getOrder(), c2.getOrder()))
                                               .collect(Collectors.toList());

        for (int i = 0; i < sortedChapters.size(); i++) {
            sortedChapters.get(i).setOrder(i);
        }

        project.setChapters(new ArrayList<>(sortedChapters)); // Update project's chapter list

        support.firePropertyChange(SELECTED_PROJECT_CHAPTERS_PROPERTY, oldChapters, new ArrayList<>(project.getChapters()));
        if (this.selectedProject != null && this.selectedProject.getId().equals(projectId)) {
            support.firePropertyChange(SELECTED_PROJECT_PROPERTY, this.selectedProject, this.selectedProject);
        }
        // No need to update daily word count here as title/summary changes don't affect it.
        // However, if we decide they should, then call updateDailyWordCountStats(project) here.
        saveProjects();
    }

    private void updateDailyWordCountStats(Project project) {
        if (project == null) {
            return;
        }

        int totalWordCount = 0;
        for (Chapter chapter : project.getChapters()) {
            totalWordCount += WordCounterUtil.countWords(chapter.getContent());
            // Optionally include words from title and summary:
            // totalWordCount += WordCounterUtil.countWords(chapter.getTitle());
            // totalWordCount += WordCounterUtil.countWords(chapter.getSummary());
        }

        String todayDateString = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE); // YYYY-MM-DD

        Map<String, Integer> dailyCounts = project.getDailyWordCounts();
        // The field is initialized in Project model, but a defensive check here is fine.
        if (dailyCounts == null) {
            dailyCounts = new java.util.HashMap<>();
            project.setDailyWordCounts(dailyCounts);
        }
        dailyCounts.put(todayDateString, totalWordCount);

        System.out.println("Updated daily word count for project '" + project.getName() + "' on " + todayDateString + ": " + totalWordCount + " words.");
    }


    private Optional<Chapter> findChapter(String projectId, String chapterId) {
        Project project = findProjectById(projectId);
        if (project != null) {
            return project.getChapters().stream()
                          .filter(c -> c.getId().equals(chapterId))
                          .findFirst();
        }
        return Optional.empty();
    }

    private Project findProjectById(String projectId) {
        return this.projects.stream()
                            .filter(p -> p.getId().equals(projectId))
                            .findFirst()
                            .orElse(null);
    }

    public void updateProjectGoals(String projectId, int dailyGoal, int totalGoal) {
        Project projectToUpdate = findProjectById(projectId);
        if (projectToUpdate != null) {
            boolean changed = false;
            if (projectToUpdate.getDailyWritingGoal() != dailyGoal) {
                projectToUpdate.setDailyWritingGoal(dailyGoal);
                changed = true;
            }
            if (projectToUpdate.getTotalWritingGoal() != totalGoal) {
                projectToUpdate.setTotalWritingGoal(totalGoal);
                changed = true;
            }

            if (changed) {
                // If the updated project is the currently selected one,
                // we can fire a property change to notify UI components
                // that might need to refresh based on the selected project's properties.
                if (this.selectedProject != null && this.selectedProject.getId().equals(projectId)) {
                    // Firing with the same object but it forces bound components to re-evaluate.
                    // Or, one could create a shallow copy if direct object mutation isn't picked up.
                    // For this specific case, ProjectListView re-reads from selectedProject on selection event,
                    // but this can be useful for other potential listeners.
                    support.firePropertyChange(SELECTED_PROJECT_PROPERTY, this.selectedProject, this.selectedProject);
                }

                System.out.println("Updating goals for project: " + projectToUpdate.getName() +
                                   " - Daily: " + dailyGoal + ", Total: " + totalGoal);
                saveProjects(); // Persist changes
            }
        } else {
            System.err.println("Attempted to update goals for a non-existent project ID: " + projectId);
        }
    }

    // Helper method to get word count for a specific date
    public int getWordCountForDate(Project project, LocalDate date) {
        if (project == null || project.getDailyWordCounts() == null || date == null) {
            return 0;
        }
        String dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        return project.getDailyWordCounts().getOrDefault(dateString, 0);
    }

    // Helper method to calculate the current total word count of a project
    public int calculateTotalWordCount(Project project) {
        if (project == null || project.getChapters() == null) {
            return 0;
        }
        int total = 0;
        for (Chapter chapter : project.getChapters()) {
            if (chapter.getContent() != null) { // Check for null content
                total += WordCounterUtil.countWords(chapter.getContent());
            }
        }
        return total;
    }
}
