import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HealthChatbot {
    private JFrame frame;
    private JTextField inputField;
    private JPanel chatContainer;
    private JScrollPane chatScrollPane;
    private JPanel emergencyBanner;

    private static final String SYSTEM_PROMPT = "You are an educational health assistant, not a clinician. Provide cautious, evidence-informed general information; do not diagnose, prescribe, or claim certainty. For possible heart attack or stroke symptoms, severe chest pain, breathing difficulty, fainting, or sudden weakness, tell the user to contact local emergency services immediately and not wait for an online response. Encourage professional medical evaluation for concerning symptoms and remind users that this service cannot replace clinical care.";
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .readTimeout(java.time.Duration.ofSeconds(35))
            .callTimeout(java.time.Duration.ofSeconds(45))
            .build();

    private enum ProviderType {
        OPENAI_COMPATIBLE,
        GEMINI
    }

    private static final class ChatProvider {
        private final String name;
        private final String key;
        private final String model;
        private final String endpoint;
        private final ProviderType type;

        private ChatProvider(
                String name, String key, String model, String endpoint, ProviderType type
        ) {
            this.name = name;
            this.key = key;
            this.model = model;
            this.endpoint = endpoint;
            this.type = type;
        }
    }

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
        List<ChatProvider> providers = configuredProviders();
        if (providers.isEmpty()) {
            String reply = getOfflineReply(question)
                    + "\n\nLive AI is not configured. Add a GROK_API_KEY and/or "
                    + "GEMINI_API_KEY to .env. Tavily provides web search, not chat.";
            Timer timer = new Timer(500, evt -> addBotMessage(reply));
            timer.setRepeats(false);
            timer.start();
            return;
        }

        tryProvider(question, providers, 0, new ArrayList<>());
    }

    private static List<ChatProvider> configuredProviders() {
        List<ChatProvider> providers = new ArrayList<>();
        addProviderIfConfigured(
                providers, "Grok",
                ConfigLoader.get("GROK_API_KEY"),
                ConfigLoader.get("GROK_MODEL", "grok-3-mini"),
                "https://api.x.ai/v1/chat/completions",
                ProviderType.OPENAI_COMPATIBLE
        );
        addProviderIfConfigured(
                providers, "Gemini",
                ConfigLoader.get("GEMINI_API_KEY"),
                ConfigLoader.get("GEMINI_MODEL", "gemini-2.5-flash"),
                "https://generativelanguage.googleapis.com/v1beta/models/",
                ProviderType.GEMINI
        );

        String omniRouteKey = ConfigLoader.get("OMNIROUTE_API_KEY");
        String omniRouteBase = ConfigLoader.get("OMNIROUTE_BASE_URL");
        String omniRouteModel = ConfigLoader.get("OMNIROUTE_MODEL");
        if (isConfiguredSecret(omniRouteKey)
                && omniRouteBase != null && !omniRouteBase.isBlank()
                && omniRouteModel != null && !omniRouteModel.isBlank()) {
            providers.add(new ChatProvider(
                    "OmniRoute", omniRouteKey, omniRouteModel, omniRouteBase,
                    ProviderType.OPENAI_COMPATIBLE
            ));
        }
        return providers;
    }

    private static void addProviderIfConfigured(
            List<ChatProvider> providers,
            String name,
            String key,
            String model,
            String endpoint,
            ProviderType type
    ) {
        if (isConfiguredSecret(key) && model != null && !model.isBlank()) {
            providers.add(new ChatProvider(name, key, model, endpoint, type));
        }
    }

    private static boolean isConfiguredSecret(String value) {
        return value != null && !value.isBlank()
                && !value.toLowerCase().contains("your_")
                && !value.toLowerCase().contains("replace_me");
    }

    private void tryProvider(
            String question,
            List<ChatProvider> providers,
            int providerIndex,
            List<String> failures
    ) {
        if (providerIndex >= providers.size()) {
            String message = "All configured AI providers failed:\n"
                    + String.join("\n", failures)
                    + "\n\nCheck the provider keys and model names in .env. "
                    + "A provider quota or configuration error will automatically "
                    + "fall through to the next configured provider.";
            SwingUtilities.invokeLater(() -> addBotMessage(message));
            return;
        }

        ChatProvider provider = providers.get(providerIndex);
        final Request request;
        try {
            request = createRequest(provider, question);
        } catch (RuntimeException exception) {
            failures.add(provider.name + ": invalid endpoint or request configuration.");
            tryProvider(question, providers, providerIndex + 1, failures);
            return;
        }

        HTTP_CLIENT.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, java.io.IOException exception) {
                failures.add(provider.name + ": network request failed.");
                tryProvider(question, providers, providerIndex + 1, failures);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String answer = parseAnswer(provider, response.body().string());
                            if (!answer.isBlank()) {
                                SwingUtilities.invokeLater(() -> addBotMessage(answer));
                                return;
                            }
                            failures.add(provider.name + ": returned an empty response.");
                        } catch (RuntimeException exception) {
                            failures.add(provider.name + ": returned an invalid response.");
                        }
                    } else {
                        String detail = response.body() == null
                                ? ""
                                : getProviderError(response.body().string(), provider.key);
                        failures.add(provider.name + " HTTP " + response.code()
                                + (detail.isBlank() ? "." : ": " + detail));
                    }
                } catch (java.io.IOException exception) {
                    failures.add(provider.name + ": could not read its response.");
                }
                tryProvider(question, providers, providerIndex + 1, failures);
            }
        });
    }

    private static Request createRequest(ChatProvider provider, String question) {
        if (provider.type == ProviderType.GEMINI) {
            JSONObject body = new JSONObject()
                    .put("systemInstruction", new JSONObject()
                            .put("parts", new JSONArray().put(new JSONObject()
                                    .put("text", SYSTEM_PROMPT))))
                    .put("contents", new JSONArray().put(new JSONObject()
                            .put("role", "user")
                            .put("parts", new JSONArray().put(new JSONObject()
                                    .put("text", question)))))
                    .put("generationConfig", new JSONObject().put("temperature", 0.7));
            HttpUrl endpoint = HttpUrl.get(
                    provider.endpoint + provider.model + ":generateContent"
            );
            return new Request.Builder()
                    .url(endpoint)
                    .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                    .addHeader("x-goog-api-key", provider.key)
                    .build();
        }

        String endpoint = "OmniRoute".equals(provider.name)
                ? getChatCompletionsUrl(provider.endpoint)
                : provider.endpoint;
        JSONArray messages = new JSONArray()
                .put(new JSONObject().put("role", "system").put("content", SYSTEM_PROMPT))
                .put(new JSONObject().put("role", "user").put("content", question));
        JSONObject body = new JSONObject()
                .put("model", provider.model)
                .put("messages", messages)
                .put("temperature", 0.7);
        return new Request.Builder()
                .url(endpoint)
                .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + provider.key)
                .build();
    }

    private static String parseAnswer(ChatProvider provider, String responseBody) {
        JSONObject response = new JSONObject(responseBody);
        if (provider.type == ProviderType.GEMINI) {
            JSONArray parts = response.getJSONArray("candidates")
                    .getJSONObject(0).getJSONObject("content").getJSONArray("parts");
            StringBuilder answer = new StringBuilder();
            for (int i = 0; i < parts.length(); i++) {
                answer.append(parts.getJSONObject(i).optString("text", ""));
            }
            return answer.toString().trim();
        }
        Object content = response.getJSONArray("choices")
                .getJSONObject(0).getJSONObject("message").get("content");
        return content instanceof String text ? text.trim() : "";
    }

    private static String getProviderError(String body, String secret) {
        try {
            JSONObject response = new JSONObject(body);
            Object error = response.opt("error");
            String message = error instanceof JSONObject errorObject
                    ? errorObject.optString("message", "")
                    : response.optString("message", "");
            if (!message.isBlank()) {
                String safeMessage = message.replace(secret, "[redacted]")
                        .replaceAll("(?i)Bearer\\s+\\S+", "Bearer [redacted]")
                        .replaceAll("(?i)(key|token|password)\\s*[:=]\\s*\\S+", "$1=[redacted]");
                return safeMessage.length() > 240
                        ? safeMessage.substring(0, 237) + "..."
                        : safeMessage;
            }
        } catch (JSONException exception) {
            return "";
        }
        return "";
    }

    private static String getOfflineReply(String question) {
        String lower = question.toLowerCase();
        for (Map.Entry<String, String> entry : OFFLINE_KNOWLEDGE.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "I can offer general wellness guidance, but a live AI provider is not "
                + "configured, so I cannot answer that specific question.";
    }

    private static String getChatCompletionsUrl(String configuredBaseUrl) {
        URI baseUri = URI.create(configuredBaseUrl.trim());
        String scheme = baseUri.getScheme();
        String host = baseUri.getHost();
        boolean secure = "https".equalsIgnoreCase(scheme);
        boolean localHttp = "http".equalsIgnoreCase(scheme) && isLoopbackHost(host);
        if ((!secure && !localHttp)
                || host == null
                || baseUri.getUserInfo() != null
                || baseUri.getQuery() != null
                || baseUri.getFragment() != null) {
            throw new IllegalArgumentException(
                    "OMNIROUTE_BASE_URL must use HTTPS, or HTTP on localhost only, without embedded credentials, query, or fragment."
            );
        }
        String baseUrl = configuredBaseUrl.trim().replaceAll("/+$", "");
        return baseUrl.endsWith("/chat/completions")
                ? baseUrl
                : baseUrl + "/chat/completions";
    }

    private static boolean isLoopbackHost(String host) {
        return "localhost".equalsIgnoreCase(host)
                || "127.0.0.1".equals(host)
                || "::1".equals(host)
                || "[::1]".equals(host);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HealthChatbot::new);
    }
}