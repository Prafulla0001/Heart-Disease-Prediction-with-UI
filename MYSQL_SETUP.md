# Hosted MySQL setup

The app can keep assessments in its existing local JSON file or store opted-in
heart and BMI assessments in a hosted MySQL database. The selected storage is
exclusive; switching between `local` and `mysql` does not migrate or merge
existing records.

## 1. Create a database and restricted application user

Create a database in your MySQL hosting provider's control panel. Run
[`schema.sql`](./schema.sql) in that database. Create a dedicated application
user with only `SELECT` and `INSERT` access to `health_assessments`; do not use
the database administrator account in the application.

## 2. Configure the application

Copy the following settings into your ignored `.env` file and replace the
placeholders locally:

```dotenv
HEALTH_HISTORY_STORAGE=mysql
HEALTH_DB_URL=jdbc:mysql://YOUR_HOST:3306/YOUR_DATABASE?sslMode=VERIFY_IDENTITY&connectionTimeZone=UTC&connectTimeout=10000&socketTimeout=15000
HEALTH_DB_USER=YOUR_RESTRICTED_USER
HEALTH_DB_PASSWORD=YOUR_DATABASE_PASSWORD
```

The connection URL must use `sslMode=VERIFY_IDENTITY`. Keep the credentials in
`.env` or environment variables; never commit them or paste them into chat.
Set `HEALTH_HISTORY_STORAGE=local` to use `health_history.json` again. The
application does not silently fall back to local storage if MySQL is selected
but unavailable.

## 3. Build and launch

Build with Maven so the MySQL Connector/J driver is included in the packaged
application:

```powershell
mvn package
java -jar target\HealthPredictionSystem.jar
```

Only assessments that a user explicitly opts to save are sent to the configured
database. Health data is sensitive: use provider access controls, restricted
database credentials, encrypted backups, and hosting that meets your
organization's legal and compliance requirements.
