package com.industrialinspection.gui;

import com.industrialinspection.model.User;
import com.industrialinspection.service.DatabaseBackupService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 17. Database Backup & Disaster Recovery Station.
 */
public class DatabaseBackupPanel extends JPanel {

    private final DatabaseBackupService backupService = new DatabaseBackupService();
    private final User currentUser;

    private final JTextField folderField;
    private final JTextArea logArea;
    private final JProgressBar progressBar;

    public DatabaseBackupPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(14, 14));
        setBackground(UITheme.BG_DARKEST);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.BG_DARKEST);

        JLabel title = new JLabel("Database Backup & Archive Generation");
        title.setFont(UITheme.FONT_HEADER_LARGE);
        title.setForeground(UITheme.TEXT_MAIN);

        JLabel sub = new JLabel("Create standalone SQL backup snapshots using ProcessBuilder / JDBC dumpers");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_MUTED);

        topBar.add(title, BorderLayout.NORTH);
        topBar.add(sub, BorderLayout.SOUTH);
        add(topBar, BorderLayout.NORTH);

        // Center card with backup trigger and options
        JPanel center = UITheme.createCardPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel fLbl = new JLabel("Destination Backup Directory:");
        fLbl.setFont(UITheme.FONT_BOLD);
        fLbl.setForeground(UITheme.TEXT_MAIN);
        fLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel folderRow = new JPanel(new BorderLayout(8, 0));
        folderRow.setBackground(UITheme.BG_CARD);
        folderRow.setMaximumSize(new Dimension(800, 34));

        folderField = UITheme.createTextField();
        folderField.setText("backups");
        JButton browseBtn = UITheme.createSecondaryButton("Browse Folder...");
        browseBtn.addActionListener(e -> chooseFolder());

        folderRow.add(folderField, BorderLayout.CENTER);
        folderRow.add(browseBtn, BorderLayout.EAST);

        JButton executeBackupBtn = UITheme.createPrimaryButton("⚡ Create Database Backup Snapshot Now");
        executeBackupBtn.setMaximumSize(new Dimension(800, 40));
        executeBackupBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        executeBackupBtn.addActionListener(e -> executeBackup());
        if (!currentUser.canBackupDatabase()) executeBackupBtn.setEnabled(false);

        progressBar = new JProgressBar();
        progressBar.setMaximumSize(new Dimension(800, 10));
        progressBar.setForeground(UITheme.ACCENT_GREEN);
        progressBar.setBackground(new Color(30, 36, 50));
        progressBar.setBorderPainted(false);
        progressBar.setVisible(false);

        JLabel logLbl = new JLabel("Backup Execution Log:");
        logLbl.setFont(UITheme.FONT_BOLD);
        logLbl.setForeground(UITheme.ACCENT_CYAN);
        logLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(UITheme.FONT_MONO);
        logArea.setBackground(UITheme.BG_INPUT);
        logArea.setForeground(UITheme.TEXT_MAIN);
        logArea.setText("[SYSTEM] Backup subsystem ready. Target schema: industrial_inspection\n");

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setPreferredSize(new Dimension(800, 260));
        logScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        center.add(fLbl);
        center.add(Box.createRigidArea(new Dimension(0, 6)));
        center.add(folderRow);
        center.add(Box.createRigidArea(new Dimension(0, 14)));
        center.add(executeBackupBtn);
        center.add(Box.createRigidArea(new Dimension(0, 10)));
        center.add(progressBar);
        center.add(Box.createRigidArea(new Dimension(0, 14)));
        center.add(logLbl);
        center.add(Box.createRigidArea(new Dimension(0, 6)));
        center.add(logScroll);

        add(center, BorderLayout.CENTER);
    }

    private void chooseFolder() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            folderField.setText(fc.getSelectedFile().getAbsolutePath());
        }
    }

    private void executeBackup() {
        File dir = new File(folderField.getText().trim());
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);

        logArea.append("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] Starting backup archive generation...\n");

        SwingWorker<File, Void> worker = new SwingWorker<>() {
            @Override
            protected File doInBackground() throws Exception {
                return backupService.backupDatabase(dir, currentUser.getUserId());
            }

            @Override
            protected void done() {
                progressBar.setVisible(false);
                try {
                    File result = get();
                    logArea.append("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] SUCCESS: Backup created: " + result.getName() + "\n");
                    logArea.append("Full Path: " + result.getAbsolutePath() + "\n");
                    logArea.append("File Size: " + (result.length() / 1024) + " KB\n\n");
                    JOptionPane.showMessageDialog(DatabaseBackupPanel.this,
                            "Database backup completed successfully!\nFile: " + result.getName(),
                            "Backup Successful", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    logArea.append("[ERROR] Backup failed: " + ex.getMessage() + "\n");
                    JOptionPane.showMessageDialog(DatabaseBackupPanel.this,
                            "Backup error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
