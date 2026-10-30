CREATE TABLE IF NOT EXISTS health_assessments (
    id CHAR(36) NOT NULL PRIMARY KEY,
    patient_name VARCHAR(120) NOT NULL,
    age SMALLINT UNSIGNED NOT NULL,
    gender VARCHAR(32) NOT NULL,
    test_type VARCHAR(32) NOT NULL,
    inputs_json JSON NOT NULL,
    result VARCHAR(255) NOT NULL,
    risk_level VARCHAR(64) NOT NULL,
    risk_score DOUBLE NOT NULL,
    score_unit VARCHAR(16) NOT NULL,
    timestamp_utc DATETIME(6) NOT NULL,
    INDEX idx_health_assessments_timestamp (timestamp_utc),
    INDEX idx_health_assessments_test_type (test_type)
);
