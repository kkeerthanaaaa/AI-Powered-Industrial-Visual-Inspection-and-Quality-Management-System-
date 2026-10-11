package com.industrialinspection.gui;

import com.industrialinspection.exception.InvalidLoginException;
import com.industrialinspection.model.User;
import com.industrialinspection.service.AuthenticationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * 2. Secure Operator & Engineer Login Screen.
 */
public class LoginFrame extends JFrame {

    private final AuthenticationService authService = new AuthenticationService();
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JLabel errorLabel;

    public LoginFrame() {
        setTitle("ABC Precision - Secure Industrial Login");
        setSize(480, 560);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(UITheme.BG_DARKEST);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Company Logo / Title
        JLabel brandLabel = new JLabel("ABC PRECISION");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        brandLabel.setForeground(UITheme.TEXT_MAIN);
        brandLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subBrand = new JLabel("Visual Inspection & Quality System");
        subBrand.setFont(UITheme.FONT_SMALL);
        subBrand.setForeground(UITheme.ACCENT_CYAN);
        subBrand.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(brandLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        mainPanel.add(subBrand);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        // Login Card
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new GridLayout(6, 1, 6, 8));

        JLabel uLbl = new JLabel("Username / Operator ID:");
        uLbl.setFont(UITheme.FONT_BOLD);
        uLbl.setForeground(UITheme.TEXT_MAIN);
        usernameField = UITheme.createTextField();
        usernameField.setText("admin");

        JLabel pLbl = new JLabel("Password:");
        pLbl.setFont(UITheme.FONT_BOLD);
        pLbl.setForeground(UITheme.TEXT_MAIN);
        passwordField = UITheme.createPasswordField();
        passwordField.setText("Admin@123");

        card.add(uLbl);
        card.add(usernameField);
        card.add(pLbl);
        card.add(passwordField);

        mainPanel.add(card);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        errorLabel = new JLabel(" ");
        errorLabel.setFont(UITheme.FONT_SMALL);
        errorLabel.setForeground(UITheme.ACCENT_RED);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(errorLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Action Buttons
        JButton loginBtn = UITheme.createPrimaryButton("Authenticate & Enter System");
        loginBtn.setMaximumSize(new Dimension(400, 40));
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.addActionListener(e -> executeLogin());

        // Quick Role Switcher Buttons
        JPanel quickSwitch = new JPanel(new GridLayout(1, 4, 6, 6));
        quickSwitch.setBackground(UITheme.BG_DARKEST);
        quickSwitch.setMaximumSize(new Dimension(400, 32));

        JButton btnAdmin = createQuickBtn("Admin", "admin", "Admin@123");
        JButton btnQE = createQuickBtn("Engineer", "engineer", "Pass@123");
        JButton btnSup = createQuickBtn("Supervisor", "supervisor", "Pass@123");
        JButton btnOp = createQuickBtn("Operator", "operator", "Pass@123");

        quickSwitch.add(btnAdmin);
        quickSwitch.add(btnQE);
        quickSwitch.add(btnSup);
        quickSwitch.add(btnOp);

        mainPanel.add(loginBtn);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel hintLbl = new JLabel("Quick Demo Role Switcher:");
        hintLbl.setFont(UITheme.FONT_SMALL);
        hintLbl.setForeground(UITheme.TEXT_MUTED);
        hintLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(hintLbl);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        mainPanel.add(quickSwitch);

        // Enter key shortcut
        KeyAdapter enterKey = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    executeLogin();
                }
            }
        };
        usernameField.addKeyListener(enterKey);
        passwordField.addKeyListener(enterKey);

        setContentPane(mainPanel);
    }

    private JButton createQuickBtn(String text, String user, String pass) {
        JButton btn = UITheme.createSecondaryButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));
        btn.addActionListener(e -> {
            usernameField.setText(user);
            passwordField.setText(pass);
            errorLabel.setText(" ");
        });
        return btn;
    }

    private void executeLogin() {
        String u = usernameField.getText().trim();
        String p = new String(passwordField.getPassword());
        try {
            User user = authService.login(u, p, "127.0.0.1");
            dispose();
            SwingUtilities.invokeLater(() -> {
                MainDashboardFrame frame = new MainDashboardFrame(user);
                frame.setVisible(true);
            });
        } catch (InvalidLoginException ex) {
            errorLabel.setText(ex.getMessage());
        }
    }
}
