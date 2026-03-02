package io.storyflame.desktop;

import io.storyflame.core.archive.ProjectArchiveLayout;
import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public final class StoryFlameDesktopApp {
    private StoryFlameDesktopApp() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StoryFlameDesktopApp::showWindow);
    }

    private static void showWindow() {
        JFrame frame = new JFrame("StoryFlame Desktop");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(720, 480);

        JPanel panel = new JPanel(new BorderLayout());
        String label = "<html><div style='text-align:center;'>"
                + "<h1>StoryFlame</h1>"
                + "<p>Semana 1 inicializada.</p>"
                + "<p>ZIP v" + ProjectArchiveLayout.SPEC_VERSION + " pronto para evolucao.</p>"
                + "</div></html>";
        panel.add(new JLabel(label, SwingConstants.CENTER), BorderLayout.CENTER);

        frame.setContentPane(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

