import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BMICalculator {
    private static JTextField weightField;
    private static JTextField heightField;
    private static JComboBox<String> unitCombo;
    private static JLabel resultBmiValue;
    private static JLabel resultCategoryLabel;
    private static JLabel idealWeightLabel;
    private static JLabel tipLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BMICalculator::buildAndShowUI);
    }

    public static void buildAndShowUI() {
        JFrame frame = new JFrame("BMI & Body Composition Calculator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(950, 700);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = UIUtils.createHeader(
                "BMI & Body Composition Calculator",
                "Assess Body Mass Index, Weight Category & Ideal Weight Range",
                () -> {
                    frame.dispose();
                    MainMenuUI.launchMainMenu();
                }
        );
        frame.add(headerPanel, BorderLayout.NORTH);

        // Content
        JPanel mainContent = new JPanel(new GridLayout(1, 2, 25, 25));
        mainContent.setBackground(UIUtils.COLOR_BG);
        mainContent.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Left: Input Card
        JPanel inputCard = UIUtils.createCardPanel(Color.WHITE, 16);
        inputCard.setLayout(new BorderLayout(15, 15));
        inputCard.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel inputTitle = new JLabel("Enter Measurements");
        inputTitle.setFont(UIUtils.FONT_HEADER);
        inputTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);
        inputCard.add(inputTitle, BorderLayout.NORTH);

        JPanel formGrid = new JPanel(new GridLayout(4, 1, 0, 15));
        formGrid.setOpaque(false);

        unitCombo = UIUtils.createStyledComboBox(new String[]{
                "Metric Units (kg, cm)",
                "Imperial / US Units (kg, feet)"
        });

        weightField = UIUtils.createStyledTextField("e.g. 70");
        heightField = UIUtils.createStyledTextField("e.g. 175 (cm) or 5.9 (feet)");

        formGrid.add(createFieldWrapper("Measurement System:", unitCombo));
        formGrid.add(createFieldWrapper("Body Weight:", weightField));
        formGrid.add(createFieldWrapper("Height:", heightField));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnPanel.setOpaque(false);
        JButton calculateBtn = UIUtils.createStyledButton(" Calculate BMI", UIUtils.COLOR_PRIMARY, Color.WHITE, 180, 40);
        calculateBtn.addActionListener(e -> calculateBMI());

        JButton sampleBtn = UIUtils.createStyledButton(" Sample (70kg, 175cm)", UIUtils.COLOR_SECONDARY, Color.WHITE, 180, 40);
        sampleBtn.addActionListener(e -> {
            unitCombo.setSelectedIndex(0);
            weightField.setText("70");
            heightField.setText("175");
            calculateBMI();
        });

        btnPanel.add(calculateBtn);
        btnPanel.add(sampleBtn);
        formGrid.add(btnPanel);

        inputCard.add(formGrid, BorderLayout.CENTER);

        // Right: Result Card
        JPanel resultCard = UIUtils.createCardPanel(Color.WHITE, 16);
        resultCard.setLayout(new BorderLayout(15, 15));
        resultCard.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel resultTitle = new JLabel("BMI Assessment");
        resultTitle.setFont(UIUtils.FONT_HEADER);
        resultTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);
        resultCard.add(resultTitle, BorderLayout.NORTH);

        JPanel resultGrid = new JPanel(new GridLayout(4, 1, 0, 12));
        resultGrid.setOpaque(false);

        resultBmiValue = new JLabel("BMI: --", JLabel.CENTER);
        resultBmiValue.setFont(new Font("Segoe UI", Font.BOLD, 36));
        resultBmiValue.setForeground(UIUtils.COLOR_PRIMARY);

        resultCategoryLabel = new JLabel("Category: Awaiting Input", JLabel.CENTER);
        resultCategoryLabel.setFont(UIUtils.FONT_SUBHEADER);
        resultCategoryLabel.setOpaque(true);
        resultCategoryLabel.setBackground(UIUtils.COLOR_BG);
        resultCategoryLabel.setBorder(new EmptyBorder(8, 12, 8, 12));

        idealWeightLabel = new JLabel("Ideal Weight Range: --", JLabel.CENTER);
        idealWeightLabel.setFont(UIUtils.FONT_REGULAR);
        idealWeightLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);

        tipLabel = new JLabel("<html><center>Normal BMI range is 18.5 - 24.9 kg/m².<br>Maintaining normal weight lowers cardiovascular disease risks.</center></html>", JLabel.CENTER);
        tipLabel.setFont(UIUtils.FONT_SMALL);
        tipLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);

        resultGrid.add(resultBmiValue);
        resultGrid.add(resultCategoryLabel);
        resultGrid.add(idealWeightLabel);
        resultGrid.add(tipLabel);

        resultCard.add(resultGrid, BorderLayout.CENTER);

        mainContent.add(inputCard);
        mainContent.add(resultCard);

        frame.add(mainContent, BorderLayout.CENTER);
        frame.setVisible(true);
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

    private static void calculateBMI() {
        try {
            double weight = Double.parseDouble(weightField.getText().trim());
            double heightInput = Double.parseDouble(heightField.getText().trim());

            if (weight <= 10 || weight > 350) {
                JOptionPane.showMessageDialog(null, "Please enter a realistic weight (10 - 350 kg).", "Input Range", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double heightMeters;
            if (unitCombo.getSelectedIndex() == 0) { // Metric (cm)
                if (heightInput < 50 || heightInput > 260) {
                    JOptionPane.showMessageDialog(null, "Please enter a realistic height in cm (50 - 260 cm).", "Input Range", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                heightMeters = heightInput / 100.0;
            } else { // Feet
                if (heightInput < 2.0 || heightInput > 8.5) {
                    JOptionPane.showMessageDialog(null, "Please enter height in feet (e.g. 5.8).", "Input Range", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                heightMeters = heightInput * 0.3048;
            }

            double bmi = weight / (heightMeters * heightMeters);

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

            resultBmiValue.setText(String.format("%.1f", bmi) + " kg/m²");
            resultCategoryLabel.setText("Category: " + category);
            resultCategoryLabel.setForeground(catColor);
            resultCategoryLabel.setBackground(bgColor);

            // Compute ideal weight range (BMI 18.5 to 24.9)
            double minHealthyKg = 18.5 * (heightMeters * heightMeters);
            double maxHealthyKg = 24.9 * (heightMeters * heightMeters);

            idealWeightLabel.setText(String.format("Ideal Weight Range: %.1f kg - %.1f kg", minHealthyKg, maxHealthyKg));

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Please enter valid numeric values for weight and height.", "Invalid Number", JOptionPane.WARNING_MESSAGE);
        }
    }
}
