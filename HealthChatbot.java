import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class HealthChatbot {
    private JFrame frame;
    private JTextField inputField;
    private JPanel chatContainer;
    private JScrollPane chatScrollPane;
    private JPanel emergencyBanner;

    private static final String API_KEY = ConfigLoader.get("OPENAI_API_KEY");
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    // Offline Medical Knowledge Base for reliable fallback
    private static final Map<String, String> OFFLINE_KNOWLEDGE = new HashMap<>();
    static {
        OFFLINE_KNOWLEDGE.put("heart", "To maintain heart health: (1) Keep LDL cholesterol < 100 mg/dL, (2) Exercise 150 min/week, (3) Limit sodium < 2g/day, (4) Manage chronic stress and maintain 7-8 hours sleep.");
        OFFLINE_KNOWLEDGE.put("blood pressure", "Normal BP is under 120/80 mm Hg. For elevated BP, practice DASH diet (rich in potassium, magnesium, calcium), reduce salt, exercise regularly, and avoid smoking.");
        OFFLINE_KNOWLEDGE.put("diet", "A balanced heart-healthy diet emphasizes whole grains, leafy green vegetables, fatty fish (rich in Omega-3), extra virgin olive oil, nuts, and berries, while avoiding trans fats and refined sugars.");
        OFFLINE_KNOWLEDGE.put("sleep", "To improve sleep hygiene: Avoid screens 1 hour before bed, keep the bedroom cool (18-20°C), avoid caffeine after 2 PM, and maintain consistent sleep and wake times daily.");
        OFFLINE_KNOWLEDGE.put("stress", "To lower acute stress: Practice 4-7-8 breathing technique (inhale 4s, hold 7s, exhale 8s), take a 15-minute walk outside, stay hydrated, and limit excess caffeine.");
        OFFLINE_KNOWLEDGE.put("water", "General hydration target is 30-35 mL per kg of body weight (approx 2.5 - 3.5 Liters daily). Drink more during workouts or high temperature weather.");
    }

    public HealthChatbot() {
        frame = new JFrame("AI Health & Wellness Assistant");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(950, 780);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = UIUtils.createHeader(
                "AI Health & Medical Assistant",
                "Intelligent 24/7 Clinical Screening, Nutrition & Wellness Guide",
                () -> {
                    frame.dispose();
                    MainMenuUI.launchMainMenu();
                }
        );
        frame.add(headerPanel, BorderLayout.NORTH);

        // Center Container
        JPanel mainContent = new JPanel(new BorderLayout(10, 10));
        mainContent.setBackground(UIUtils.COLOR_BG);
        mainContent.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Emergency Warning Banner (Hidden by default, shown when red flag symptoms detected)
        emergencyBanner = new JPanel(new BorderLayout());
        emergencyBanner.setBackground(UIUtils.COLOR_DANGER);
        emergencyBanner.setBorder(new EmptyBorder(8, 14, 8, 14));
        JLabel emergencyLabel = new JLabel(" EMERGENCY ADVISORY: If you or someone is experiencing severe chest pain, shortness of breath, or sudden weakness, call emergency medical services immediately!");
        emergencyLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        emergencyLabel.setForeground(Color.WHITE);
        emergencyBanner.add(emergencyLabel, BorderLayout.CENTER);
        emergencyBanner.setVisible(false);

        // Quick Suggestion Chips Bar
        JPanel chipsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        chipsPanel.setOpaque(false);

        String[] quickPrompts = {
                "Heart disease prevention",
                "High blood pressure tips",
                "Heart-healthy diet plan",
                "How to improve sleep",
                "Stress reduction techniques",
                "Daily water intake rule"
        };

        for (String prompt : quickPrompts) {
            JButton chip = UIUtils.createStyledButton(prompt, Color.WHITE, UIUtils.COLOR_PRIMARY, 190, 30);
            chip.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            chip.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UIUtils.COLOR_BORDER, 1, true),
                    new EmptyBorder(4, 8, 4, 8)
            ));
            chip.addActionListener(e -> {
                inputField.setText(prompt);
                processUserMessage(prompt);
            });
            chipsPanel.add(chip);
        }

        JPanel topSubPanel = new JPanel(new BorderLayout(0, 5));
        topSubPanel.setOpaque(false);
        topSubPanel.add(emergencyBanner, BorderLayout.NORTH);
        topSubPanel.add(chipsPanel, BorderLayout.SOUTH);
        mainContent.add(topSubPanel, BorderLayout.NORTH);

        // Chat Bubble Scroll Area
        chatContainer = new JPanel();
        chatContainer.setLayout(new BoxLayout(chatContainer, BoxLayout.Y_AXIS));
        chatContainer.setBackground(UIUtils.COLOR_BG);
        chatContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        chatScrollPane = new JScrollPane(chatContainer);
        chatScrollPane.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER));
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        chatScrollPane.setBackground(UIUtils.COLOR_BG);
        mainContent.add(chatScrollPane, BorderLayout.CENTER);

        // Add Initial Bot Greeting
        addBotMessage("Hello! I am your AI Health & Wellness Assistant. You can ask me about cardiovascular health, diet, symptoms, workouts, and lifestyle habits.\n\n*Note: I am designed for educational guidance. For emergency care, contact a medical professional immediately.*");

        // Bottom Input Toolbar
        JPanel bottomBar = new JPanel(new BorderLayout(10, 0));
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(10, 0, 5, 0));

        inputField = UIUtils.createStyledTextField("Type your health question here and press Enter...");
        JButton sendBtn = UIUtils.createStyledButton(" Send", UIUtils.COLOR_PRIMARY, Color.WHITE, 120, 38);

        ActionListener sendAction = e -> {
            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                processUserMessage(text);
            }
        };

        inputField.addActionListener(sendAction);
        sendBtn.addActionListener(sendAction);

        bottomBar.add(inputField, BorderLayout.CENTER);
        bottomBar.add(sendBtn, BorderLayout.EAST);
        mainContent.add(bottomBar, BorderLayout.SOUTH);

        frame.add(mainContent, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private void processUserMessage(String question) {
        inputField.setText("");
        addUserMessage(question);

        // Check for emergency keywords
        String lower = question.toLowerCase();
        if (lower.contains("chest pain") || lower.contains("stroke") || lower.contains("heart attack") ||
            lower.contains("unconscious") || lower.contains("fainting") || lower.contains("difficulty breathing") ||
            lower.contains("severe pain")) {
            emergencyBanner.setVisible(true);
        } else {
            emergencyBanner.setVisible(false);
        }

        // Fetch AI Response
        fetchAIResponse(question);
    }

    private void addUserMessage(String message) {
        JPanel bubble = createChatBubble(message, true);
        chatContainer.add(bubble);
        chatContainer.add(Box.createVerticalStrut(10));
        scrollToBottom();
    }

    private void addBotMessage(String message) {
        JPanel bubble = createChatBubble(message, false);
        chatContainer.add(bubble);
        chatContainer.add(Box.createVerticalStrut(10));
        scrollToBottom();
    }

    private JPanel createChatBubble(String text, boolean isUser) {
        JPanel rowPanel = new JPanel(new FlowLayout(isUser ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        rowPanel.setOpaque(false);
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 500));

        JPanel bubble = new JPanel(new BorderLayout(5, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isUser ? UIUtils.COLOR_PRIMARY : Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                if (!isUser) {
                    g2.setColor(UIUtils.COLOR_BORDER);
                    g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        bubble.setOpaque(false);
        bubble.setBorder(new EmptyBorder(10, 14, 10, 14));

        String timeStr = new SimpleDateFormat("HH:mm").format(new Date());

        JLabel senderLabel = new JLabel(isUser ? "You (" + timeStr + ")" : "AI Assistant (" + timeStr + ")");
        senderLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        senderLabel.setForeground(isUser ? new Color(200, 225, 255) : UIUtils.COLOR_TEXT_MUTED);

        JLabel msgLabel = new JLabel("<html><body style='width: 480px; font-family: Segoe UI; font-size: 13px; color: " +
                (isUser ? "#FFFFFF" : "#212B36") + ";'>" + text.replace("\n", "<br>") + "</body></html>");

        bubble.add(senderLabel, BorderLayout.NORTH);
        bubble.add(msgLabel, BorderLayout.CENTER);

        rowPanel.add(bubble);
        return rowPanel;
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            chatContainer.revalidate();
            chatContainer.repaint();
            JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void fetchAIResponse(String question) {
        // If API key is not configured or offline, use smart offline knowledge base
        if (API_KEY == null || API_KEY.trim().isEmpty() || API_KEY.contains("your_openai_api_key")) {
            String lower = question.toLowerCase();
            String matchedReply = null;
            for (Map.Entry<String, String> entry : OFFLINE_KNOWLEDGE.entrySet()) {
                if (lower.contains(entry.getKey())) {
                    matchedReply = entry.getValue();
                    break;
                }
            }

            if (matchedReply == null) {
                matchedReply = "Regarding '" + question + "': Maintaining balanced nutrition, regular exercise (150 min/week), adequate hydration, and annual health check-ups are key to optimal health. (Configure OPENAI_API_KEY in .env for full live conversational responses).";
            }

            final String reply = matchedReply;
            Timer timer = new Timer(500, evt -> addBotMessage(reply));
            timer.setRepeats(false);
            timer.start();
            return;
        }

        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", "You are an expert AI Health & Medical Assistant. Give clear, evidence-based health, nutrition, cardiovascular, and wellness recommendations. Always remind users to seek clinical diagnosis for emergencies.");

            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", question);

            JSONArray messages = new JSONArray();
            messages.put(systemMsg);
            messages.put(userMsg);

            JSONObject json = new JSONObject();
            json.put("model", "gpt-3.5-turbo");
            json.put("messages", messages);
            json.put("temperature", 0.7);

            RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, java.io.IOException e) {
                    SwingUtilities.invokeLater(() -> addBotMessage("Network connection notice: Could not connect to online server (" + e.getMessage() + "). Providing offline health guidance."));
                }

                @Override
                public void onResponse(Call call, Response response) throws java.io.IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            JSONObject resp = new JSONObject(response.body().string());
                            String reply = resp.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
                            SwingUtilities.invokeLater(() -> addBotMessage(reply.trim()));
                        } catch (Exception ex) {
                            SwingUtilities.invokeLater(() -> addBotMessage("Error parsing AI response: " + ex.getMessage()));
                        }
                    } else {
                        SwingUtilities.invokeLater(() -> addBotMessage("Notice: Online AI service returned status code " + response.code() + ". Please check your API key in .env."));
                    }
                }
            });
        } catch (Exception ex) {
            addBotMessage("Error preparing AI request: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HealthChatbot::new);
    }
}