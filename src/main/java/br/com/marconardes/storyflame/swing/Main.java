package br.com.marconardes.storyflame.swing;

import br.com.marconardes.storyflame.swing.model.Chapter;
import br.com.marconardes.storyflame.swing.model.Project;
import br.com.marconardes.storyflame.swing.view.ChapterEditorListener;
import br.com.marconardes.storyflame.swing.view.ChapterEditorView;
import br.com.marconardes.storyflame.swing.view.ChapterSectionView;
import br.com.marconardes.storyflame.swing.view.ChapterSelectionListener;
import br.com.marconardes.storyflame.swing.view.ProjectListView;
import br.com.marconardes.storyflame.swing.viewmodel.ProjectViewModel;

import br.com.marconardes.storyflame.swing.util.ThemeManager;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;

public class Main implements ChapterEditorListener, ChapterSelectionListener {

    private ThemeManager themeManager;
    private ProjectViewModel projectViewModel;
    private ProjectListView projectListView;
    private ChapterSectionView chapterSectionView;
    private ChapterEditorView chapterEditorView; // New editor view

    private JPanel centerPanel; // Panel that will use CardLayout
    private CardLayout cardLayout;
    private JFrame frame; // Made JFrame a field

    private static final String CHAPTER_SECTION_PANEL = "CHAPTER_SECTION_PANEL";
    private static final String CHAPTER_EDITOR_PANEL = "CHAPTER_EDITOR_PANEL";


    public Main() {
        themeManager = new ThemeManager();
        projectViewModel = new ProjectViewModel();
    }

    private void createAndShowGUI() {
        frame = new JFrame("StoryFlame Swing"); // Use the field
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(800, 600));
        frame.setLocationRelativeTo(null);

        projectListView = new ProjectListView(projectViewModel);

        // Setup ChapterSectionView and set its listener
        chapterSectionView = new ChapterSectionView(projectViewModel);
        chapterSectionView.setChapterSelectionListener(this);


        // Setup ChapterEditorView and set its listener
        chapterEditorView = new ChapterEditorView(projectViewModel);
        chapterEditorView.setEditorListener(this);

        // Setup center panel with CardLayout
        cardLayout = new CardLayout();
        centerPanel = new JPanel(cardLayout);
        centerPanel.add(chapterSectionView, CHAPTER_SECTION_PANEL);
        centerPanel.add(chapterEditorView, CHAPTER_EDITOR_PANEL);

        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        mainPanel.add(projectListView, BorderLayout.WEST);
        mainPanel.add(centerPanel, BorderLayout.CENTER); // Add CardLayout panel to center

        frame.getContentPane().add(mainPanel);
        setupMenuBar(); // Call setupMenuBar
        frame.setVisible(true);

        UIManager.addPropertyChangeListener(evt -> {
            if ("lookAndFeel".equals(evt.getPropertyName())) {
                SwingUtilities.updateComponentTreeUI(frame);
            }
        });

        // Show ChapterSectionView initially
        cardLayout.show(centerPanel, CHAPTER_SECTION_PANEL);
    }

    // Implementation for ChapterSelectionListener (from ChapterSectionView)
    @Override
    public void onEditChapterRequested(Chapter chapter) {
        Project selectedProject = projectViewModel.getSelectedProject();
        if (selectedProject != null && chapter != null) {
            chapterEditorView.editChapter(selectedProject, chapter);
            cardLayout.show(centerPanel, CHAPTER_EDITOR_PANEL);
        }
    }

    // Implementation for ChapterEditorListener (from ChapterEditorView)
    @Override
    public void onEditorClosed(boolean saved) {
        // Regardless of save, switch back to chapter list view
        cardLayout.show(centerPanel, CHAPTER_SECTION_PANEL);
        // If saved, ProjectViewModel would have already notified ChapterSectionView
        // to update itself if necessary.
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        // Add file menu items if any exist or are planned

        JMenu viewMenu = new JMenu("View");
        JMenuItem toggleThemeItem = new JMenuItem("Toggle Theme");
        toggleThemeItem.addActionListener(e -> {
            themeManager.toggleTheme();
            // The UIManager property change listener should handle the UI update for the frame.
        });
        viewMenu.add(toggleThemeItem);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        frame.setJMenuBar(menuBar); // Set the menuBar to the frame
    }

    public static void main(String[] args) {
        Main app = new Main();
        app.themeManager.applyCurrentTheme();

        SwingUtilities.invokeLater(app::createAndShowGUI);
    }
}
