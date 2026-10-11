package com.industrialinspection.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Modern High-Contrast Industrial Design System for ABC Precision Manufacturing GUI.
 * Features vibrant colors, high contrast readable typography, and custom rendered components.
 */
public class UITheme {

    // Industrial Dark Color Palette - High Contrast & Distinct Layers
    public static final Color BG_DARKEST    = new Color(13, 17, 26);   // Deep obsidian space
    public static final Color BG_SIDEBAR    = new Color(19, 25, 40);   // Distinct navy slate sidebar
    public static final Color BG_CARD       = new Color(28, 37, 58);   // Elevated readable card surface
    public static final Color BG_CARD_HOVER = new Color(38, 50, 78);   // Hover surface
    public static final Color BG_INPUT      = new Color(20, 28, 45);   // High-contrast input background
    public static final Color BORDER_COLOR  = new Color(52, 70, 105);  // Crisp visible border
    public static final Color BORDER_LIGHT  = new Color(80, 105, 155); // Highlighted border

    // Vibrant High-Visibility Accent Colors
    public static final Color ACCENT_BLUE   = new Color(14, 165, 233); // Sky / Electric Blue
    public static final Color ACCENT_CYAN   = new Color(0, 229, 255);  // Vibrant Neon Cyan
    public static final Color ACCENT_GREEN  = new Color(16, 185, 129); // Vibrant Emerald Green
    public static final Color ACCENT_RED    = new Color(244, 63, 94);  // Vibrant Coral/Rose Red
    public static final Color ACCENT_AMBER  = new Color(251, 146, 60); // Vibrant Amber / Warning Gold
    public static final Color ACCENT_PURPLE = new Color(168, 85, 247); // Violet Intelligence Accent

    // High-Contrast Readable Text Colors
    public static final Color TEXT_MAIN     = new Color(255, 255, 255); // Crisp Bright Pure White
    public static final Color TEXT_PRIMARY  = new Color(241, 245, 249); // Primary Off-White
    public static final Color TEXT_MUTED    = new Color(185, 202, 228); // Bright Silvery Light-Blue (High Contrast)
    public static final Color TEXT_LABEL    = new Color(210, 224, 245); // Form Label Text

    // Typography
    public static final Font FONT_HEADER_LARGE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER_MED   = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_REGULAR      = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD         = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL        = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO         = new Font("Consolas", Font.PLAIN, 12);

    /**
     * Set global UIManager defaults so standard dialogs and menus blend seamlessly.
     */
    public static void setupUIManagerDefaults() {
        try {
            UIManager.put("Panel.background", BG_DARKEST);
            UIManager.put("OptionPane.background", BG_CARD);
            UIManager.put("OptionPane.messageForeground", TEXT_MAIN);
            UIManager.put("Label.foreground", TEXT_MAIN);
            UIManager.put("ScrollPane.background", BG_DARKEST);
            UIManager.put("Viewport.background", BG_DARKEST);
            UIManager.put("Table.background", BG_CARD);
            UIManager.put("Table.foreground", TEXT_MAIN);
            UIManager.put("TableHeader.background", BG_SIDEBAR);
            UIManager.put("TableHeader.foreground", TEXT_MAIN);
        } catch (Exception ignored) {}
    }

    /**
     * Custom Modern Button UI with anti-aliasing, rounded corners, and hover states.
     */
    public static class ModernButtonUI extends BasicButtonUI {
        private final Color normalBg;
        private final Color hoverBg;
        private final Color pressedBg;
        private final Color borderColor;
        private final int cornerRadius;

        public ModernButtonUI(Color normalBg, Color hoverBg, Color pressedBg, Color borderColor, int cornerRadius) {
            this.normalBg = normalBg;
            this.hoverBg = hoverBg;
            this.pressedBg = pressedBg;
            this.borderColor = borderColor;
            this.cornerRadius = cornerRadius;
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            AbstractButton b = (AbstractButton) c;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            ButtonModel model = b.getModel();
            Color bg = normalBg;
            if (!b.isEnabled()) {
                bg = new Color(normalBg.getRed(), normalBg.getGreen(), normalBg.getBlue(), 80);
            } else if (model.isPressed()) {
                bg = pressedBg;
            } else if (model.isRollover()) {
                bg = hoverBg;
            }

            int w = c.getWidth();
            int h = c.getHeight();

            // Background
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w, h, cornerRadius, cornerRadius);

            // Border
            if (borderColor != null && b.isEnabled()) {
                Color bc = model.isRollover() ? borderColor.brighter() : borderColor;
                g2.setColor(bc);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, cornerRadius, cornerRadius);
            }

            g2.dispose();
            super.paint(g, c);
        }
    }

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setUI(new ModernButtonUI(
                ACCENT_BLUE,
                new Color(56, 189, 248),
                new Color(2, 132, 199),
                new Color(56, 189, 248),
                8
        ));
        btn.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(TEXT_MAIN);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setUI(new ModernButtonUI(
                new Color(30, 41, 64),
                new Color(44, 60, 92),
                new Color(22, 32, 50),
                BORDER_COLOR,
                8
        ));
        btn.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createSuccessButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setUI(new ModernButtonUI(
                ACCENT_GREEN,
                new Color(52, 211, 153),
                new Color(5, 150, 105),
                new Color(52, 211, 153),
                8
        ));
        btn.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setUI(new ModernButtonUI(
                ACCENT_RED,
                new Color(251, 113, 133),
                new Color(225, 29, 72),
                new Color(251, 113, 133),
                8
        ));
        btn.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(FONT_REGULAR);
        tf.setForeground(TEXT_MAIN);
        tf.setBackground(BG_INPUT);
        tf.setCaretColor(ACCENT_CYAN);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)
        ));
        return tf;
    }

    public static JPasswordField createPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(FONT_REGULAR);
        pf.setForeground(TEXT_MAIN);
        pf.setBackground(BG_INPUT);
        pf.setCaretColor(ACCENT_CYAN);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)
        ));
        return pf;
    }

    public static <T> JComboBox<T> createComboBox(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setFont(FONT_REGULAR);
        cb.setForeground(TEXT_MAIN);
        cb.setBackground(BG_INPUT);
        cb.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    c.setBackground(ACCENT_BLUE);
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(BG_INPUT);
                    c.setForeground(TEXT_MAIN);
                }
                setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
                return c;
            }
        });
        return cb;
    }

    public static void styleTable(JTable table) {
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_MAIN);
        table.setGridColor(BORDER_COLOR);
        table.setFont(FONT_REGULAR);
        table.setRowHeight(36);
        table.setSelectionBackground(new Color(2, 132, 199));
        table.setSelectionForeground(Color.WHITE);
        table.setShowGrid(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(18, 25, 42));
        header.setForeground(TEXT_MAIN);
        header.setFont(FONT_BOLD);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_BLUE));
        header.setPreferredSize(new Dimension(0, 38));
        header.setOpaque(true);

        // Explicit header renderer to fix Windows L&F painting native white headers with invisible white text
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasFocus, row, col);
                l.setBackground(new Color(18, 25, 42));
                l.setForeground(TEXT_MAIN);
                l.setFont(FONT_BOLD);
                l.setHorizontalAlignment(SwingConstants.LEFT);
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR),
                        BorderFactory.createEmptyBorder(0, 12, 0, 12)
                ));
                l.setOpaque(true);
                return l;
            }
        });

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, isSel, hasFocus, row, col);
                if (isSel) {
                    c.setBackground(new Color(2, 132, 199));
                    c.setForeground(Color.WHITE);
                    c.setFont(FONT_REGULAR);
                } else {
                    c.setBackground(row % 2 == 0 ? BG_CARD : new Color(33, 44, 70));
                    c.setForeground(TEXT_MAIN);

                    // Contextual status pill coloring
                    if (val != null) {
                        String str = val.toString().trim();
                        if ("ACTIVE".equalsIgnoreCase(str) || "APPROVED".equalsIgnoreCase(str) || "PASS".equalsIgnoreCase(str) || "VERIFIED".equalsIgnoreCase(str) || "COMPLETED".equalsIgnoreCase(str)) {
                            c.setForeground(new Color(52, 211, 153)); // Vibrant Emerald Green
                            c.setFont(FONT_BOLD);
                        } else if ("INACTIVE".equalsIgnoreCase(str) || "REJECTED".equalsIgnoreCase(str) || "FAIL".equalsIgnoreCase(str) || "TERMINATED".equalsIgnoreCase(str)) {
                            c.setForeground(new Color(248, 113, 113)); // Vibrant Coral Red
                            c.setFont(FONT_BOLD);
                        } else if ("PENDING".equalsIgnoreCase(str) || "MAINTENANCE".equalsIgnoreCase(str) || "ON_LEAVE".equalsIgnoreCase(str) || "IN_PROGRESS".equalsIgnoreCase(str)) {
                            c.setForeground(new Color(251, 191, 36)); // Vibrant Amber
                            c.setFont(FONT_BOLD);
                        } else {
                            c.setFont(FONT_REGULAR);
                        }
                    } else {
                        c.setFont(FONT_REGULAR);
                    }
                }
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                return c;
            }
        };

        table.setDefaultRenderer(Object.class, cellRenderer);
        table.setDefaultRenderer(Number.class, cellRenderer);
        table.setDefaultRenderer(Integer.class, cellRenderer);
        table.setDefaultRenderer(Double.class, cellRenderer);
        table.setDefaultRenderer(Float.class, cellRenderer);
        table.setDefaultRenderer(Long.class, cellRenderer);
        table.setDefaultRenderer(String.class, cellRenderer);
        table.setDefaultRenderer(Boolean.class, cellRenderer);
    }

    public static JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(16, 18, 16, 18)
        ));
        return card;
    }

    /**
     * Custom High-Contrast TabbedPane UI for dark theme.
     * Guarantees tabs are crystal clear, highly visible, and elegantly styled.
     */
    public static class ModernTabbedPaneUI extends BasicTabbedPaneUI {
        @Override
        protected void installDefaults() {
            super.installDefaults();
            tabInsets = new Insets(10, 20, 10, 20);
            selectedTabPadInsets = new Insets(0, 0, 0, 0);
            contentBorderInsets = new Insets(12, 12, 12, 12);
        }

        @Override
        protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isSelected) {
                // Active tab: glowing electric blue gradient
                GradientPaint gp = new GradientPaint(
                        x, y, new Color(24, 60, 115),
                        x, y + h, new Color(16, 42, 85)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(x + 2, y + 2, w - 4, h - 2, 8, 8);

                // Top / Bottom neon cyan accent bar
                g2.setColor(ACCENT_CYAN);
                g2.fillRect(x + 6, y + h - 3, w - 12, 3);
            } else {
                // Inactive tab: distinct readable slate card
                g2.setColor(new Color(32, 43, 66));
                g2.fillRoundRect(x + 2, y + 2, w - 4, h - 2, 8, 8);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(x + 2, y + 2, w - 4, h - 2, 8, 8);
            }
            g2.dispose();
        }

        @Override
        protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(isSelected ? FONT_BOLD : FONT_REGULAR);
            g2.setColor(isSelected ? Color.WHITE : TEXT_LABEL);
            g2.drawString(title, textRect.x, textRect.y + metrics.getAscent());
            g2.dispose();
        }

        @Override
        protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            if (isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 229, 255, 180));
                g2.drawRoundRect(x + 2, y + 2, w - 4, h - 2, 8, 8);
                g2.dispose();
            }
        }

        @Override
        protected void paintContentBorderTopEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
            g.setColor(BORDER_COLOR);
            g.drawLine(x, y, x + w - 1, y);
        }

        @Override
        protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
            g.setColor(BORDER_COLOR);
            g.drawLine(x, y, x, y + h - 1);
        }

        @Override
        protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
            g.setColor(BORDER_COLOR);
            g.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
        }

        @Override
        protected void paintContentBorderRightEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
            g.setColor(BORDER_COLOR);
            g.drawLine(x + w - 1, y, x + w - 1, y + h - 1);
        }

        @Override
        protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
            // Suppress default focus dotted rectangle for clean look
        }
    }

    /**
     * Applies the modern high-contrast styling to any JTabbedPane.
     */
    public static void styleTabbedPane(JTabbedPane tabbedPane) {
        tabbedPane.setUI(new ModernTabbedPaneUI());
        tabbedPane.setBackground(BG_CARD);
        tabbedPane.setForeground(TEXT_MAIN);
        tabbedPane.setFont(FONT_BOLD);
    }
}
