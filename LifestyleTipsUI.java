import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class LifestyleTipsUI {
    private static final String[] TIP_TITLES = {
            "Stay Hydrated",
            "Prioritize Quality Sleep",
            "Exercise Regularly",
            "Eat a Balanced Diet",
            "Manage Mental Stress",
            "Reduce Digital Screen Time"
    };

    private static final String[] TIP_DESCRIPTIONS = {
            "Drink at least 2.5 to 3 liters of fresh water daily to improve circulation, kidney function, and energy levels.",
            "Aim for 7 to 8 hours of uninterrupted sleep every night to facilitate cell repair and heart health recovery.",
            "Engage in at least 30 minutes of moderate cardiovascular activity 5 days a week (walking, cycling, swimming).",
            "Focus on whole grains, leafy greens, healthy fats (olive oil, avocados, nuts), and limit processed sodium.",
            "Practice 10-15 minutes of mindfulness meditation, deep diaphragmatic breathing, or yoga to lower cortisol.",
            "Take 20-second breaks every 20 minutes from screens. Avoid blue light 1 hour before bedtime for melatonin regulation."
    };

    private static final String[] IMAGE_PATHS = {
            "water.jpg",
            "sleep.jpg",
            "exercise.jpg",
            "eat.jpg",
            "stress.jpg",
            "screen.jpg"
    };

    private static int currentIndex = 0;
    private static JPanel cardPanel;
    private static JLabel counterLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LifestyleTipsUI::buildAndShowUI);
    }

    public static void buildAndShowUI() {
        JFrame frame = new JFrame("Lifestyle & Wellness Guide");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(950, 750);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // Modern Header
        JPanel headerPanel = UIUtils.createHeader(
                "Daily Lifestyle & Heart Health Habits",
                "Evidence-based daily habits for longevity and vitality",
                () -> {
                    frame.dispose();
                    MainMenuUI.launchMainMenu();
                }
        );
        frame.add(headerPanel, BorderLayout.NORTH);

        // Center Container
        JPanel mainContent = new JPanel(new BorderLayout(20, 20));
        mainContent.setBackground(UIUtils.COLOR_BG);
        mainContent.setBorder(new EmptyBorder(25, 40, 25, 40));

        // Card Container with CardLayout
        cardPanel = new JPanel(new CardLayout());
        cardPanel.setOpaque(false);

        for (int i = 0; i < TIP_TITLES.length; i++) {
            cardPanel.add(createTipCard(TIP_TITLES[i], TIP_DESCRIPTIONS[i], IMAGE_PATHS[i]), String.valueOf(i));
        }

        mainContent.add(cardPanel, BorderLayout.CENTER);

        // Navigation Footer Bar
        JPanel footerBar = new JPanel(new BorderLayout());
        footerBar.setOpaque(false);
        footerBar.setBorder(new EmptyBorder(10, 0, 10, 0));

        JButton prevBtn = UIUtils.createStyledButton(" Previous Habit", UIUtils.COLOR_PRIMARY, Color.WHITE, 180, 42);
        JButton nextBtn = UIUtils.createStyledButton("Next Habit ", UIUtils.COLOR_PRIMARY, Color.WHITE, 180, 42);

        counterLabel = new JLabel("Habit 1 of " + TIP_TITLES.length, JLabel.CENTER);
        counterLabel.setFont(UIUtils.FONT_BOLD);
        counterLabel.setForeground(UIUtils.COLOR_TEXT_MAIN);

        prevBtn.addActionListener(e -> navigateCard(-1));
        nextBtn.addActionListener(e -> navigateCard(1));

        footerBar.add(prevBtn, BorderLayout.WEST);
        footerBar.add(counterLabel, BorderLayout.CENTER);
        footerBar.add(nextBtn, BorderLayout.EAST);

        mainContent.add(footerBar, BorderLayout.SOUTH);

        frame.add(mainContent, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private static JPanel createTipCard(String title, String desc, String imagePath) {
        JPanel card = UIUtils.createCardPanel(Color.WHITE, 16);
        card.setLayout(new BorderLayout(20, 20));
        card.setBorder(new EmptyBorder(25, 30, 25, 30));

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(UIUtils.COLOR_PRIMARY);
        card.add(titleLabel, BorderLayout.NORTH);

        // Image Center
        JLabel imgLabel = new JLabel("", JLabel.CENTER);
        File imgFile = new File(imagePath);
        if (imgFile.exists()) {
            ImageIcon icon = new ImageIcon(imagePath);
            Image scaled = icon.getImage().getScaledInstance(380, 240, Image.SCALE_SMOOTH);
            imgLabel.setIcon(new ImageIcon(scaled));
        } else {
            imgLabel.setText("[Image: " + imagePath + "]");
            imgLabel.setFont(UIUtils.FONT_SUBHEADER);
        }
        card.add(imgLabel, BorderLayout.CENTER);

        // Description Bottom
        JLabel descLabel = new JLabel("<html><center style='width: 500px; font-size: 14px; color: #2C3E50; line-height: 1.5;'>" + desc + "</center></html>", JLabel.CENTER);
        descLabel.setFont(UIUtils.FONT_REGULAR);
        descLabel.setBorder(new EmptyBorder(15, 20, 10, 20));
        card.add(descLabel, BorderLayout.SOUTH);

        return card;
    }

    private static void navigateCard(int direction) {
        currentIndex = (currentIndex + direction + TIP_TITLES.length) % TIP_TITLES.length;
        CardLayout cl = (CardLayout) cardPanel.getLayout();
        cl.show(cardPanel, String.valueOf(currentIndex));
        counterLabel.setText("Habit " + (currentIndex + 1) + " of " + TIP_TITLES.length);
    }
}
