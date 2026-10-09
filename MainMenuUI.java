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
        JFrame frame = new JFrame("Health Prediction & Wellness System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setSize(1100, 750);
        frame.setLocationRelativeTo(null);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIUtils.COLOR_PRIMARY);
        headerPanel.setBorder(new EmptyBorder(25, 40, 25, 40));

        JPanel titleContainer = new JPanel(new GridLayout(2, 1, 0, 5));
        titleContainer.setOpaque(false);

        JLabel appTitle = new JLabel("Health Prediction & Wellness Platform");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        appTitle.setForeground(Color.WHITE);

        JLabel appSubtitle = new JLabel("Intelligent Disease Screening, Body Composition & Medical Guidance");
        appSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        appSubtitle.setForeground(new Color(200, 225, 250));

        titleContainer.add(appTitle);
        titleContainer.add(appSubtitle);
        headerPanel.add(titleContainer, BorderLayout.WEST);

        // Quote / Status Badge on the right
        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        badgePanel.setOpaque(false);
        JLabel versionBadge = new JLabel(" AI Powered v2.0 ");
        versionBadge.setOpaque(true);
        versionBadge.setBackground(new Color(255, 255, 255, 35));
        versionBadge.setForeground(Color.WHITE);
        versionBadge.setFont(new Font("Segoe UI", Font.BOLD, 13));
        versionBadge.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        badgePanel.add(versionBadge);
        headerPanel.add(badgePanel, BorderLayout.EAST);

        frame.add(headerPanel, BorderLayout.NORTH);

        // Main Center Area with background
        JPanel centerPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon bgIcon = new ImageIcon("background.jpg");
                if (bgIcon.getIconWidth() > 0) {
                    g.drawImage(bgIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
                    g.setColor(new Color(245, 247, 250, 210)); // Soft overlay for readability
                    g.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    g.setColor(UIUtils.COLOR_BG);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };

        // Grid of 4 Feature Cards
        JPanel gridPanel = new JPanel(new GridLayout(2, 2, 30, 30));
        gridPanel.setOpaque(false);
        gridPanel.setBorder(new EmptyBorder(40, 50, 40, 50));

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

        gridPanel.add(heartCard);
        gridPanel.add(bmiCard);
        gridPanel.add(tipsCard);
        gridPanel.add(chatbotCard);

        centerPanel.add(gridPanel);
        frame.add(centerPanel, BorderLayout.CENTER);

        // Footer with Disclaimer
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel footerLabel = new JLabel("Disclaimer: This software is intended for educational and preliminary screening purposes. Always consult a medical professional for clinical diagnosis.");
        footerLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        footerLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);
        footerPanel.add(footerLabel);

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

                int arc = 16;
                // Card Background
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));

                // Left Accent Stripe
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, 8, getHeight(), arc, arc);
                g2.fillRect(4, 0, 4, getHeight());

                // Subtle Border
                g2.setColor(hovered ? accentColor : UIUtils.COLOR_BORDER);
                g2.setStroke(new BasicStroke(hovered ? 2f : 1f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, arc, arc));

                g2.dispose();
                super.paintComponent(g);
            }
        };

        card.setLayout(new BorderLayout());
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(22, 26, 22, 26));
        card.setPreferredSize(new Dimension(420, 180));

        JPanel contentPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        contentPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(UIUtils.COLOR_TEXT_MAIN);

        JLabel descLabel = new JLabel("<html><body style='width: 280px;'>" + desc + "</body></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);

        contentPanel.add(titleLabel);
        contentPanel.add(descLabel);
        card.add(contentPanel, BorderLayout.CENTER);

        JButton actionBtn = UIUtils.createStyledButton(btnText, accentColor, Color.WHITE, 160, 40);
        actionBtn.addActionListener(e -> onClick.run());

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnWrapper.setOpaque(false);
        btnWrapper.add(actionBtn);
        card.add(btnWrapper, BorderLayout.SOUTH);

        return card;
    }
}
