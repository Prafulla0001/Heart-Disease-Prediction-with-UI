import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class HeartDiseasePredictionUI {
    // Form Inputs
    private static JTextField ageField;
    private static JComboBox<String> sexCombo;
    private static JComboBox<String> cpCombo;
    private static JTextField bpField;
    private static JTextField cholField;
    private static JComboBox<String> fbsCombo;
    private static JComboBox<String> ecgCombo;
    private static JTextField maxHrField;
    private static JComboBox<String> exangCombo;
    private static JTextField oldpeakField;
    private static JComboBox<String> slopeCombo;
    private static JComboBox<String> caCombo;
    private static JComboBox<String> thalCombo;

    // Result Components
    private static JLabel resultStatusLabel;
    private static JLabel resultImageLabel;
    private static JTextArea suggestionsArea;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HeartDiseasePredictionUI::buildAndShowUI);
    }

    public static void buildAndShowUI() {
        JFrame frame = new JFrame("Heart Disease Prediction System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 850);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // 1. Modern Header
        JPanel headerPanel = UIUtils.createHeader(
                "Heart Disease Risk Assessment",
                "13-Factor Machine Learning Clinical Diagnostic Screening",
                () -> {
                    frame.dispose();
                    MainMenuUI.launchMainMenu();
                }
        );
        frame.add(headerPanel, BorderLayout.NORTH);

        // 2. Main Content
        JPanel mainContent = new JPanel(new BorderLayout(20, 20));
        mainContent.setBackground(UIUtils.COLOR_BG);
        mainContent.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Form Container Card
        JPanel formCard = UIUtils.createCardPanel(Color.WHITE, 16);
        formCard.setLayout(new BorderLayout(15, 15));
        formCard.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Presets Toolbar at top of Form
        JPanel presetBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        presetBar.setOpaque(false);
        JLabel presetLabel = new JLabel("Quick Presets:");
        presetLabel.setFont(UIUtils.FONT_BOLD);
        presetLabel.setForeground(UIUtils.COLOR_TEXT_MAIN);
        presetBar.add(presetLabel);

        JButton loadHealthyBtn = UIUtils.createStyledButton(" Healthy Sample", UIUtils.COLOR_SUCCESS, Color.WHITE, 160, 32);
        loadHealthyBtn.addActionListener(e -> fillHealthySample());
        presetBar.add(loadHealthyBtn);

        JButton loadHighRiskBtn = UIUtils.createStyledButton(" High-Risk Sample", UIUtils.COLOR_DANGER, Color.WHITE, 160, 32);
        loadHighRiskBtn.addActionListener(e -> fillHighRiskSample());
        presetBar.add(loadHighRiskBtn);

        JButton resetBtn = UIUtils.createStyledButton(" Reset Form", UIUtils.COLOR_TEXT_MUTED, Color.WHITE, 120, 32);
        resetBtn.addActionListener(e -> resetForm());
        presetBar.add(resetBtn);

        formCard.add(presetBar, BorderLayout.NORTH);

        // Form Grid
        JPanel formGrid = new JPanel(new GridLayout(7, 2, 20, 14));
        formGrid.setOpaque(false);
        formGrid.setBorder(new EmptyBorder(10, 0, 10, 0));

        // Initialize Form Controls
        ageField = UIUtils.createStyledTextField("e.g. 52");
        sexCombo = UIUtils.createStyledComboBox(new String[]{"Male (1)", "Female (0)"});
        cpCombo = UIUtils.createStyledComboBox(new String[]{
                "0: Typical Angina",
                "1: Atypical Angina",
                "2: Non-Anginal Pain",
                "3: Asymptomatic"
        });
        bpField = UIUtils.createStyledTextField("e.g. 125 (80-200)");
        cholField = UIUtils.createStyledTextField("e.g. 212 (100-600)");
        fbsCombo = UIUtils.createStyledComboBox(new String[]{
                "<= 120 mg/dL Normal (0)",
                "> 120 mg/dL Elevated (1)"
        });
        ecgCombo = UIUtils.createStyledComboBox(new String[]{
                "0: Normal",
                "1: ST-T Wave Abnormality",
                "2: Left Ventricular Hypertrophy"
        });
        maxHrField = UIUtils.createStyledTextField("e.g. 168 (60-220)");
        exangCombo = UIUtils.createStyledComboBox(new String[]{
                "No Exercise Angina (0)",
                "Yes, Induced by Exercise (1)"
        });
        oldpeakField = UIUtils.createStyledTextField("e.g. 1.0 (0.0 - 6.0)");
        slopeCombo = UIUtils.createStyledComboBox(new String[]{
                "0: Upsloping",
                "1: Flat",
                "2: Downsloping"
        });
        caCombo = UIUtils.createStyledComboBox(new String[]{
                "0 Major Vessels",
                "1 Major Vessel",
                "2 Major Vessels",
                "3 Major Vessels",
                "4 Major Vessels"
        });
        thalCombo = UIUtils.createStyledComboBox(new String[]{
                "1: Normal Result",
                "2: Fixed Defect",
                "3: Reversible Defect"
        });

        // Add to form grid
        formGrid.add(createFieldWrapper("1. Age (Years, 1-120):", ageField));
        formGrid.add(createFieldWrapper("2. Biological Sex:", sexCombo));
        formGrid.add(createFieldWrapper("3. Chest Pain Type (CP):", cpCombo));
        formGrid.add(createFieldWrapper("4. Resting BP (mm Hg, 80-200):", bpField));
        formGrid.add(createFieldWrapper("5. Serum Cholesterol (mg/dL, 100-600):", cholField));
        formGrid.add(createFieldWrapper("6. Fasting Blood Sugar (FBS):", fbsCombo));
        formGrid.add(createFieldWrapper("7. Resting ECG:", ecgCombo));
        formGrid.add(createFieldWrapper("8. Max Heart Rate (Thalach, 60-220):", maxHrField));
        formGrid.add(createFieldWrapper("9. Exercise Induced Angina (Exang):", exangCombo));
        formGrid.add(createFieldWrapper("10. ST Depression (Oldpeak, 0.0-6.0):", oldpeakField));
        formGrid.add(createFieldWrapper("11. Peak Exercise ST Slope:", slopeCombo));
        formGrid.add(createFieldWrapper("12. Major Vessels Colored (CA 0-4):", caCombo));
        formGrid.add(createFieldWrapper("13. Thallium Stress Test (Thal):", thalCombo));

        // Action Panel
        JPanel predictActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        predictActionPanel.setOpaque(false);
        JButton predictBtn = UIUtils.createStyledButton(" Run ML Cardiac Prediction", UIUtils.COLOR_PRIMARY, Color.WHITE, 260, 42);
        predictBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        predictBtn.addActionListener(e -> performPrediction());
        predictActionPanel.add(predictBtn);
        formGrid.add(predictActionPanel);

        formCard.add(formGrid, BorderLayout.CENTER);

        // 3. Right: Result Display Card
        JPanel resultCard = UIUtils.createCardPanel(Color.WHITE, 16);
        resultCard.setLayout(new BorderLayout(15, 15));
        resultCard.setBorder(new EmptyBorder(20, 20, 20, 20));
        resultCard.setPreferredSize(new Dimension(430, 0));

        JLabel resultTitle = new JLabel("Diagnostic Output & Advice");
        resultTitle.setFont(UIUtils.FONT_HEADER);
        resultTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);
        resultCard.add(resultTitle, BorderLayout.NORTH);

        JPanel resultBody = new JPanel(new BorderLayout(10, 10));
        resultBody.setOpaque(false);

        resultStatusLabel = new JLabel("Awaiting patient input...", JLabel.CENTER);
        resultStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        resultStatusLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);
        resultStatusLabel.setOpaque(true);
        resultStatusLabel.setBackground(UIUtils.COLOR_BG);
        resultStatusLabel.setBorder(new EmptyBorder(12, 16, 12, 16));
        resultBody.add(resultStatusLabel, BorderLayout.NORTH);

        resultImageLabel = new JLabel("", JLabel.CENTER);
        resultImageLabel.setPreferredSize(new Dimension(180, 160));
        resultBody.add(resultImageLabel, BorderLayout.CENTER);

        suggestionsArea = new JTextArea();
        suggestionsArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        suggestionsArea.setLineWrap(true);
        suggestionsArea.setWrapStyleWord(true);
        suggestionsArea.setEditable(false);
        suggestionsArea.setBackground(new Color(250, 252, 255));
        suggestionsArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        suggestionsArea.setText("Select a quick preset above or fill in the clinical parameters, then click 'Run ML Cardiac Prediction'.");

        JScrollPane scrollPane = new JScrollPane(suggestionsArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UIUtils.COLOR_BORDER),
                "Clinical Recommendations & Action Plan"
        ));
        scrollPane.setPreferredSize(new Dimension(390, 240));
        resultBody.add(scrollPane, BorderLayout.SOUTH);

        resultCard.add(resultBody, BorderLayout.CENTER);

        // Assemble Layout
        mainContent.add(formCard, BorderLayout.CENTER);
        mainContent.add(resultCard, BorderLayout.EAST);

        frame.add(mainContent, BorderLayout.CENTER);
        frame.setVisible(true);

        fillHealthySample();
    }

    private static JPanel createFieldWrapper(String labelText, JComponent component) {
        JPanel wrapper = new JPanel(new BorderLayout(5, 4));
        wrapper.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(UIUtils.COLOR_TEXT_MAIN);

        wrapper.add(label, BorderLayout.NORTH);
        wrapper.add(component, BorderLayout.CENTER);
        return wrapper;
    }

    private static void fillHealthySample() {
        ageField.setText("52");
        sexCombo.setSelectedIndex(0);     // Male (1)
        cpCombo.setSelectedIndex(0);      // Typical Angina (0)
        bpField.setText("125");
        cholField.setText("212");
        fbsCombo.setSelectedIndex(0);     // <= 120 (0)
        ecgCombo.setSelectedIndex(1);     // ST-T (1)
        maxHrField.setText("168");
        exangCombo.setSelectedIndex(0);   // No (0)
        oldpeakField.setText("1.0");
        slopeCombo.setSelectedIndex(2);   // Downsloping (2)
        caCombo.setSelectedIndex(2);      // 2 vessels
        thalCombo.setSelectedIndex(2);    // Reversible (3)
    }

    private static void fillHighRiskSample() {
        ageField.setText("58");
        sexCombo.setSelectedIndex(1);     // Female (0)
        cpCombo.setSelectedIndex(0);      // Typical Angina (0)
        bpField.setText("100");
        cholField.setText("248");
        fbsCombo.setSelectedIndex(0);     // <= 120 (0)
        ecgCombo.setSelectedIndex(0);     // Normal (0)
        maxHrField.setText("122");
        exangCombo.setSelectedIndex(0);   // No (0)
        oldpeakField.setText("1.0");
        slopeCombo.setSelectedIndex(1);   // Flat (1)
        caCombo.setSelectedIndex(0);      // 0 vessels
        thalCombo.setSelectedIndex(1);    // Fixed defect (2)
    }

    private static void resetForm() {
        ageField.setText("");
        sexCombo.setSelectedIndex(0);
        cpCombo.setSelectedIndex(0);
        bpField.setText("");
        cholField.setText("");
        fbsCombo.setSelectedIndex(0);
        ecgCombo.setSelectedIndex(0);
        maxHrField.setText("");
        exangCombo.setSelectedIndex(0);
        oldpeakField.setText("");
        slopeCombo.setSelectedIndex(0);
        caCombo.setSelectedIndex(0);
        thalCombo.setSelectedIndex(0);

        resultStatusLabel.setText("Awaiting patient input...");
        resultStatusLabel.setBackground(UIUtils.COLOR_BG);
        resultStatusLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);
        resultImageLabel.setIcon(null);
        suggestionsArea.setText("Please enter patient data and click 'Run ML Cardiac Prediction'.");
    }

    private static void performPrediction() {
        try {
            double age = Double.parseDouble(ageField.getText().trim());
            if (age < 1 || age > 120) throw new IllegalArgumentException("Age must be between 1 and 120.");

            double sex = (sexCombo.getSelectedIndex() == 0) ? 1.0 : 0.0;
            double cp = (double) cpCombo.getSelectedIndex();

            double trestbps = Double.parseDouble(bpField.getText().trim());
            if (trestbps < 60 || trestbps > 240) throw new IllegalArgumentException("Blood Pressure must be between 60 and 240 mm Hg.");

            double chol = Double.parseDouble(cholField.getText().trim());
            if (chol < 80 || chol > 700) throw new IllegalArgumentException("Cholesterol must be between 80 and 700 mg/dL.");

            double fbs = (double) fbsCombo.getSelectedIndex();
            double restecg = (double) ecgCombo.getSelectedIndex();

            double thalach = Double.parseDouble(maxHrField.getText().trim());
            if (thalach < 50 || thalach > 250) throw new IllegalArgumentException("Max Heart Rate must be between 50 and 250 bpm.");

            double exang = (double) exangCombo.getSelectedIndex();

            double oldpeak = Double.parseDouble(oldpeakField.getText().trim());
            if (oldpeak < 0.0 || oldpeak > 10.0) throw new IllegalArgumentException("Oldpeak must be between 0.0 and 10.0.");

            double slope = (double) slopeCombo.getSelectedIndex();
            double ca = (double) caCombo.getSelectedIndex();
            double thal = (double) (thalCombo.getSelectedIndex() + 1); // 1, 2, 3

            // Call Python Model via PythonBridge
            String[] args = new String[]{
                    String.valueOf(age),
                    String.valueOf(sex),
                    String.valueOf(cp),
                    String.valueOf(trestbps),
                    String.valueOf(chol),
                    String.valueOf(fbs),
                    String.valueOf(restecg),
                    String.valueOf(thalach),
                    String.valueOf(exang),
                    String.valueOf(oldpeak),
                    String.valueOf(slope),
                    String.valueOf(ca),
                    String.valueOf(thal)
            };

            PythonBridge.ExecutionResult execRes = PythonBridge.runScript("predict_heart.py", args);

            if (!execRes.isSuccess() && execRes.getOutput().isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "Python execution failed:\n" + execRes.getError(),
                        "Execution Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            String prediction = execRes.getOutput().trim();

            if (prediction.equalsIgnoreCase("Healthy Heart")) {
                resultStatusLabel.setText(" Diagnosis: Healthy Cardiac Profile");
                resultStatusLabel.setBackground(UIUtils.COLOR_SUCCESS_LIGHT);
                resultStatusLabel.setForeground(UIUtils.COLOR_SUCCESS);

                setResultImage("healthyheart.jpg");

                suggestionsArea.setText(
                        "PREVENTIVE CARDIAC RECOMMENDATIONS:\n\n" +
                        "1. Routine Monitoring: Check lipid profiles and blood pressure annually.\n" +
                        "2. Active Lifestyle: Maintain >= 150 minutes of moderate cardiovascular activity weekly.\n" +
                        "3. Mediterranean Diet: Emphasize extra virgin olive oil, nuts, leafy greens, and lean proteins.\n" +
                        "4. Hydration & Rest: Drink 2-3 liters of water and maintain 7-8 hours of restful sleep daily.\n" +
                        "5. Stress Regulation: Practice daily mindfulness, breathing exercises, or restorative yoga."
                );
            } else if (prediction.equalsIgnoreCase("Defective Heart")) {
                resultStatusLabel.setText(" Warning: Cardiac Abnormality Detected");
                resultStatusLabel.setBackground(UIUtils.COLOR_DANGER_LIGHT);
                resultStatusLabel.setForeground(UIUtils.COLOR_DANGER);

                setResultImage("defectiveheart.jpg");

                suggestionsArea.setText(
                        "CLINICAL ACTION PLAN & PRECAUTIONS:\n\n" +
                        "1. Physician Consultation: Promptly schedule a comprehensive clinical exam with a cardiologist.\n" +
                        "2. Diagnostic Follow-up: Consider an Echocardiogram, Angiography, or Exercise Stress Test.\n" +
                        "3. Dietary Sodium Restriction: Limit sodium intake to < 2,000 mg/day to reduce myocardial stress.\n" +
                        "4. Medication Adherence: Strictly take prescribed statins or anti-hypertensives as directed.\n" +
                        "5. Tobacco & Alcohol Cessation: Completely eliminate smoking and limit alcohol consumption."
                );
            } else {
                resultStatusLabel.setText("Output: " + prediction);
                resultStatusLabel.setBackground(UIUtils.COLOR_BG);
                resultStatusLabel.setForeground(UIUtils.COLOR_TEXT_MAIN);
                suggestionsArea.setText("Model response: " + prediction);
            }

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null,
                    "Please ensure all numeric fields (Age, BP, Cholesterol, Max HR, Oldpeak) have valid numbers.",
                    "Invalid Numeric Input",
                    JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException iae) {
            JOptionPane.showMessageDialog(null,
                    iae.getMessage(),
                    "Input Range Validation",
                    JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "An unexpected error occurred: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void setResultImage(String imagePath) {
        File imgFile = new File(imagePath);
        if (imgFile.exists()) {
            ImageIcon icon = new ImageIcon(imagePath);
            Image scaled = icon.getImage().getScaledInstance(180, 160, Image.SCALE_SMOOTH);
            resultImageLabel.setIcon(new ImageIcon(scaled));
        } else {
            resultImageLabel.setIcon(null);
            resultImageLabel.setText("[Image: " + imagePath + "]");
        }
    }
}