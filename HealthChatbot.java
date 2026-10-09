import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import okhttp3.*;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.IOException;

public class HealthChatbot {
    private JFrame frame;
    private JTextField inputField;
    private JTextArea chatArea;
    private JList<String> suggestionList;
    private DefaultListModel<String> suggestionModel;

    private static final String API_KEY = ConfigLoader.get("OPENAI_API_KEY");
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    private static final String[] predefinedQuestions = {
        "What are the symptoms of high blood pressure?",
        "What should I do for a sudden severe headache?",
        "How can I improve my sleep quality naturally?",
        "What is a heart-healthy diet plan?",
        "How do I relieve acute mental stress and anxiety?",
        "What are early warning signs of heart disease?",
        "How much water should I drink daily?"
    };

    public HealthChatbot() {
        frame = new JFrame("AI Health & Wellness Assistant");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(850, 700);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // Header with Back Button
        JPanel headerPanel = UIUtils.createHeader(
                "AI Health Assistant",
                "Intelligent Medical & Wellness Guidance Bot",
                () -> {
                    frame.dispose();
                    MainMenuUI.launchMainMenu();
                }
        );
        frame.add(headerPanel, BorderLayout.NORTH);

        // Center Content Panel
        JPanel mainContent = new JPanel(new BorderLayout(15, 15));
        mainContent.setBackground(UIUtils.COLOR_BG);
        mainContent.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Suggestions Area (Clickable / Autocomplete)
        suggestionModel = new DefaultListModel<>();
        for (String q : predefinedQuestions) {
            suggestionModel.addElement(q);
        }

        suggestionList = new JList<>(suggestionModel);
        suggestionList.setFont(UIUtils.FONT_REGULAR);
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setBackground(new Color(250, 252, 255));
        suggestionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && suggestionList.getSelectedValue() != null) {
                inputField.setText(suggestionList.getSelectedValue());
            }
        });

        JScrollPane suggestionScroll = new JScrollPane(suggestionList);
        suggestionScroll.setPreferredSize(new Dimension(800, 90));
        suggestionScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UIUtils.COLOR_BORDER),
                " Quick Medical Questions (Click to select)"
        ));
        mainContent.add(suggestionScroll, BorderLayout.NORTH);

        // Chat Conversation Area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatArea.setBackground(Color.WHITE);
        chatArea.setForeground(UIUtils.COLOR_TEXT_MAIN);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setMargin(new Insets(12, 12, 12, 12));

        chatArea.append("Assistant: Hello! I am your AI Health Assistant. Ask me anything about health, nutrition, wellness, or medical symptoms.\n\n");

        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER));
        mainContent.add(chatScroll, BorderLayout.CENTER);

        // Bottom Input Area
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(10, 0, 5, 0));

        inputField = UIUtils.createStyledTextField("Type your health question here and press Enter...");
        JButton sendBtn = UIUtils.createStyledButton(" Send", UIUtils.COLOR_PRIMARY, Color.WHITE, 120, 38);

        ActionListener sendAction = e -> {
            String question = inputField.getText().trim();
            if (!question.isEmpty()) {
                chatArea.append("You: " + question + "\n\n");
                inputField.setText("");
                fetchAIResponse(question);
            }
        };

        inputField.addActionListener(sendAction);
        sendBtn.addActionListener(sendAction);

        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendBtn, BorderLayout.EAST);
        mainContent.add(bottomPanel, BorderLayout.SOUTH);

        frame.add(mainContent, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private void fetchAIResponse(String question) {
        if (API_KEY == null || API_KEY.trim().isEmpty() || API_KEY.contains("your_key_here")) {
            chatArea.append("Assistant: [Notice] OpenAI API Key is not configured in .env. To enable live AI responses, provide a valid OPENAI_API_KEY.\n\n");
            return;
        }

        chatArea.append("Assistant: Thinking...\n");

        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", "You are a helpful and knowledgeable health assistant. Provide concise, clear, and medically informative guidance. Remind users to seek doctor consultation for diagnosis.");

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
                public void onFailure(Call call, IOException e) {
                    SwingUtilities.invokeLater(() -> chatArea.append("Assistant: Failed to connect to AI server: " + e.getMessage() + "\n\n"));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            JSONObject resp = new JSONObject(response.body().string());
                            String reply = resp.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
                            SwingUtilities.invokeLater(() -> {
                                // Remove temporary "Thinking..." line
                                String current = chatArea.getText();
                                if (current.endsWith("Assistant: Thinking...\n")) {
                                    chatArea.setText(current.substring(0, current.length() - 23));
                                }
                                chatArea.append("Assistant: " + reply.trim() + "\n\n");
                            });
                        } catch (Exception ex) {
                            SwingUtilities.invokeLater(() -> chatArea.append("Assistant: Error parsing AI response: " + ex.getMessage() + "\n\n"));
                        }
                    } else {
                        SwingUtilities.invokeLater(() -> chatArea.append("Assistant: Server returned error code " + response.code() + " (" + response.message() + ").\n\n"));
                    }
                }
            });
        } catch (Exception ex) {
            chatArea.append("Assistant: Error preparing request: " + ex.getMessage() + "\n\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HealthChatbot::new);
    }
}
