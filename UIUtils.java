import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class UIUtils {
    private static boolean darkTheme;
    private static boolean lookAndFeelInitialized;
    private static boolean flatLafAvailable;

    public static void initializeLookAndFeel() {
        if (!lookAndFeelInitialized) {
            applyLookAndFeel(false);
        }
    }

    public static boolean toggleTheme() {
        applyLookAndFeel(!darkTheme);
        return darkTheme;
    }

    public static boolean isDarkTheme() {
        return darkTheme;
    }

    public static boolean isFlatLafAvailable() {
        return flatLafAvailable;
    }

    private static void applyLookAndFeel(boolean dark) {
        String lookAndFeel = dark
                ? "com.formdev.flatlaf.FlatDarkLaf"
                : "com.formdev.flatlaf.FlatLightLaf";
        try {
            UIManager.setLookAndFeel(lookAndFeel);
            darkTheme = dark;
            lookAndFeelInitialized = true;
            flatLafAvailable = true;
            for (Window window : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(window);
                window.repaint();
            }
        } catch (ClassNotFoundException e) {
            flatLafAvailable = false;
            if (!applyNimbusFallback()) {
                System.err.println("FlatLaf and Nimbus are unavailable; using the platform theme.");
            }
        } catch (InstantiationException | IllegalAccessException
                 | UnsupportedLookAndFeelException e) {
            flatLafAvailable = false;
            System.err.println("Unable to apply application theme: " + e.getMessage());
        }
    }

    private static boolean applyNimbusFallback() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    UIManager.put("control", COLOR_BG);
                    UIManager.put("nimbusBase", COLOR_PRIMARY);
                    UIManager.put("nimbusBlueGrey", new Color(215, 230, 229));
                    UIManager.put("nimbusLightBackground", COLOR_CARD_BG);
                    UIManager.put("text", COLOR_TEXT_MAIN);
                    UIManager.put("Panel.background", COLOR_BG);
                    darkTheme = false;
                    lookAndFeelInitialized = true;
                    updateOpenWindows();
                    return true;
                }
            }
            return false;
        } catch (ClassNotFoundException | InstantiationException
                 | IllegalAccessException | UnsupportedLookAndFeelException e) {
            System.err.println("Unable to apply Nimbus fallback theme: " + e.getMessage());
            return false;
        }
    }

    private static void updateOpenWindows() {
        for (Window window : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
            window.repaint();
        }
    }

    // Theme Palette
    public static final Color COLOR_PRIMARY = new Color(15, 91, 101);
    public static final Color COLOR_PRIMARY_DARK = new Color(10, 64, 73);
    public static final Color COLOR_PRIMARY_HOVER = new Color(24, 119, 127);
    public static final Color COLOR_ACCENT = new Color(27, 145, 137);
    public static final Color COLOR_SECONDARY = new Color(53, 133, 154);
    
    public static final Color COLOR_SUCCESS = new Color(35, 132, 101);
    public static final Color COLOR_SUCCESS_LIGHT = new Color(231, 245, 239);
    public static final Color COLOR_DANGER = new Color(190, 67, 75);
    public static final Color COLOR_DANGER_LIGHT = new Color(251, 237, 238);
    public static final Color COLOR_WARNING = new Color(190, 132, 48);
    
    public static final Color COLOR_BG = new Color(241, 247, 247);
    public static final Color COLOR_CARD_BG = new Color(255, 255, 255);
    public static final Color COLOR_CARD_TRANSLUCENT = new Color(255, 255, 255, 238);
    
    public static final Color COLOR_TEXT_MAIN = new Color(31, 55, 62);
    public static final Color COLOR_TEXT_MUTED = new Color(99, 120, 125);
    public static final Color COLOR_BORDER = new Color(216, 229, 229);

    // Common Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    /**
     * Creates a rounded, styled button with hover and click feedback.
     */
    public static JButton createStyledButton(String text, Color bgColor, Color fgColor, int width, int height) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bgColor.brighter());
                } else {
                    g2.setColor(bgColor);
                }
                
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setFont(FONT_BOLD);
        button.setForeground(fgColor);
        button.setPreferredSize(new Dimension(width, height));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    /**
     * Creates a modern styled text field with rounded padding and border.
     */
    public static JTextField createStyledTextField(String placeholder) {
        JTextField textField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !(FocusManager.getCurrentKeyboardFocusManager().getFocusOwner() == this)) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(COLOR_TEXT_MUTED);
                    g2.setFont(FONT_REGULAR);
                    Insets insets = getInsets();
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(placeholder, insets.left + 5, getHeight() / 2 + fm.getAscent() / 2 - 2);
                    g2.dispose();
                }
            }
        };

        textField.setFont(FONT_REGULAR);
        textField.setForeground(COLOR_TEXT_MAIN);
        textField.setBackground(COLOR_CARD_BG);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        textField.setPreferredSize(new Dimension(200, 36));
        return textField;
    }

    /**
     * Creates a modern styled ComboBox.
     */
    public static <T> JComboBox<T> createStyledComboBox(T[] items) {
        JComboBox<T> comboBox = new JComboBox<>(items);
        comboBox.setFont(FONT_REGULAR);
        comboBox.setForeground(COLOR_TEXT_MAIN);
        comboBox.setBackground(COLOR_CARD_BG);
        comboBox.setPreferredSize(new Dimension(200, 36));
        comboBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return comboBox;
    }

    /**
     * Creates a modern styled card panel with rounded corners and optional border.
     */
    public static JPanel createCardPanel(Color bgColor, int cornerRadius) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));
                g2.setColor(new Color(0, 0, 0, 20));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    /**
     * Creates a modern top banner header for sub-screens.
     */
    public static JPanel createHeader(String title, String subtitle, Runnable onBack) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_PRIMARY);
        headerPanel.setBorder(new EmptyBorder(18, 28, 18, 28));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(220, 235, 252));

        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);
        headerPanel.add(textPanel, BorderLayout.WEST);

        if (onBack != null) {
            JButton backBtn = createStyledButton(" Back to Menu", new Color(255, 255, 255, 40), Color.WHITE, 150, 38);
            backBtn.addActionListener(e -> onBack.run());
            headerPanel.add(backBtn, BorderLayout.EAST);
        }

        return headerPanel;
    }
}
