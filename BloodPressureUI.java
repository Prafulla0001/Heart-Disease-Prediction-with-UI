import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class BloodPressureUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(BloodPressureUI::showWindow);
    }

    private static void showWindow() {
        JFrame frame = new JFrame("Blood Pressure Category Check");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(760, 500);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        frame.add(UIUtils.createHeader(
                "Blood Pressure Category Check",
                "A screening aid for adult blood pressure readings",
                () -> {
                    frame.dispose();
                    MainMenuUI.launchMainMenu();
                }
        ), BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(1, 2, 20, 20));
        content.setBackground(UIUtils.COLOR_BG);
        content.setBorder(new EmptyBorder(25, 30, 25, 30));

        JPanel inputCard = UIUtils.createCardPanel(Color.WHITE, 16);
        inputCard.setLayout(new GridLayout(4, 1, 8, 8));
        inputCard.setBorder(new EmptyBorder(24, 26, 24, 26));
        JLabel instruction = new JLabel("<html>Enter a resting blood pressure reading in mm Hg.</html>");
        instruction.setFont(UIUtils.FONT_SUBHEADER);
        JTextField systolicField = UIUtils.createStyledTextField("e.g. 118");
        JTextField diastolicField = UIUtils.createStyledTextField("e.g. 76");
        inputCard.add(createField("Systolic (top number)", systolicField));
        inputCard.add(createField("Diastolic (bottom number)", diastolicField));
        inputCard.add(instruction);

        JLabel result = new JLabel("Enter both values to view a category.", JLabel.CENTER);
        result.setFont(new Font("Segoe UI", Font.BOLD, 18));
        result.setOpaque(true);
        result.setBackground(UIUtils.COLOR_BG);
        result.setBorder(new EmptyBorder(20, 18, 20, 18));

        JButton classifyButton = UIUtils.createStyledButton(
                "Classify Reading", UIUtils.COLOR_PRIMARY, Color.WHITE, 180, 40
        );
        classifyButton.addActionListener(event -> classify(
                systolicField.getText(), diastolicField.getText(), result
        ));
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        actionPanel.setOpaque(false);
        actionPanel.add(classifyButton);
        inputCard.add(actionPanel);

        JPanel resultCard = UIUtils.createCardPanel(Color.WHITE, 16);
        resultCard.setLayout(new BorderLayout(12, 12));
        resultCard.setBorder(new EmptyBorder(24, 26, 24, 26));
        resultCard.add(new JLabel("Screening Result", JLabel.CENTER), BorderLayout.NORTH);
        resultCard.add(result, BorderLayout.CENTER);
        JLabel disclaimer = new JLabel(
                "<html><center>Screening only, not a diagnosis. Repeat unusual readings and consult a clinician.<br>" +
                "If a reading is above 180 systolic or 120 diastolic, seek urgent medical advice; " +
                "call emergency services for severe symptoms.</center></html>",
                JLabel.CENTER
        );
        disclaimer.setFont(UIUtils.FONT_SMALL);
        disclaimer.setForeground(UIUtils.COLOR_TEXT_MUTED);
        resultCard.add(disclaimer, BorderLayout.SOUTH);

        content.add(inputCard);
        content.add(resultCard);
        frame.add(content, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private static JPanel createField(String label, JTextField input) {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setOpaque(false);
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setFont(UIUtils.FONT_BOLD);
        panel.add(fieldLabel, BorderLayout.NORTH);
        panel.add(input, BorderLayout.CENTER);
        return panel;
    }

    private static void classify(String systolicInput, String diastolicInput, JLabel result) {
        try {
            int systolic = Integer.parseInt(systolicInput.trim());
            int diastolic = Integer.parseInt(diastolicInput.trim());
            String category = classifyReading(systolic, diastolic);
            Color color = category.equals("Normal Range")
                    ? UIUtils.COLOR_SUCCESS
                    : category.startsWith("Stage 2") || category.startsWith("Hypertensive")
                    ? UIUtils.COLOR_DANGER
                    : UIUtils.COLOR_WARNING;

            result.setText(String.format(
                    "<html><center>%s<br><br>%d / %d mm Hg</center></html>",
                    category, systolic, diastolic
            ));
            result.setForeground(color);
            result.setBackground(color == UIUtils.COLOR_SUCCESS
                    ? UIUtils.COLOR_SUCCESS_LIGHT
                    : color == UIUtils.COLOR_DANGER ? UIUtils.COLOR_DANGER_LIGHT : new Color(254, 249, 231));
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(
                    result, "Enter whole-number systolic and diastolic values.",
                    "Invalid Reading", JOptionPane.WARNING_MESSAGE
            );
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(
                    result, exception.getMessage(), "Reading Out of Range",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    public static String classifyReading(int systolic, int diastolic) {
        if (systolic < 70 || systolic > 250 || diastolic < 40 || diastolic > 150) {
            throw new IllegalArgumentException("Enter systolic 70-250 and diastolic 40-150 mm Hg.");
        }
        if (systolic > 180 || diastolic > 120) {
            return "Hypertensive Crisis Range";
        }
        if (systolic >= 140 || diastolic >= 90) {
            return "Stage 2 Hypertension Range";
        }
        if (systolic >= 130 || diastolic >= 80) {
            return "Stage 1 Hypertension Range";
        }
        if (systolic >= 120) {
            return "Elevated Range";
        }
        return "Normal Range";
    }
}
