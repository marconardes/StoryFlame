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
import br.com.marconardes.storyflame.swing.util.TxtProjectExporter;
import br.com.marconardes.storyflame.swing.util.PdfProjectExporter; // Add this
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import javax.swing.JFileChooser; // Added
import javax.swing.JOptionPane; // Added
import java.io.File; // Added
import java.nio.file.Paths; // Added
// JMenu, JMenuBar, JMenuItem are already covered by javax.swing.*

public class Main implements ChapterEditorListener, ChapterSelectionListener {

    private ThemeManager themeManager;
    private ProjectViewModel projectViewModel;
    private ProjectListView projectListView;
    private ChapterSectionView chapterSectionView;
    private ChapterEditorView chapterEditorView; // New editor view
    private TxtProjectExporter txtProjectExporter;
    private PdfProjectExporter pdfProjectExporter; // Added

    private JPanel centerPanel; // Panel that will use CardLayout
    private CardLayout cardLayout;
    private JFrame frame; // Made JFrame a field

    private static final String CHAPTER_SECTION_PANEL = "CHAPTER_SECTION_PANEL";
    private static final String CHAPTER_EDITOR_PANEL = "CHAPTER_EDITOR_PANEL";


    public Main() {
        themeManager = new ThemeManager();
        projectViewModel = new ProjectViewModel();
        txtProjectExporter = new TxtProjectExporter();
        pdfProjectExporter = new PdfProjectExporter(); // Added
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

        // --- File Menu (New/Updated) ---
        JMenu fileMenu = new JMenu("File");
        JMenuItem exportToTxtItem = new JMenuItem("Export Project to .txt");
        exportToTxtItem.addActionListener(e -> handleExportToTxt());
        fileMenu.add(exportToTxtItem);

        JMenuItem exportToPdfItem = new JMenuItem("Export Project to .pdf"); // New item
        exportToPdfItem.addActionListener(e -> handleExportToPdf());    // New handler method
        fileMenu.add(exportToPdfItem); // Add to existing fileMenu
        menuBar.add(fileMenu);

        // --- View Menu (existing) ---
        JMenu viewMenu = new JMenu("View");
        JMenuItem toggleThemeItem = new JMenuItem("Toggle Theme");
        toggleThemeItem.addActionListener(e -> {
            themeManager.toggleTheme();
            // The UIManager property change listener should handle the UI update for the frame.
        });
        viewMenu.add(toggleThemeItem);
        menuBar.add(viewMenu); // Add View menu to menubar

        // menuBar.add(fileMenu); // Already added above
        // menuBar.add(viewMenu); // Already added above
        frame.setJMenuBar(menuBar); // Set the menuBar to the frame
    }

    private void handleExportToTxt() {
        Project selectedProject = projectViewModel.getSelectedProject();
        if (selectedProject == null) {
            JOptionPane.showMessageDialog(frame,
                    "Please select a project to export.",
                    "No Project Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Project to .txt");
        // Suggest a filename
        String suggestedFileName = selectedProject.getName().replaceAll("[^a-zA-Z0-9.-]", "_") + ".txt";
        fileChooser.setSelectedFile(new File(suggestedFileName));

        int userSelection = fileChooser.showSaveDialog(frame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                txtProjectExporter.exportProject(selectedProject, fileToSave.toPath());
                JOptionPane.showMessageDialog(frame,
                        "Project exported successfully to " + fileToSave.getAbsolutePath(),
                        "Export Successful",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame,
                        "Error exporting project: " + ex.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace(); // For developer console
            }
        }
    }

    public static void main(String[] args) {
        Main app = new Main();
        app.themeManager.applyCurrentTheme();

        SwingUtilities.invokeLater(app::createAndShowGUI);
    }

    private void handleExportToPdf() {
        Project selectedProject = projectViewModel.getSelectedProject();
        if (selectedProject == null) {
            JOptionPane.showMessageDialog(frame,
                    "Please select a project to export.",
                    "No Project Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Project to .pdf");
        // Suggest a filename
        String suggestedFileName = selectedProject.getName().replaceAll("[^a-zA-Z0-9.-]", "_") + ".pdf";
        fileChooser.setSelectedFile(new File(suggestedFileName));
        // Add a file filter for PDF files
        javax.swing.filechooser.FileNameExtensionFilter filter = new javax.swing.filechooser.FileNameExtensionFilter("PDF Documents (*.pdf)", "pdf");
        fileChooser.setFileFilter(filter);

        int userSelection = fileChooser.showSaveDialog(frame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            // Ensure the file has a .pdf extension if the user didn't type it
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".pdf")) {
                fileToSave = new File(filePath + ".pdf");
            }

            try {
                pdfProjectExporter.exportProject(selectedProject, fileToSave.toPath());
                JOptionPane.showMessageDialog(frame,
                        "Project exported successfully to " + fileToSave.getAbsolutePath(),
                        "Export Successful",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame,
                        "Error exporting project to PDF: " + ex.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace(); // For developer console
            }
        }
    }
}
