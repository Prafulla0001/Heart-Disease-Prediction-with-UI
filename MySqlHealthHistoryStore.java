import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

final class MySqlHealthHistoryStore {
    private static final String INSERT_SQL = """
            INSERT INTO health_assessments (
                id, patient_name, age, gender, test_type, inputs_json,
                result, risk_level, risk_score, score_unit, timestamp_utc
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private MySqlHealthHistoryStore() {
    }

    static void save(JSONObject entry) throws IOException {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setQueryTimeout(15);
            statement.setString(1, entry.getString("id"));
            statement.setString(2, entry.getString("patient_name"));
            statement.setInt(3, entry.getInt("age"));
            statement.setString(4, entry.getString("gender"));
            statement.setString(5, entry.getString("test_type"));
            statement.setString(6, entry.getJSONObject("inputs").toString());
            statement.setString(7, entry.getString("result"));
            statement.setString(8, entry.getString("risk_level"));
            statement.setDouble(9, entry.getDouble("risk_score"));
            statement.setString(10, entry.getString("score_unit"));
            statement.setObject(11, LocalDateTime.ofInstant(
                    java.time.Instant.parse(entry.getString("timestamp")), ZoneOffset.UTC
            ));
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw databaseError("save the assessment", exception);
        }
    }

    static List<HealthHistoryStore.HistoryEntry> find(
            String searchText,
            String testType,
            LocalDate fromDate,
            LocalDate toDate
    ) throws IOException {
        StringBuilder sql = new StringBuilder("""
                SELECT id, patient_name, age, gender, test_type, inputs_json,
                       result, risk_level, risk_score, score_unit, timestamp_utc
                FROM health_assessments
                WHERE 1 = 1
                """);
        List<Object> parameters = new ArrayList<>();
        String search = searchText == null ? "" : searchText.trim();
        if (!search.isEmpty()) {
            sql.append(" AND LOCATE(LOWER(?), LOWER(patient_name)) > 0");
            parameters.add(search);
        }
        if (testType != null && !testType.equals("All")) {
            sql.append(" AND test_type = ?");
            parameters.add(testType);
        }
        ZoneId zone = ZoneId.systemDefault();
        if (fromDate != null) {
            sql.append(" AND timestamp_utc >= ?");
            parameters.add(fromDate.atStartOfDay(zone).withZoneSameInstant(ZoneOffset.UTC)
                    .toLocalDateTime());
        }
        if (toDate != null) {
            sql.append(" AND timestamp_utc < ?");
            parameters.add(toDate.plusDays(1).atStartOfDay(zone)
                    .withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
        }
        sql.append(" ORDER BY timestamp_utc DESC");

        List<HealthHistoryStore.HistoryEntry> results = new ArrayList<>();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            statement.setQueryTimeout(15);
            for (int i = 0; i < parameters.size(); i++) {
                Object parameter = parameters.get(i);
                if (parameter instanceof LocalDateTime dateTime) {
                    statement.setObject(i + 1, dateTime);
                } else {
                    statement.setString(i + 1, (String) parameter);
                }
            }
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    JSONObject entry = new JSONObject()
                            .put("id", rows.getString("id"))
                            .put("patient_name", rows.getString("patient_name"))
                            .put("age", rows.getInt("age"))
                            .put("gender", rows.getString("gender"))
                            .put("test_type", rows.getString("test_type"))
                            .put("inputs", new JSONObject(rows.getString("inputs_json")))
                            .put("result", rows.getString("result"))
                            .put("risk_level", rows.getString("risk_level"))
                            .put("risk_score", rows.getDouble("risk_score"))
                            .put("score_unit", rows.getString("score_unit"))
                            .put("timestamp", rows.getObject(
                                    "timestamp_utc", LocalDateTime.class
                            ).toInstant(ZoneOffset.UTC).toString());
                    results.add(new HealthHistoryStore.HistoryEntry(entry));
                }
            }
        } catch (SQLException | JSONException exception) {
            if (exception instanceof SQLException sqlException) {
                throw databaseError("read assessment history", sqlException);
            }
            throw new IOException("Hosted MySQL returned an invalid health-history record.", exception);
        }
        return results;
    }

    static void validateConnectionUrl(String url) throws IOException {
        if (url == null || !url.startsWith("jdbc:mysql://")) {
            throw new IOException("HEALTH_DB_URL must be a MySQL JDBC URL.");
        }
        try {
            URI uri = URI.create(url.substring("jdbc:".length()));
            if (!"mysql".equalsIgnoreCase(uri.getScheme())
                    || uri.getHost() == null
                    || uri.getUserInfo() != null
                    || !hasVerifiedTls(url)) {
                throw new IOException(
                        "HEALTH_DB_URL must use a MySQL host, separate credentials, "
                                + "and sslMode=VERIFY_IDENTITY."
                );
            }
        } catch (IllegalArgumentException exception) {
            throw new IOException("HEALTH_DB_URL is not a valid MySQL JDBC URL.", exception);
        }
    }

    private static boolean hasVerifiedTls(String url) {
        int queryStart = url.indexOf('?');
        if (queryStart < 0) {
            return false;
        }
        for (String parameter : url.substring(queryStart + 1).split("&")) {
            String[] parts = parameter.split("=", 2);
            if (parts.length == 2 && parts[0].equalsIgnoreCase("sslMode")
                    && parts[1].equalsIgnoreCase("VERIFY_IDENTITY")) {
                return true;
            }
        }
        return false;
    }

    private static Connection openConnection() throws IOException {
        String url = ConfigLoader.get("HEALTH_DB_URL");
        String username = ConfigLoader.get("HEALTH_DB_USER");
        String password = ConfigLoader.get("HEALTH_DB_PASSWORD");
        if (url == null || url.isBlank()
                || username == null || username.isBlank()
                || password == null || password.isBlank()) {
            throw new IOException(
                    "Hosted MySQL is selected but HEALTH_DB_URL, HEALTH_DB_USER, "
                            + "or HEALTH_DB_PASSWORD is missing. Configure them in .env."
            );
        }
        validateConnectionUrl(url);
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException exception) {
            throw databaseError("connect to hosted MySQL", exception);
        }
    }

    private static IOException databaseError(String action, SQLException exception) {
        String state = exception.getSQLState();
        return new IOException(
                "Could not " + action + " in hosted MySQL"
                        + (state == null ? "." : " (SQL state " + state + ").")
                        + " Check the connection settings, permissions, and MYSQL_SETUP.md.",
                exception
        );
    }
}
