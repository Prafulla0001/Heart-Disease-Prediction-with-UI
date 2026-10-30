import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class MainMenuUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainMenuUI::launchMainMenu);
    }

    public static void launchMainMenu() {
        UIUtils.initializeLookAndFeel();
        JFrame frame = new JFrame("Health Prediction & Wellness System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setMinimumSize(new Dimension(980, 720));
        frame.setSize(1180, 820);
        frame.setLocationRelativeTo(null);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIUtils.COLOR_PRIMARY);
        headerPanel.setBorder(new EmptyBorder(20, 34, 20, 34));

        JPanel titleContainer = new JPanel(new GridLayout(2, 1, 0, 4));
        titleContainer.setOpaque(false);

        JLabel appTitle = new JLabel("Carewell Health");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 27));
        appTitle.setForeground(Color.WHITE);

        JLabel appSubtitle = new JLabel("HEALTH SCREENING  ·  WELLNESS  ·  EDUCATION");
        appSubtitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        appSubtitle.setForeground(new Color(206, 232, 230));

        titleContainer.add(appTitle);
        titleContainer.add(appSubtitle);
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        brandPanel.setOpaque(false);
        brandPanel.add(createMedicalMark());
        brandPanel.add(titleContainer);
        headerPanel.add(brandPanel, BorderLayout.WEST);

        // Quote / Status Badge on the right
        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        badgePanel.setOpaque(false);
        JLabel versionBadge = new JLabel("  PRELIMINARY SCREENING  ");
        versionBadge.setOpaque(true);
        versionBadge.setBackground(new Color(255, 255, 255, 30));
        versionBadge.setForeground(Color.WHITE);
        versionBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        versionBadge.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        badgePanel.add(versionBadge);
        JButton themeButton = UIUtils.createStyledButton("Dark mode", Color.WHITE, UIUtils.COLOR_PRIMARY, 115, 36);
        themeButton.setEnabled(UIUtils.isFlatLafAvailable());
        themeButton.setToolTipText(
                UIUtils.isFlatLafAvailable()
                        ? "Switch between light and dark themes"
                        : "Theme switching is available in the Maven build with FlatLaf."
        );
        themeButton.addActionListener(e -> {
            boolean dark = UIUtils.toggleTheme();
            themeButton.setText(dark ? "Light mode" : "Dark mode");
        });
        badgePanel.add(themeButton);
        headerPanel.add(badgePanel, BorderLayout.EAST);

        frame.add(headerPanel, BorderLayout.NORTH);

        // Main Center Area with background
        JPanel centerPanel = new JPanel(new BorderLayout(0, 8));
        centerPanel.setBackground(UIUtils.COLOR_BG);

        JPanel gridPanel = new JPanel(new GridLayout(2, 3, 18, 18));
        gridPanel.setOpaque(false);
        gridPanel.setBorder(new EmptyBorder(18, 34, 28, 34));

        // Card 1: Heart Disease
        JPanel heartCard = createDashboardCard(
                "Heart Disease Prediction",
                "13-Factor clinical ML risk assessment with customized cardiac advice.",
                " Assess Risk",
                UIUtils.COLOR_DANGER,
                () -> {
                    frame.dispose();
                    HeartDiseasePredictionUI.main(new String[]{});
                }
        );

        // Card 2: BMI Calculator
        JPanel bmiCard = createDashboardCard(
                "BMI & Body Composition",
                "Calculate Body Mass Index, healthy weight range, and category diagnosis.",
                " Calculate BMI",
                UIUtils.COLOR_PRIMARY,
                () -> {
                    frame.dispose();
                    BMICalculator.main(new String[]{});
                }
        );

        // Card 3: Lifestyle Tips
        JPanel tipsCard = createDashboardCard(
                "Lifestyle & Wellness Guide",
                "Interactive visual cards for hydration, sleep, stress, and nutrition habits.",
                " Explore Tips",
                UIUtils.COLOR_SUCCESS,
                () -> {
                    frame.dispose();
                    LifestyleTipsUI.main(new String[]{});
                }
        );

        // Card 4: AI Health Assistant
        JPanel chatbotCard = createDashboardCard(
                "AI Health Assistant",
                "24/7 conversational assistant for symptom screening and wellness answers.",
                " Launch Chatbot",
                UIUtils.COLOR_SECONDARY,
                () -> {
                    frame.dispose();
                    HealthChatbot.main(new String[]{});
                }
        );

        JPanel bloodPressureCard = createDashboardCard(
                "Blood Pressure Check",
                "Classify systolic and diastolic readings using standard adult screening ranges.",
                " Check Reading",
                UIUtils.COLOR_WARNING,
                () -> {
                    frame.dispose();
                    BloodPressureUI.main(new String[]{});
                }
        );

        JPanel historyCard = createDashboardCard(
                "Health History",
                "Search locally saved screening results by patient, date, or assessment type.",
                " View History",
                UIUtils.COLOR_SECONDARY,
                () -> {
                    frame.dispose();
                    HealthHistoryUI.main(new String[]{});
                }
        );

        gridPanel.add(heartCard);
        gridPanel.add(bmiCard);
        gridPanel.add(tipsCard);
        gridPanel.add(chatbotCard);
        gridPanel.add(bloodPressureCard);
        gridPanel.add(historyCard);

        JPanel welcomePanel = new JPanel(new BorderLayout(12, 6));
        welcomePanel.setOpaque(false);
        welcomePanel.setBorder(new EmptyBorder(20, 42, 0, 42));
        JPanel welcomeCopy = new JPanel(new GridLayout(2, 1, 0, 4));
        welcomeCopy.setOpaque(false);
        JLabel welcomeTitle = new JLabel("Your health, at a glance");
        welcomeTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        welcomeTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);
        JLabel welcomeSubtitle = new JLabel(
                "Choose a tool to explore wellness information and preliminary screening."
        );
        welcomeSubtitle.setFont(UIUtils.FONT_REGULAR);
        welcomeSubtitle.setForeground(UIUtils.COLOR_TEXT_MUTED);
        welcomeCopy.add(welcomeTitle);
        welcomeCopy.add(welcomeSubtitle);
        welcomePanel.add(welcomeCopy, BorderLayout.CENTER);

        JLabel careNote = new JLabel("  EDUCATIONAL USE ONLY  ");
        careNote.setOpaque(true);
        careNote.setBackground(new Color(225, 240, 237));
        careNote.setForeground(UIUtils.COLOR_PRIMARY);
        careNote.setFont(new Font("Segoe UI", Font.BOLD, 10));
        careNote.setBorder(new EmptyBorder(9, 8, 9, 8));
        welcomePanel.add(careNote, BorderLayout.EAST);
        centerPanel.add(welcomePanel, BorderLayout.NORTH);
        centerPanel.add(gridPanel, BorderLayout.CENTER);
        frame.add(centerPanel, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(new EmptyBorder(12, 34, 12, 34));

        JLabel footerLabel = new JLabel(
                "For education and preliminary screening only. Not a diagnosis or substitute for professional medical care."
        );
        footerLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        footerLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);
        footerPanel.add(footerLabel, BorderLayout.CENTER);
        JLabel footerMark = new JLabel("CAREWELL  ·  WELLNESS");
        footerMark.setFont(new Font("Segoe UI", Font.BOLD, 10));
        footerMark.setForeground(UIUtils.COLOR_PRIMARY);
        footerPanel.add(footerMark, BorderLayout.EAST);

        frame.add(footerPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private static JPanel createDashboardCard(String title, String desc, String btnText, Color accentColor, Runnable onClick) {
        JPanel card = new JPanel() {
            private boolean hovered = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int arc = 20;
                g2.setColor(new Color(15, 48, 54, hovered ? 22 : 12));
                g2.fill(new RoundRectangle2D.Float(2, 4, getWidth() - 4, getHeight() - 4, arc, arc));
                g2.setColor(UIUtils.COLOR_CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 2, getHeight() - 6, arc, arc));
                g2.setColor(hovered ? accentColor : UIUtils.COLOR_BORDER);
                g2.setStroke(new BasicStroke(hovered ? 2f : 1f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 2, getHeight() - 7, arc, arc));

                g2.dispose();
                super.paintComponent(g);
            }
        };

        card.setLayout(new BorderLayout());
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(15, 18, 12, 18));
        card.setPreferredSize(new Dimension(330, 205));

        JPanel contentPanel = new JPanel(new BorderLayout(12, 10));
        contentPanel.setOpaque(false);
        JPanel heading = new JPanel(new BorderLayout(11, 0));
        heading.setOpaque(false);
        JLabel icon = new JLabel(cardInitials(title), JLabel.CENTER);
        icon.setOpaque(true);
        icon.setBackground(new Color(
                Math.min(255, accentColor.getRed() + 210),
                Math.min(255, accentColor.getGreen() + 210),
                Math.min(255, accentColor.getBlue() + 210)
        ));
        icon.setForeground(accentColor.darker());
        icon.setFont(new Font("Segoe UI", Font.BOLD, 15));
        icon.setPreferredSize(new Dimension(42, 42));
        heading.add(icon, BorderLayout.WEST);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(UIUtils.COLOR_TEXT_MAIN);
        heading.add(titleLabel, BorderLayout.CENTER);
        contentPanel.add(heading, BorderLayout.NORTH);

        JLabel descLabel = new JLabel("<html><body style='width: 260px;'>" + desc + "</body></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);
        contentPanel.add(descLabel, BorderLayout.CENTER);
        card.add(contentPanel, BorderLayout.CENTER);

        JButton actionBtn = UIUtils.createStyledButton(btnText + "  →", accentColor, Color.WHITE, 148, 36);
        actionBtn.addActionListener(e -> onClick.run());

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnWrapper.setOpaque(false);
        btnWrapper.add(actionBtn);
        card.add(btnWrapper, BorderLayout.SOUTH);

        return card;
    }

    private static JLabel createMedicalMark() {
        JLabel mark = new JLabel("+", JLabel.CENTER);
        mark.setOpaque(true);
        mark.setBackground(new Color(236, 248, 246));
        mark.setForeground(UIUtils.COLOR_PRIMARY);
        mark.setFont(new Font("Segoe UI", Font.BOLD, 32));
        mark.setPreferredSize(new Dimension(52, 52));
        mark.setBorder(BorderFactory.createLineBorder(new Color(188, 224, 218), 2, true));
        return mark;
    }

    private static String cardInitials(String title) {
        String[] words = title.split("\\s+");
        if (words.length == 1) {
            return words[0].substring(0, 1);
        }
        return (words[0].substring(0, 1) + words[1].substring(0, 1)).toUpperCase();
    }
}
