package br.com.marconardes.storyflame.swing.util;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.util.prefs.Preferences;

public class ThemeManager {

    private static final String PREF_KEY_THEME = "storyflame.theme";
    private static final String LIGHT_THEME = "light";
    private static final String DARK_THEME = "dark";

    private Preferences prefs;

    public ThemeManager() {
        prefs = Preferences.userNodeForPackage(ThemeManager.class);
    }

    public void applyCurrentTheme() {
        String currentTheme = prefs.get(PREF_KEY_THEME, LIGHT_THEME);
        if (DARK_THEME.equals(currentTheme)) {
            applyDarkTheme();
        } else {
            applyLightTheme();
        }
    }

    public void applyLightTheme() {
        try {
            FlatLightLaf.setup();
            // Update Swing components - assuming this will be handled in Main or by property change listener
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void applyDarkTheme() {
        try {
            FlatDarkLaf.setup();
            // Update Swing components - assuming this will be handled in Main or by property change listener
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveThemePreference(String themeName) {
        prefs.put(PREF_KEY_THEME, themeName);
    }

    public String getCurrentThemeName() {
        return prefs.get(PREF_KEY_THEME, LIGHT_THEME);
    }

    public void toggleTheme() {
        if (DARK_THEME.equals(getCurrentThemeName())) {
            applyLightTheme();
            saveThemePreference(LIGHT_THEME);
        } else {
            applyDarkTheme();
            saveThemePreference(DARK_THEME);
        }
        // Trigger UI update, typically SwingUtilities.updateComponentTreeUI(frame) for each top-level window.
        // This will be handled by the property change listener in Main.java
    }
}
