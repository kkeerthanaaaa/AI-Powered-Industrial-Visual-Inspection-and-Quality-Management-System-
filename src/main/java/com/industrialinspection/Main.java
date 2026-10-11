package com.industrialinspection;

import com.industrialinspection.gui.LoginFrame;
import com.industrialinspection.gui.SplashScreen;
import com.industrialinspection.gui.UITheme;

import javax.swing.*;
import java.io.File;

/**
 * Main Desktop GUI Entry Point for ABC Precision Manufacturing Pvt. Ltd.
 * Launches Splash Screen with asynchronous module loading into Login screen.
 */
public class Main {

    public static void main(String[] args) {
        // Initialize working storage directories
        new File("uploads/inspections").mkdirs();
        new File("reports/export").mkdirs();
        new File("backups").mkdirs();

        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel for native typography and styling
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            UITheme.setupUIManagerDefaults();

            SplashScreen splash = new SplashScreen();
            splash.launch(() -> {
                LoginFrame login = new LoginFrame();
                login.setVisible(true);
            });
        });
    }
}
