# Heart Disease Prediction with UI

A Java-based health prediction and wellness application that combines a machine-learning heart disease risk model with both Swing and browser interfaces.

The application accepts clinical inputs, uses a scikit-learn model to estimate cardiac disease risk, identifies contributing risk factors, provides lifestyle recommendations, exports assessment reports, and optionally stores assessment history locally or in MySQL.

![Application preview](screen.jpg)

## Features

- 13-factor heart disease risk assessment
- Risk probability and low/moderate/high classification
- Healthy and high-risk input presets
- Personalized risk factors and action recommendations
- Desktop interface built with Java Swing and FlatLaf
- Browser interface served through a built-in Java HTTP server
- Health chatbot with an optional AI provider configuration
- BMI and blood pressure utilities
- Exportable text assessment reports
- Optional local JSON or hosted MySQL health history
- Lifestyle guidance for diet, exercise, sleep, stress, and hydration

## Technology Stack

| Area | Technology |
|---|---|
| Desktop UI | Java 17, Swing, FlatLaf |
| Web UI | HTML, CSS, JavaScript |
| Web server | Java `HttpServer` |
| Machine learning | Python, scikit-learn, pandas, NumPy, joblib |
| Build system | Maven |
| Database | Optional MySQL / MySQL Connector/J |
| Configuration | `.env`-style configuration |

## Project Structure

```text
.
├── MainMenuUI.java                 # Desktop application entry point
├── HeartDiseasePredictionUI.java   # Heart assessment interface
├── BMICalculator.java              # BMI calculator
├── BloodPressureUI.java            # Blood pressure interface
├── HealthHistoryUI.java            # Assessment history interface
├── HealthChatbot.java              # Desktop chatbot
├── HealthWebApp.java               # Embedded web server and API
├── LifestyleTipsUI.java            # Wellness guidance
├── PythonBridge.java               # Java-to-Python execution bridge
├── train_heart_model.py            # Model training and selection script
├── predict_heart.py                # Heart risk inference script
├── heart.csv                       # Training dataset
├── models/                         # Generated model, scaler, and feature files
├── web/                            # HTML, CSS, and JavaScript frontend
├── schema.sql                      # MySQL database schema
├── MYSQL_SETUP.md                  # Hosted MySQL setup guide
├── pom.xml                         # Maven configuration
└── requirements.txt                # Python dependencies
```

## Requirements

- Java 17 or newer
- Maven 3.8 or newer
- Python 3.10 or newer
- pip
- Windows is recommended for the included `.bat` launchers
- Optional: MySQL for hosted assessment history

## Installation

### 1. Clone the repository

```bash
git clone https://github.com/Prafulla0001/Heart-Disease-Prediction-with-UI.git
cd Heart-Disease-Prediction-with-UI
```

### 2. Create a Python environment

**Windows:**

```powershell
python -m venv .venv
.venv\Scripts\activate
python -m pip install --upgrade pip
pip install -r requirements.txt
```

**Linux/macOS:**

```bash
python3 -m venv .venv
source .venv/bin/activate
python -m pip install --upgrade pip
pip install -r requirements.txt
```

### 3. Train the model

The repository includes the training data. Run:

```bash
python train_heart_model.py
```

This evaluates several classifiers using cross-validation, selects the best candidate, and creates:

- `models/heart_model.pkl`
- `models/heart_scaler.pkl`
- `models/heart_features.pkl`

### 4. Build the Java application

```bash
mvn clean package
```

This creates `target/HealthPredictionSystem.jar`.

## Running the Desktop Application

### Windows

After building the project:

```powershell
.\"Run Health Prediction.bat"
```

Or run the JAR directly:

```powershell
java -jar target\HealthPredictionSystem.jar
```

### Linux/macOS

```bash
java -jar target/HealthPredictionSystem.jar
```

The desktop menu provides access to heart risk prediction, BMI calculation, blood pressure checking, health history, a chatbot, and lifestyle tips.

## Running the Web Interface

Start the Java web server:

```powershell
java -cp "target/HealthPredictionSystem.jar" HealthWebApp
```

On Windows, the server also attempts to open a browser automatically. Otherwise, visit:

<http://localhost:8080>

The web server exposes:

- `POST /api/predict/heart`
- `POST /api/chat`

## API Examples

### Heart prediction

Send a JSON array containing the 13 values in the following order:

```text
age, sex, cp, trestbps, chol, fbs, restecg,
thalach, exang, oldpeak, slope, ca, thal
```

```bash
curl -X POST http://localhost:8080/api/predict/heart \
  -H "Content-Type: application/json" \
  -d '[52,1,0,125,212,0,1,168,0,1.0,2,2,3]'
```

### Chat

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"How can I reduce cholesterol?"}'
```

## Configuration

Copy `.env.example` to `.env` for local configuration:

```dotenv
GROK_API_KEY=
GEMINI_API_KEY=
OMNIROUTE_API_KEY=
HEALTH_HISTORY_STORAGE=local
```

Supported chatbot providers include Grok, Gemini, and OmniRoute. Provider settings are optional; the application includes offline guidance and should not store real API keys in source control.

Health history defaults to local storage in `health_history.json`. To use MySQL, follow [`MYSQL_SETUP.md`](MYSQL_SETUP.md).

## MySQL Storage

To enable hosted MySQL storage:

1. Create a database.
2. Run [`schema.sql`](schema.sql).
3. Create a restricted database user.
4. Set `HEALTH_HISTORY_STORAGE=mysql`.
5. Configure `HEALTH_DB_URL`, `HEALTH_DB_USER`, and `HEALTH_DB_PASSWORD` in `.env` or environment variables.

MySQL mode is exclusive: it does not migrate or merge existing local history. Never use a database administrator account or commit database credentials.

## Input Reference

| Feature | Description |
|---|---|
| age | Age in years |
| sex | 1 = male, 0 = female |
| cp | Chest pain type: 0–3 |
| trestbps | Resting blood pressure in mm Hg |
| chol | Serum cholesterol in mg/dL |
| fbs | Fasting blood sugar above 120 mg/dL: 1 = yes, 0 = no |
| restecg | Resting ECG result: 0–2 |
| thalach | Maximum heart rate achieved |
| exang | Exercise-induced angina: 1 = yes, 0 = no |
| oldpeak | ST depression induced by exercise |
| slope | Peak exercise ST segment slope: 0–2 |
| ca | Number of major vessels colored: 0–4 |
| thal | Thalassemia result: 1 = normal, 2 = fixed defect, 3 = reversible defect |

## Medical Disclaimer

This application is a software demonstration for preliminary screening and educational purposes only. It is not a medical device and does not provide a confirmed diagnosis or replace advice from a qualified healthcare professional. Seek immediate medical assistance for severe chest pain, fainting, significant breathing difficulty, or other emergency symptoms.

## Contributing

Contributions are welcome.

1. Fork the repository.
2. Create a feature branch.
3. Make and test your changes.
4. Submit a pull request with a clear description.

## License

No license file is currently included. Copyright belongs to the repository owner unless a license is added.
