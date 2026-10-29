import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class HealthHistoryStore {
    private static final Path HISTORY_FILE =
            Path.of(System.getProperty("user.dir"), "health_history.json");

    private HealthHistoryStore() {
    }

    public static synchronized void saveHeartAssessment(
            String patientName,
            int age,
            String gender,
            JSONObject inputs,
            String result,
            String riskLevel,
            double riskScore
    ) throws IOException {
        if (age < 1 || age > 120 || !Double.isFinite(riskScore)
                || riskScore < 0 || riskScore > 100) {
            throw new IllegalArgumentException("Invalid patient age or risk score.");
        }

        String normalizedName = patientName == null || patientName.isBlank()
                ? "Guest Patient"
                : patientName.trim();
        JSONArray history = readHistory();
        JSONObject entry = new JSONObject()
                .put("id", UUID.randomUUID().toString())
                .put("patient_name", normalizedName)
                .put("age", age)
                .put("gender", gender)
                .put("test_type", "Heart Disease")
                .put("inputs", new JSONObject(inputs.toString()))
                .put("result", result)
                .put("risk_level", riskLevel)
                .put("risk_score", riskScore)
                .put("timestamp", Instant.now().toString());
        history.put(entry);
        writeHistory(history);
    }

    public static synchronized List<HistoryEntry> find(
            String searchText,
            String testType,
            LocalDate fromDate,
            LocalDate toDate
    ) throws IOException {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("Start date must not be after end date.");
        }

        String search = searchText == null
                ? ""
                : searchText.trim().toLowerCase(Locale.ROOT);
        List<HistoryEntry> results = new ArrayList<>();
        JSONArray history = readHistory();
        try {
            for (int i = 0; i < history.length(); i++) {
                JSONObject entry = history.getJSONObject(i);
                HistoryEntry record = new HistoryEntry(entry);
                if (!search.isEmpty()
                        && !record.patientName.toLowerCase(Locale.ROOT).contains(search)) {
                    continue;
                }
                if (testType != null && !testType.equals("All")
                        && !record.testType.equals(testType)) {
                    continue;
                }
                LocalDate recordDate = Instant.parse(record.timestamp)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                if (fromDate != null && recordDate.isBefore(fromDate)) {
                    continue;
                }
                if (toDate != null && recordDate.isAfter(toDate)) {
                    continue;
                }
                results.add(record);
            }
        } catch (JSONException | DateTimeParseException exception) {
            throw new IOException("Health history contains an invalid record.", exception);
        }
        results.sort(Comparator.comparing((HistoryEntry record) -> record.timestamp).reversed());
        return results;
    }

    private static JSONArray readHistory() throws IOException {
        if (!Files.exists(HISTORY_FILE)) {
            return new JSONArray();
        }
        try {
            return new JSONArray(Files.readString(HISTORY_FILE, StandardCharsets.UTF_8));
        } catch (JSONException exception) {
            throw new IOException("Health history file is invalid; no data was changed.", exception);
        }
    }

    private static void writeHistory(JSONArray history) throws IOException {
        Path parent = HISTORY_FILE.getParent();
        Files.createDirectories(parent);
        Path temporaryFile = Files.createTempFile(parent, "health-history-", ".tmp");
        try {
            Files.writeString(
                    temporaryFile, history.toString(2), StandardCharsets.UTF_8
            );
            try {
                Files.move(
                        temporaryFile, HISTORY_FILE,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING
                );
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporaryFile, HISTORY_FILE, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    public static final class HistoryEntry {
        public final String id;
        public final String patientName;
        public final int age;
        public final String gender;
        public final String testType;
        public final JSONObject inputs;
        public final String result;
        public final String riskLevel;
        public final double riskScore;
        public final String timestamp;

        private HistoryEntry(JSONObject entry) {
            id = entry.getString("id");
            patientName = entry.getString("patient_name");
            age = entry.getInt("age");
            gender = entry.getString("gender");
            testType = entry.getString("test_type");
            inputs = entry.getJSONObject("inputs");
            result = entry.getString("result");
            riskLevel = entry.getString("risk_level");
            riskScore = entry.getDouble("risk_score");
            timestamp = entry.getString("timestamp");
        }
    }
}
