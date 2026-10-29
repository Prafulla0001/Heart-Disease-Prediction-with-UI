import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class HealthHistoryUI {
    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final JFrame frame = new JFrame("Health Assessment History");
    private final JTextField searchField = UIUtils.createStyledTextField("Patient name");
    private final JTextField fromDateField = UIUtils.createStyledTextField("YYYY-MM-DD");
    private final JTextField toDateField = UIUtils.createStyledTextField("YYYY-MM-DD");
    private final JComboBox<String> testTypeCombo =
            UIUtils.createStyledComboBox(new String[]{"All", "Heart Disease"});
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Date", "Patient", "Age", "Gender", "Test", "Result", "Risk"},
            0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private List<HealthHistoryStore.HistoryEntry> visibleEntries = List.of();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HealthHistoryUI().show());
    }

    private void show() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1120, 700);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());
        frame.add(UIUtils.createHeader(
                "Health Assessment History",
                "Search locally saved screening results",
                () -> {
                    frame.dispose();
                    MainMenuUI.launchMainMenu();
                }
        ), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBackground(UIUtils.COLOR_BG);
        content.setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel filters = new JPanel(new GridLayout(2, 4, 10, 8));
        filters.setOpaque(false);
        filters.add(createFilter("Patient search", searchField));
        filters.add(createFilter("Assessment type", testTypeCombo));
        filters.add(createFilter("From date (optional)", fromDateField));
        filters.add(createFilter("To date (optional)", toDateField));

        JButton searchButton = UIUtils.createStyledButton(
                "Search History", UIUtils.COLOR_PRIMARY, Color.WHITE, 160, 36
        );
        searchButton.addActionListener(event -> refresh());
        JButton detailsButton = UIUtils.createStyledButton(
                "View Inputs", UIUtils.COLOR_SECONDARY, Color.WHITE, 130, 36
        );
        detailsButton.addActionListener(event -> showSelectedInputs());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        actions.add(searchButton);
        actions.add(detailsButton);
        filters.add(actions);
        filters.add(new JLabel("Dates use YYYY-MM-DD."));
        filters.add(new JLabel());
        filters.add(new JLabel());
        content.add(filters, BorderLayout.NORTH);

        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(26);
        table.getTableHeader().setReorderingAllowed(false);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() == 2) {
                    showSelectedInputs();
                }
            }
        });
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER));
        content.add(tableScroll, BorderLayout.CENTER);

        JLabel privacyNote = new JLabel(
                "History is stored locally in health_history.json. Only assessments explicitly opted in on the prediction screen are saved."
        );
        privacyNote.setFont(UIUtils.FONT_SMALL);
        privacyNote.setForeground(UIUtils.COLOR_TEXT_MUTED);
        content.add(privacyNote, BorderLayout.SOUTH);
        frame.add(content, BorderLayout.CENTER);
        refresh();
        frame.setVisible(true);
    }

    private static JPanel createFilter(String title, JComponent input) {
        JPanel wrapper = new JPanel(new BorderLayout(4, 4));
        wrapper.setOpaque(false);
        JLabel label = new JLabel(title);
        label.setFont(UIUtils.FONT_BOLD);
        wrapper.add(label, BorderLayout.NORTH);
        wrapper.add(input, BorderLayout.CENTER);
        return wrapper;
    }

    private void refresh() {
        try {
            LocalDate fromDate = parseDate(fromDateField.getText());
            LocalDate toDate = parseDate(toDateField.getText());
            visibleEntries = HealthHistoryStore.find(
                    searchField.getText(),
                    (String) testTypeCombo.getSelectedItem(),
                    fromDate,
                    toDate
            );
            tableModel.setRowCount(0);
            for (HealthHistoryStore.HistoryEntry entry : visibleEntries) {
                tableModel.addRow(new Object[]{
                        DISPLAY_DATE.format(java.time.Instant.parse(entry.timestamp)
                                .atZone(java.time.ZoneId.systemDefault())),
                        entry.patientName,
                        entry.age,
                        entry.gender,
                        entry.testType,
                        entry.result,
                        String.format("%.1f%% (%s)", entry.riskScore, entry.riskLevel)
                });
            }
        } catch (DateTimeParseException exception) {
            JOptionPane.showMessageDialog(
                    frame, "Enter dates in YYYY-MM-DD format.",
                    "Invalid Date", JOptionPane.WARNING_MESSAGE
            );
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(
                    frame, exception.getMessage(), "Invalid Filter",
                    JOptionPane.WARNING_MESSAGE
            );
        } catch (IOException exception) {
            showHistoryError(exception);
        }
    }

    private static LocalDate parseDate(String value) {
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : LocalDate.parse(trimmed);
    }

    private void showSelectedInputs() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= visibleEntries.size()) {
            JOptionPane.showMessageDialog(
                    frame, "Select a history record first.",
                    "No Record Selected", JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }
        HealthHistoryStore.HistoryEntry selected = visibleEntries.get(selectedRow);
        JTextArea details = new JTextArea(selected.inputs.toString(2), 18, 48);
        details.setEditable(false);
        details.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JOptionPane.showMessageDialog(
                frame, new JScrollPane(details),
                "Clinical Inputs — " + selected.patientName,
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showHistoryError(IOException exception) {
        JOptionPane.showMessageDialog(
                frame,
                "Could not read local history:\n" + exception.getMessage(),
                "History Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
