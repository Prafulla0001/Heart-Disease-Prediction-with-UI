import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONObject;

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
    private static JTextField patientNameField;
    private static JCheckBox saveHistoryCheckBox;

    // Result Components
    private static JLabel resultStatusLabel;
    private static JProgressBar riskProgressBar;
    private static JLabel resultImageLabel;
    private static JTextArea suggestionsArea;
    private static JTextArea riskFactorsArea;
    private static JButton exportBtn;

    // Last Prediction Cache for Exporting
    private static String lastDiagnosis = "";
    private static double lastRiskScore = 0.0;
    private static String lastRiskLevel = "";
    private static String lastSuggestions = "";
    private static String lastRiskFactors = "";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HeartDiseasePredictionUI::buildAndShowUI);
    }

    public static void buildAndShowUI() {
        JFrame frame = new JFrame("Heart Disease Prediction System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1250, 880);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // 1. Header
        JPanel headerPanel = UIUtils.createHeader(
                "Heart Disease Risk Assessment",
                "13-Factor AI Cardiac Diagnostic Screening with Risk Probability Meter",
                () -> {
                    frame.dispose();
                    MainMenuUI.launchMainMenu();
                }
        );
        frame.add(headerPanel, BorderLayout.NORTH);

        // 2. Main Container
        JPanel mainContent = new JPanel(new BorderLayout(20, 20));
        mainContent.setBackground(UIUtils.COLOR_BG);
        mainContent.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Form Container Card
        JPanel formCard = UIUtils.createCardPanel(Color.WHITE, 16);
        formCard.setLayout(new BorderLayout(12, 12));
        formCard.setBorder(new EmptyBorder(15, 20, 15, 20));

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
        JPanel formGrid = new JPanel(new GridLayout(8, 2, 18, 12));
        formGrid.setOpaque(false);
        formGrid.setBorder(new EmptyBorder(5, 0, 5, 0));

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
        patientNameField = UIUtils.createStyledTextField("Optional");
        patientNameField.setToolTipText("Leave blank to save this assessment as Guest Patient.");

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
        formGrid.add(createFieldWrapper("Patient Name (optional):", patientNameField));

        // Action Panel
        JPanel predictActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        predictActionPanel.setOpaque(false);
        saveHistoryCheckBox = new JCheckBox(
                "Save to " + HealthHistoryStore.getStorageDescription()
        );
        saveHistoryCheckBox.setOpaque(false);
        saveHistoryCheckBox.setToolTipText(
                "Opt in to saving this assessment to "
                        + HealthHistoryStore.getStorageDescription() + "."
        );
        predictActionPanel.add(saveHistoryCheckBox);
        JButton predictBtn = UIUtils.createStyledButton(" Run ML Cardiac Prediction", UIUtils.COLOR_PRIMARY, Color.WHITE, 260, 40);
        predictBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        predictBtn.addActionListener(e -> performPrediction());
        predictActionPanel.add(predictBtn);
        formGrid.add(predictActionPanel);

        formCard.add(formGrid, BorderLayout.CENTER);

        // 3. Right: Result Display Card
        JPanel resultCard = UIUtils.createCardPanel(Color.WHITE, 16);
        resultCard.setLayout(new BorderLayout(12, 12));
        resultCard.setBorder(new EmptyBorder(18, 18, 18, 18));
        resultCard.setPreferredSize(new Dimension(460, 0));

        JLabel resultTitle = new JLabel("Diagnostic Output & Risk Analysis");
        resultTitle.setFont(UIUtils.FONT_HEADER);
        resultTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);
        resultCard.add(resultTitle, BorderLayout.NORTH);

        JPanel resultBody = new JPanel();
        resultBody.setLayout(new BoxLayout(resultBody, BoxLayout.Y_AXIS));
        resultBody.setOpaque(false);

        // Status Banner
        resultStatusLabel = new JLabel("Awaiting patient evaluation...", JLabel.CENTER);
        resultStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        resultStatusLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);
        resultStatusLabel.setOpaque(true);
        resultStatusLabel.setBackground(UIUtils.COLOR_BG);
        resultStatusLabel.setBorder(new EmptyBorder(10, 14, 10, 14));
        resultStatusLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        resultBody.add(resultStatusLabel);
        resultBody.add(Box.createVerticalStrut(10));

        // Risk Progress Gauge
        JLabel riskGaugeTitle = new JLabel("Cardiac Disease Probability Risk Meter:");
        riskGaugeTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        riskGaugeTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);
        resultBody.add(riskGaugeTitle);
        resultBody.add(Box.createVerticalStrut(4));

        riskProgressBar = new JProgressBar(0, 100);
        riskProgressBar.setValue(0);
        riskProgressBar.setStringPainted(true);
        riskProgressBar.setString("Risk Score: -- %");
        riskProgressBar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        riskProgressBar.setPreferredSize(new Dimension(420, 26));
        riskProgressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        riskProgressBar.setForeground(UIUtils.COLOR_PRIMARY);
        resultBody.add(riskProgressBar);
        resultBody.add(Box.createVerticalStrut(10));

        // Detected Risk Factors Area
        JLabel factorsTitle = new JLabel("Contributing Clinical Risk Flags:");
        factorsTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        factorsTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);
        resultBody.add(factorsTitle);
        resultBody.add(Box.createVerticalStrut(4));

        riskFactorsArea = new JTextArea(3, 30);
        riskFactorsArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        riskFactorsArea.setLineWrap(true);
        riskFactorsArea.setWrapStyleWord(true);
        riskFactorsArea.setEditable(false);
        riskFactorsArea.setBackground(new Color(254, 249, 231));
        riskFactorsArea.setText("None identified yet.");
        riskFactorsArea.setBorder(new EmptyBorder(6, 8, 6, 8));

        JScrollPane factorsScroll = new JScrollPane(riskFactorsArea);
        factorsScroll.setPreferredSize(new Dimension(420, 75));
        factorsScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        factorsScroll.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER));
        resultBody.add(factorsScroll);
        resultBody.add(Box.createVerticalStrut(10));

        // Image & Recommendations Area
        JPanel centerImagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerImagePanel.setOpaque(false);
        resultImageLabel = new JLabel("", JLabel.CENTER);
        resultImageLabel.setPreferredSize(new Dimension(140, 100));
        centerImagePanel.add(resultImageLabel);
        resultBody.add(centerImagePanel);
        resultBody.add(Box.createVerticalStrut(6));

        suggestionsArea = new JTextArea(6, 30);
        suggestionsArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        suggestionsArea.setLineWrap(true);
        suggestionsArea.setWrapStyleWord(true);
        suggestionsArea.setEditable(false);
        suggestionsArea.setBackground(new Color(250, 252, 255));
        suggestionsArea.setText("Select a preset or enter patient metrics on the left to generate clinical recommendations.");

        JScrollPane scrollPane = new JScrollPane(suggestionsArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UIUtils.COLOR_BORDER),
                "Personalized Action Plan"
        ));
        scrollPane.setPreferredSize(new Dimension(420, 140));
        resultBody.add(scrollPane);

        resultCard.add(resultBody, BorderLayout.CENTER);

        // Export Report Button at the bottom of result card
        exportBtn = UIUtils.createStyledButton(" Export Medical Summary (.txt)", UIUtils.COLOR_SECONDARY, Color.WHITE, 240, 36);
        exportBtn.setEnabled(false);
        exportBtn.addActionListener(e -> exportMedicalReport());

        JPanel exportWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        exportWrapper.setOpaque(false);
        exportWrapper.add(exportBtn);
        resultCard.add(exportWrapper, BorderLayout.SOUTH);

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
        patientNameField.setText("");
        saveHistoryCheckBox.setSelected(false);
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

        resultStatusLabel.setText("Awaiting patient evaluation...");
        resultStatusLabel.setBackground(UIUtils.COLOR_BG);
        resultStatusLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);
        riskProgressBar.setValue(0);
        riskProgressBar.setString("Risk Score: -- %");
        riskFactorsArea.setText("None identified yet.");
        resultImageLabel.setIcon(null);
        suggestionsArea.setText("Please enter patient metrics on the left to generate clinical recommendations.");
        exportBtn.setEnabled(false);
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
                        "Prediction execution failed:\n" + execRes.getError(),
                        "Execution Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            String outputStr = execRes.getOutput().trim();
            JSONObject json = new JSONObject(outputStr);
            if (json.has("error")) {
                JOptionPane.showMessageDialog(
                        null,
                        "Prediction failed:\n" + json.getString("error"),
                        "Prediction Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            String prediction = json.optString("prediction", "Healthy Heart");
            double riskScore = json.optDouble("risk_score", 0.0);
            String riskLevel = json.optString("risk_level", "Low Risk");
            if (saveHistoryCheckBox.isSelected()) {
                JSONObject inputs = new JSONObject();
                String[] featureNames = {
                        "age", "sex", "chest_pain_type", "resting_blood_pressure",
                        "cholesterol", "fasting_blood_sugar", "resting_ecg",
                        "maximum_heart_rate", "exercise_angina", "st_depression",
                        "st_slope", "major_vessels", "thalassemia"
                };
                for (int i = 0; i < featureNames.length; i++) {
                    inputs.put(featureNames[i], Double.parseDouble(args[i]));
                }
                try {
                    HealthHistoryStore.saveHeartAssessment(
                            patientNameField.getText(),
                            (int) age,
                            sexCombo.getSelectedIndex() == 0 ? "Male" : "Female",
                            inputs,
                            prediction,
                            riskLevel,
                            riskScore
                    );
                } catch (IOException exception) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Prediction completed, but the assessment could not be saved:\n"
                                    + exception.getMessage(),
                            "History Save Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }

            JSONArray factorsArray = json.optJSONArray("risk_factors");
            StringBuilder factorsText = new StringBuilder();
            if (factorsArray != null && factorsArray.length() > 0) {
                for (int i = 0; i < factorsArray.length(); i++) {
                    factorsText.append("• ").append(factorsArray.getString(i)).append("\n");
                }
            } else {
                factorsText.append("• All primary biomarkers within normal clinical limits.\n");
            }
            riskFactorsArea.setText(factorsText.toString().trim());

            // Update Progress Bar & Gauges
            int progressVal = (int) Math.round(riskScore);
            riskProgressBar.setValue(progressVal);
            riskProgressBar.setString(String.format("Risk Probability: %.1f%% (%s)", riskScore, riskLevel));

            if (riskScore >= 70.0) {
                riskProgressBar.setForeground(UIUtils.COLOR_DANGER);
            } else if (riskScore >= 35.0) {
                riskProgressBar.setForeground(UIUtils.COLOR_WARNING);
            } else {
                riskProgressBar.setForeground(UIUtils.COLOR_SUCCESS);
            }

            // Cache data for export
            lastDiagnosis = prediction;
            lastRiskScore = riskScore;
            lastRiskLevel = riskLevel;
            lastRiskFactors = factorsText.toString().trim();

            if (prediction.equalsIgnoreCase("Healthy Heart")) {
                resultStatusLabel.setText(" Diagnosis: Low Cardiac Risk Profile");
                resultStatusLabel.setBackground(UIUtils.COLOR_SUCCESS_LIGHT);
                resultStatusLabel.setForeground(UIUtils.COLOR_SUCCESS);

                setResultImage("healthyheart.jpg");

                lastSuggestions =
                        "PREVENTIVE CARDIAC RECOMMENDATIONS:\n\n" +
                        "1. Routine Monitoring: Check lipid profile and resting blood pressure annually.\n" +
                        "2. Active Lifestyle: Maintain >= 150 minutes of moderate aerobic activity weekly.\n" +
                        "3. Mediterranean Diet: Emphasize olive oil, nuts, avocados, leafy greens, and lean proteins.\n" +
                        "4. Hydration & Sleep: Drink 2.5L water daily and maintain 7-8 hours of quality sleep.\n" +
                        "5. Stress Regulation: Practice daily mindfulness meditation, deep breathing, or light yoga.";

                suggestionsArea.setText(lastSuggestions);
            } else {
                resultStatusLabel.setText(" Warning: Elevated Cardiac Risk Detected");
                resultStatusLabel.setBackground(UIUtils.COLOR_DANGER_LIGHT);
                resultStatusLabel.setForeground(UIUtils.COLOR_DANGER);

                setResultImage("defectiveheart.jpg");

                lastSuggestions =
                        "CLINICAL ACTION PLAN & PRECAUTIONS:\n\n" +
                        "1. Urgent Consultation: Schedule a comprehensive evaluation with a certified cardiologist.\n" +
                        "2. Diagnostic Follow-up: Consider an Echocardiogram, Angiography, or Stress ECG.\n" +
                        "3. Dietary Sodium Restriction: Limit sodium to < 2,000 mg/day to lower myocardial workload.\n" +
                        "4. Medication Adherence: Strictly follow prescribed anti-hypertensives or statins.\n" +
                        "5. Lifestyle Moderation: Complete tobacco cessation, restrict alcohol, and avoid overexertion.";

                suggestionsArea.setText(lastSuggestions);
            }

            exportBtn.setEnabled(true);

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

    private static void exportMedicalReport() {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String displayTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String fileName = "Heart_Assessment_" + timestamp + ".txt";

            File reportFile = new File(fileName);
            try (var writer = Files.newBufferedWriter(reportFile.toPath(), StandardCharsets.UTF_8)) {
                writer.write("===============================================================\n");
                writer.write("           HEALTH PREDICTION & WELLNESS SYSTEM                 \n");
                writer.write("               CARDIAC RISK ASSESSMENT REPORT                  \n");
                writer.write("===============================================================\n\n");
                writer.write("Generated On   : " + displayTime + "\n");
                writer.write("Diagnostic Result : " + lastDiagnosis + "\n");
                writer.write(String.format("Calculated Risk: %.1f%% (%s)\n\n", lastRiskScore, lastRiskLevel));
                writer.write("---------------------------------------------------------------\n");
                writer.write("CLINICAL INPUT BIOMARKERS:\n");
                writer.write("---------------------------------------------------------------\n");
                writer.write("• Age                    : " + ageField.getText() + " years\n");
                writer.write("• Sex                    : " + sexCombo.getSelectedItem() + "\n");
                writer.write("• Chest Pain Type        : " + cpCombo.getSelectedItem() + "\n");
                writer.write("• Resting Blood Pressure : " + bpField.getText() + " mm Hg\n");
                writer.write("• Serum Cholesterol      : " + cholField.getText() + " mg/dL\n");
                writer.write("• Fasting Blood Sugar    : " + fbsCombo.getSelectedItem() + "\n");
                writer.write("• Resting ECG            : " + ecgCombo.getSelectedItem() + "\n");
                writer.write("• Max Heart Rate         : " + maxHrField.getText() + " bpm\n");
                writer.write("• Exercise Induced Angina: " + exangCombo.getSelectedItem() + "\n");
                writer.write("• ST Depression (Oldpeak): " + oldpeakField.getText() + "\n");
                writer.write("• Peak ST Slope          : " + slopeCombo.getSelectedItem() + "\n");
                writer.write("• Major Vessels (CA)     : " + caCombo.getSelectedItem() + "\n");
                writer.write("• Thallium Stress Result : " + thalCombo.getSelectedItem() + "\n\n");
                writer.write("---------------------------------------------------------------\n");
                writer.write("IDENTIFIED CLINICAL RISK FACTORS:\n");
                writer.write("---------------------------------------------------------------\n");
                writer.write(lastRiskFactors + "\n\n");
                writer.write("---------------------------------------------------------------\n");
                writer.write("RECOMMENDED CLINICAL ACTION PLAN:\n");
                writer.write("---------------------------------------------------------------\n");
                writer.write(lastSuggestions + "\n\n");
                writer.write("===============================================================\n");
                writer.write("DISCLAIMER: This automated report is generated using Machine    \n");
                writer.write("Learning algorithms for preliminary screening purposes only.   \n");
                writer.write("It does not replace professional clinical diagnosis.          \n");
                writer.write("===============================================================\n");
            }

            JOptionPane.showMessageDialog(null,
                    "Medical summary successfully exported to:\n" + reportFile.getAbsolutePath(),
                    "Report Exported",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Failed to export report: " + ex.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void setResultImage(String imagePath) {
        File imgFile = new File(imagePath);
        if (imgFile.exists()) {
            ImageIcon icon = new ImageIcon(imagePath);
            Image scaled = icon.getImage().getScaledInstance(140, 100, Image.SCALE_SMOOTH);
            resultImageLabel.setIcon(new ImageIcon(scaled));
        } else {
            resultImageLabel.setIcon(null);
            resultImageLabel.setText("[Image: " + imagePath + "]");
        }
    }
}