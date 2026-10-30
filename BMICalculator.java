import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;

public class BMICalculator {
    // Tab 1: BMI Controls
    private static JTextField weightField;
    private static JTextField heightField;
    private static JComboBox<String> unitCombo;
    private static JLabel resultBmiValue;
    private static JLabel resultCategoryLabel;
    private static JLabel idealWeightLabel;
    private static JLabel weightDifferenceLabel;
    private static JTextField bmiPatientNameField;

    // Tab 2: BMR & Calorie Controls
    private static JTextField bmrAgeField;
    private static JComboBox<String> bmrGenderCombo;
    private static JTextField bmrWeightField;
    private static JTextField bmrHeightField;
    private static JComboBox<String> activityCombo;
    private static JLabel bmrResultLabel;
    private static JLabel tdeeResultLabel;
    private static JLabel lossCalorieLabel;
    private static JLabel gainCalorieLabel;

    // Tab 3: Hydration & Heart Zones
    private static JTextField hydroWeightField;
    private static JComboBox<String> hydroActivityCombo;
    private static JLabel hydroResultLabel;
    private static JTextField hrAgeField;
    private static JLabel fatBurnZoneLabel;
    private static JLabel cardioZoneLabel;
    private static JLabel peakZoneLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BMICalculator::buildAndShowUI);
    }

    public static void buildAndShowUI() {
        JFrame frame = new JFrame("Body Composition & Metabolic Health Suite");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 800);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = UIUtils.createHeader(
                "Metabolic Health & Body Composition Suite",
                "Calculate BMI, Ideal Weight, BMR Calories, Hydration Targets & Cardiac Training Zones",
                () -> {
                    frame.dispose();
                    MainMenuUI.launchMainMenu();
                }
        );
        frame.add(headerPanel, BorderLayout.NORTH);

        // JTabbedPane for the 3 Wellness Workstations
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(Color.WHITE);

        tabbedPane.addTab(" BMI & Ideal Weight", createBMIPanel());
        tabbedPane.addTab(" BMR & Daily Calories (TDEE)", createBMRPanel());
        tabbedPane.addTab(" Hydration & Training Zones", createHydrationAndHRPanel());

        frame.add(tabbedPane, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    // ==========================================
    // TAB 1: BMI & IDEAL WEIGHT
    // ==========================================
    private static JPanel createBMIPanel() {
        JPanel container = new JPanel(new GridLayout(1, 2, 25, 25));
        container.setBackground(UIUtils.COLOR_BG);
        container.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Left Input Card
        JPanel inputCard = UIUtils.createCardPanel(Color.WHITE, 16);
        inputCard.setLayout(new BorderLayout(15, 15));
        inputCard.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel inputTitle = new JLabel("Body Measurements");
        inputTitle.setFont(UIUtils.FONT_HEADER);
        inputTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);
        inputCard.add(inputTitle, BorderLayout.NORTH);

        JPanel formGrid = new JPanel(new GridLayout(5, 1, 0, 12));
        formGrid.setOpaque(false);

        unitCombo = UIUtils.createStyledComboBox(new String[]{
                "Metric Units (kg, cm)",
                "Imperial Units (lbs, feet.inches e.g. 5.9)"
        });

        weightField = UIUtils.createStyledTextField("e.g. 70");
        heightField = UIUtils.createStyledTextField("e.g. 175 (cm) or 5.9 (ft)");

        formGrid.add(createFieldWrapper("Measurement System:", unitCombo));
        formGrid.add(createFieldWrapper("Body Weight:", weightField));
        formGrid.add(createFieldWrapper("Height:", heightField));
        bmiPatientNameField = UIUtils.createStyledTextField("Optional");
        formGrid.add(createFieldWrapper("Patient Name (optional):", bmiPatientNameField));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnPanel.setOpaque(false);
        JCheckBox saveHistory = new JCheckBox(
                "Save to " + HealthHistoryStore.getStorageDescription()
        );
        saveHistory.setOpaque(false);
        saveHistory.setToolTipText(
                "Save this BMI assessment to " + HealthHistoryStore.getStorageDescription() + "."
        );
        JButton calculateBtn = UIUtils.createStyledButton(" Calculate BMI", UIUtils.COLOR_PRIMARY, Color.WHITE, 160, 38);
        calculateBtn.addActionListener(e -> calculateBMI(saveHistory.isSelected()));

        JButton sampleBtn = UIUtils.createStyledButton(" Sample Profile", UIUtils.COLOR_SECONDARY, Color.WHITE, 150, 38);
        sampleBtn.addActionListener(e -> {
            unitCombo.setSelectedIndex(0);
            weightField.setText("72");
            heightField.setText("176");
            calculateBMI(saveHistory.isSelected());
        });

        btnPanel.add(saveHistory);
        btnPanel.add(calculateBtn);
        btnPanel.add(sampleBtn);
        formGrid.add(btnPanel);

        inputCard.add(formGrid, BorderLayout.CENTER);

        // Right Result Card
        JPanel resultCard = UIUtils.createCardPanel(Color.WHITE, 16);
        resultCard.setLayout(new BorderLayout(15, 15));
        resultCard.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel resultTitle = new JLabel("BMI Classification & Targets");
        resultTitle.setFont(UIUtils.FONT_HEADER);
        resultTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);
        resultCard.add(resultTitle, BorderLayout.NORTH);

        JPanel resultGrid = new JPanel(new GridLayout(5, 1, 0, 10));
        resultGrid.setOpaque(false);

        resultBmiValue = new JLabel("BMI: -- kg/m²", JLabel.CENTER);
        resultBmiValue.setFont(new Font("Segoe UI", Font.BOLD, 38));
        resultBmiValue.setForeground(UIUtils.COLOR_PRIMARY);

        resultCategoryLabel = new JLabel("Category: Awaiting Input", JLabel.CENTER);
        resultCategoryLabel.setFont(UIUtils.FONT_SUBHEADER);
        resultCategoryLabel.setOpaque(true);
        resultCategoryLabel.setBackground(UIUtils.COLOR_BG);
        resultCategoryLabel.setBorder(new EmptyBorder(8, 12, 8, 12));

        idealWeightLabel = new JLabel("Ideal Weight Range: --", JLabel.CENTER);
        idealWeightLabel.setFont(UIUtils.FONT_BOLD);
        idealWeightLabel.setForeground(UIUtils.COLOR_TEXT_MAIN);

        weightDifferenceLabel = new JLabel("Target Adjustment: --", JLabel.CENTER);
        weightDifferenceLabel.setFont(UIUtils.FONT_REGULAR);
        weightDifferenceLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);

        JLabel infoLabel = new JLabel("<html><center>WHO Standard Guidelines:<br>Underweight &lt; 18.5 | Normal 18.5 - 24.9 | Overweight 25 - 29.9 | Obese &ge; 30</center></html>", JLabel.CENTER);
        infoLabel.setFont(UIUtils.FONT_SMALL);
        infoLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);

        resultGrid.add(resultBmiValue);
        resultGrid.add(resultCategoryLabel);
        resultGrid.add(idealWeightLabel);
        resultGrid.add(weightDifferenceLabel);
        resultGrid.add(infoLabel);

        resultCard.add(resultGrid, BorderLayout.CENTER);

        container.add(inputCard);
        container.add(resultCard);
        return container;
    }

    private static void calculateBMI(boolean saveToHistory) {
        try {
            double rawWeight = Double.parseDouble(weightField.getText().trim());
            double rawHeight = Double.parseDouble(heightField.getText().trim());
            if (!Double.isFinite(rawWeight) || !Double.isFinite(rawHeight)) {
                throw new IllegalArgumentException("Weight and height must be finite numbers.");
            }

            double weightKg;
            double heightMeters;

            if (unitCombo.getSelectedIndex() == 0) { // Metric
                if (rawWeight < 15 || rawWeight > 350) throw new IllegalArgumentException("Weight must be 15 - 350 kg.");
                if (rawHeight < 60 || rawHeight > 260) throw new IllegalArgumentException("Height must be 60 - 260 cm.");
                weightKg = rawWeight;
                heightMeters = rawHeight / 100.0;
            } else { // Imperial
                if (rawWeight < 30 || rawWeight > 750) throw new IllegalArgumentException("Weight must be 30 - 750 lbs.");
                if (rawHeight < 2.5 || rawHeight > 8.5) throw new IllegalArgumentException("Height must be 2.5 - 8.5 feet.");
                weightKg = rawWeight * 0.453592;
                heightMeters = rawHeight * 0.3048;
            }

            double bmi = weightKg / (heightMeters * heightMeters);

            String category;
            Color catColor;
            Color bgColor;

            if (bmi < 18.5) {
                category = "Underweight";
                catColor = UIUtils.COLOR_WARNING;
                bgColor = new Color(254, 249, 231);
            } else if (bmi < 25.0) {
                category = "Normal / Healthy Weight";
                catColor = UIUtils.COLOR_SUCCESS;
                bgColor = UIUtils.COLOR_SUCCESS_LIGHT;
            } else if (bmi < 30.0) {
                category = "Overweight";
                catColor = UIUtils.COLOR_WARNING;
                bgColor = new Color(254, 249, 231);
            } else {
                category = "Obese";
                catColor = UIUtils.COLOR_DANGER;
                bgColor = UIUtils.COLOR_DANGER_LIGHT;
            }

            resultBmiValue.setText(String.format("%.1f kg/m²", bmi));
            resultCategoryLabel.setText("Category: " + category);
            resultCategoryLabel.setForeground(catColor);
            resultCategoryLabel.setBackground(bgColor);

            double minIdealKg = 18.5 * (heightMeters * heightMeters);
            double maxIdealKg = 24.9 * (heightMeters * heightMeters);
            idealWeightLabel.setText(String.format("Ideal Weight Range: %.1f kg - %.1f kg", minIdealKg, maxIdealKg));

            if (weightKg < minIdealKg) {
                double diff = minIdealKg - weightKg;
                weightDifferenceLabel.setText(String.format("Recommended: Gain +%.1f kg to reach normal BMI", diff));
                weightDifferenceLabel.setForeground(UIUtils.COLOR_WARNING);
            } else if (weightKg > maxIdealKg) {
                double diff = weightKg - maxIdealKg;
                weightDifferenceLabel.setText(String.format("Recommended: Lose -%.1f kg to reach normal BMI", diff));
                weightDifferenceLabel.setForeground(UIUtils.COLOR_DANGER);
            } else {
                weightDifferenceLabel.setText("Your weight is currently optimal! Keep up the good work.");
                weightDifferenceLabel.setForeground(UIUtils.COLOR_SUCCESS);
            }
            if (saveToHistory) {
                try {
                    HealthHistoryStore.saveBmiAssessment(
                            bmiPatientNameField.getText(),
                            weightKg,
                            heightMeters,
                            bmi,
                            category
                    );
                } catch (IOException exception) {
                    JOptionPane.showMessageDialog(
                            null,
                            "BMI was calculated, but the assessment could not be saved:\n"
                                    + exception.getMessage(),
                            "History Save Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Enter valid numeric weight and height values.", "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(null, "Input error: " + ex.getMessage(), "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ==========================================
    // TAB 2: BMR & CALORIES (TDEE)
    // ==========================================
    private static JPanel createBMRPanel() {
        JPanel container = new JPanel(new GridLayout(1, 2, 25, 25));
        container.setBackground(UIUtils.COLOR_BG);
        container.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Form Card
        JPanel inputCard = UIUtils.createCardPanel(Color.WHITE, 16);
        inputCard.setLayout(new BorderLayout(15, 15));
        inputCard.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("Metabolic Profile Parameters");
        title.setFont(UIUtils.FONT_HEADER);
        title.setForeground(UIUtils.COLOR_TEXT_MAIN);
        inputCard.add(title, BorderLayout.NORTH);

        JPanel formGrid = new JPanel(new GridLayout(6, 1, 0, 10));
        formGrid.setOpaque(false);

        bmrAgeField = UIUtils.createStyledTextField("e.g. 28");
        bmrGenderCombo = UIUtils.createStyledComboBox(new String[]{"Male", "Female"});
        bmrWeightField = UIUtils.createStyledTextField("e.g. 72 (kg)");
        bmrHeightField = UIUtils.createStyledTextField("e.g. 175 (cm)");
        activityCombo = UIUtils.createStyledComboBox(new String[]{
                "Sedentary (Little or no exercise)",
                "Lightly Active (Exercise 1-3 days/week)",
                "Moderately Active (Exercise 3-5 days/week)",
                "Very Active (Hard exercise 6-7 days/week)",
                "Extremely Active (Athletic training / physical job)"
        });

        formGrid.add(createFieldWrapper("Age (Years):", bmrAgeField));
        formGrid.add(createFieldWrapper("Biological Sex:", bmrGenderCombo));
        formGrid.add(createFieldWrapper("Weight (kg):", bmrWeightField));
        formGrid.add(createFieldWrapper("Height (cm):", bmrHeightField));
        formGrid.add(createFieldWrapper("Physical Activity Level:", activityCombo));

        JButton calcBmrBtn = UIUtils.createStyledButton(" Calculate Daily Calorie Needs", UIUtils.COLOR_PRIMARY, Color.WHITE, 250, 38);
        calcBmrBtn.addActionListener(e -> calculateBMR());
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnWrap.setOpaque(false);
        btnWrap.add(calcBmrBtn);
        formGrid.add(btnWrap);

        inputCard.add(formGrid, BorderLayout.CENTER);

        // Result Card
        JPanel resultCard = UIUtils.createCardPanel(Color.WHITE, 16);
        resultCard.setLayout(new BorderLayout(15, 15));
        resultCard.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel resTitle = new JLabel("Daily Caloric Breakdown (Mifflin-St Jeor)");
        resTitle.setFont(UIUtils.FONT_HEADER);
        resTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);
        resultCard.add(resTitle, BorderLayout.NORTH);

        JPanel resGrid = new JPanel(new GridLayout(4, 1, 0, 12));
        resGrid.setOpaque(false);

        bmrResultLabel = createMetricBadge("Basal Metabolic Rate (BMR):", "-- kcal / day");
        tdeeResultLabel = createMetricBadge("Maintenance Calories (TDEE):", "-- kcal / day");
        lossCalorieLabel = createMetricBadge("Weight Loss Target (-500 kcal):", "-- kcal / day");
        gainCalorieLabel = createMetricBadge("Muscle / Weight Gain (+500 kcal):", "-- kcal / day");

        resGrid.add(bmrResultLabel);
        resGrid.add(tdeeResultLabel);
        resGrid.add(lossCalorieLabel);
        resGrid.add(gainCalorieLabel);

        resultCard.add(resGrid, BorderLayout.CENTER);

        container.add(inputCard);
        container.add(resultCard);
        return container;
    }

    private static void calculateBMR() {
        try {
            int age = Integer.parseInt(bmrAgeField.getText().trim());
            double weight = Double.parseDouble(bmrWeightField.getText().trim());
            double height = Double.parseDouble(bmrHeightField.getText().trim());
            boolean isMale = bmrGenderCombo.getSelectedIndex() == 0;

            // Mifflin-St Jeor Formula
            double bmr;
            if (isMale) {
                bmr = (10.0 * weight) + (6.25 * height) - (5.0 * age) + 5.0;
            } else {
                bmr = (10.0 * weight) + (6.25 * height) - (5.0 * age) - 161.0;
            }

            double[] multipliers = {1.2, 1.375, 1.55, 1.725, 1.9};
            double tdee = bmr * multipliers[activityCombo.getSelectedIndex()];

            bmrResultLabel.setText("Basal Metabolic Rate (BMR): " + (int) Math.round(bmr) + " kcal / day");
            tdeeResultLabel.setText("Maintenance Calories (TDEE): " + (int) Math.round(tdee) + " kcal / day");
            lossCalorieLabel.setText("Weight Loss Target (-500 kcal): " + (int) Math.round(tdee - 500) + " kcal / day");
            gainCalorieLabel.setText("Muscle / Weight Gain (+500 kcal): " + (int) Math.round(tdee + 500) + " kcal / day");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Please enter valid age, weight, and height numbers.", "Input Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ==========================================
    // TAB 3: HYDRATION & TRAINING ZONES
    // ==========================================
    private static JPanel createHydrationAndHRPanel() {
        JPanel container = new JPanel(new GridLayout(1, 2, 25, 25));
        container.setBackground(UIUtils.COLOR_BG);
        container.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Hydration Card
        JPanel hydroCard = UIUtils.createCardPanel(Color.WHITE, 16);
        hydroCard.setLayout(new BorderLayout(15, 15));
        hydroCard.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel hydroTitle = new JLabel("Daily Hydration Target");
        hydroTitle.setFont(UIUtils.FONT_HEADER);
        hydroTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);
        hydroCard.add(hydroTitle, BorderLayout.NORTH);

        JPanel hydroForm = new JPanel(new GridLayout(4, 1, 0, 10));
        hydroForm.setOpaque(false);

        hydroWeightField = UIUtils.createStyledTextField("e.g. 70 (kg)");
        hydroActivityCombo = UIUtils.createStyledComboBox(new String[]{
                "Light Activity (Desk work)",
                "Moderate Activity (30-60 min workout)",
                "Heavy Activity (Intense training / Hot climate)"
        });

        hydroForm.add(createFieldWrapper("Body Weight (kg):", hydroWeightField));
        hydroForm.add(createFieldWrapper("Daily Physical Exertion:", hydroActivityCombo));

        JButton calcHydroBtn = UIUtils.createStyledButton(" Calculate Hydration Needs", UIUtils.COLOR_PRIMARY, Color.WHITE, 240, 38);
        calcHydroBtn.addActionListener(e -> calculateHydration());
        JPanel hBtnWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        hBtnWrap.setOpaque(false);
        hBtnWrap.add(calcHydroBtn);
        hydroForm.add(hBtnWrap);

        hydroResultLabel = new JLabel("Target: -- Liters (or -- Glasses)", JLabel.CENTER);
        hydroResultLabel.setFont(UIUtils.FONT_SUBHEADER);
        hydroResultLabel.setOpaque(true);
        hydroResultLabel.setBackground(UIUtils.COLOR_BG);
        hydroResultLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        hydroForm.add(hydroResultLabel);

        hydroCard.add(hydroForm, BorderLayout.CENTER);

        // Heart Rate Training Zones Card
        JPanel hrCard = UIUtils.createCardPanel(Color.WHITE, 16);
        hrCard.setLayout(new BorderLayout(15, 15));
        hrCard.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel hrTitle = new JLabel("Cardiac Heart Rate Training Zones");
        hrTitle.setFont(UIUtils.FONT_HEADER);
        hrTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);
        hrCard.add(hrTitle, BorderLayout.NORTH);

        JPanel hrForm = new JPanel(new GridLayout(5, 1, 0, 8));
        hrForm.setOpaque(false);

        hrAgeField = UIUtils.createStyledTextField("e.g. 35");
        hrForm.add(createFieldWrapper("Enter Age:", hrAgeField));

        JButton calcHrBtn = UIUtils.createStyledButton(" Calculate Heart Rate Zones", UIUtils.COLOR_SECONDARY, Color.WHITE, 250, 38);
        calcHrBtn.addActionListener(e -> calculateHRZones());
        JPanel hrBtnWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        hrBtnWrap.setOpaque(false);
        hrBtnWrap.add(calcHrBtn);
        hrForm.add(hrBtnWrap);

        fatBurnZoneLabel = createMetricBadge("Fat Burn Zone (60% - 70% HR):", "-- bpm");
        cardioZoneLabel = createMetricBadge("Aerobic Cardio Zone (70% - 85% HR):", "-- bpm");
        peakZoneLabel = createMetricBadge("Peak Performance Zone (85% - 100% HR):", "-- bpm");

        hrForm.add(fatBurnZoneLabel);
        hrForm.add(cardioZoneLabel);
        hrForm.add(peakZoneLabel);

        hrCard.add(hrForm, BorderLayout.CENTER);

        container.add(hydroCard);
        container.add(hrCard);
        return container;
    }

    private static void calculateHydration() {
        try {
            double weight = Double.parseDouble(hydroWeightField.getText().trim());
            // Baseline 35ml per kg
            double baseMl = weight * 35.0;
            int act = hydroActivityCombo.getSelectedIndex();
            double extraMl = (act == 0) ? 0 : (act == 1 ? 500 : 1000);
            double totalLiters = (baseMl + extraMl) / 1000.0;
            int glasses = (int) Math.round((totalLiters * 1000) / 250.0);

            hydroResultLabel.setText(String.format("Recommended Daily Intake: %.2f L (~%d standard glasses)", totalLiters, glasses));
            hydroResultLabel.setForeground(UIUtils.COLOR_PRIMARY);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Please enter a valid weight in kg.", "Input Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static void calculateHRZones() {
        try {
            int age = Integer.parseInt(hrAgeField.getText().trim());
            int maxHr = 220 - age;

            int fatMin = (int) Math.round(maxHr * 0.60);
            int fatMax = (int) Math.round(maxHr * 0.70);

            int cardioMin = (int) Math.round(maxHr * 0.70);
            int cardioMax = (int) Math.round(maxHr * 0.85);

            int peakMin = (int) Math.round(maxHr * 0.85);
            int peakMax = maxHr;

            fatBurnZoneLabel.setText(String.format("Fat Burn Zone (60-70%%): %d - %d bpm", fatMin, fatMax));
            cardioZoneLabel.setText(String.format("Aerobic Cardio (70-85%%): %d - %d bpm", cardioMin, cardioMax));
            peakZoneLabel.setText(String.format("Peak Performance (85-100%%): %d - %d bpm (Max: %d)", peakMin, peakMax, maxHr));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Please enter a valid age.", "Input Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static JLabel createMetricBadge(String label, String value) {
        JLabel badge = new JLabel(label + " " + value);
        badge.setFont(UIUtils.FONT_BOLD);
        badge.setForeground(UIUtils.COLOR_TEXT_MAIN);
        badge.setOpaque(true);
        badge.setBackground(new Color(245, 248, 252));
        badge.setBorder(new EmptyBorder(8, 12, 8, 12));
        return badge;
    }

    private static JPanel createFieldWrapper(String labelText, JComponent comp) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 4));
        wrapper.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UIUtils.FONT_BOLD);
        lbl.setForeground(UIUtils.COLOR_TEXT_MAIN);
        wrapper.add(lbl, BorderLayout.NORTH);
        wrapper.add(comp, BorderLayout.CENTER);
        return wrapper;
    }
}