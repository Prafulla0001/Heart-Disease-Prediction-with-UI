import json
import sys
from pathlib import Path

import numpy as np
import joblib


ROOT = Path(__file__).resolve().parent
MODEL_PATH = ROOT / "models" / "heart_model.pkl"
SCALER_PATH = ROOT / "models" / "heart_scaler.pkl"
FEATURES_PATH = ROOT / "models" / "heart_features.pkl"
EXPECTED_FEATURES = 13
EXPECTED_FEATURE_NAMES = [
    "age", "sex", "cp", "trestbps", "chol", "fbs", "restecg",
    "thalach", "exang", "oldpeak", "slope", "ca", "thal",
]

def get_risk_factors(data):
    """Analyze individual features against standard clinical thresholds"""
    # Feature indices: 0:age, 1:sex, 2:cp, 3:trestbps, 4:chol, 5:fbs, 6:restecg, 7:thalach, 8:exang, 9:oldpeak, 10:slope, 11:ca, 12:thal
    factors = []
    age = data[0]
    trestbps = data[3]
    chol = data[4]
    fbs = data[5]
    restecg = data[6]
    thalach = data[7]
    exang = data[8]
    oldpeak = data[9]
    ca = data[11]
    thal = data[12]

    if trestbps >= 140:
        factors.append(f"Stage 2 Hypertension (Resting BP: {int(trestbps)} mm Hg)")
    elif trestbps >= 130:
        factors.append(f"Stage 1 Hypertension (Resting BP: {int(trestbps)} mm Hg)")

    if chol >= 240:
        factors.append(f"High Serum Cholesterol ({int(chol)} mg/dL - Target < 200)")
    elif chol >= 200:
        factors.append(f"Borderline Elevated Cholesterol ({int(chol)} mg/dL)")

    if oldpeak >= 2.0:
        factors.append(f"Significant ST Depression ({oldpeak:.1f} - Indication of ischemia)")
    elif oldpeak >= 1.0:
        factors.append(f"Mild ST Depression ({oldpeak:.1f})")

    if exang == 1.0:
        factors.append("Exercise Induced Angina (Cardiac strain during physical exertion)")

    if fbs == 1.0:
        factors.append("Elevated Fasting Blood Sugar (> 120 mg/dL)")

    if ca >= 1.0:
        factors.append(f"Major Fluoroscopy Vessel Blockages ({int(ca)} vessels)")

    if thal in [2.0, 3.0]:
        factors.append("Abnormal Thallium Stress Test (Perfusion Defect)")

    if thalach < 120 and age < 65:
        factors.append(f"Suboptimal Max Heart Rate ({int(thalach)} bpm)")

    return factors

def predict(input_values):
    values = np.asarray(input_values, dtype=float)
    if values.shape != (EXPECTED_FEATURES,) or not np.isfinite(values).all():
        raise ValueError(f"Expected {EXPECTED_FEATURES} finite numeric feature values.")
    if not MODEL_PATH.is_file() or not SCALER_PATH.is_file() or not FEATURES_PATH.is_file():
        raise FileNotFoundError(
            "Pre-trained heart model is missing. Run train_heart_model.py first."
        )

    model = joblib.load(MODEL_PATH)
    scaler = joblib.load(SCALER_PATH)
    feature_names = joblib.load(FEATURES_PATH)
    if feature_names != EXPECTED_FEATURE_NAMES:
        raise ValueError("Saved model feature names or order do not match inference inputs.")

    scaled_input = scaler.transform(values.reshape(1, -1))
    prediction = int(model.predict(scaled_input)[0])
    if not hasattr(model, "predict_proba"):
        raise TypeError("The saved classifier does not provide calibrated probabilities.")

    probabilities = model.predict_proba(scaled_input)[0]
    classes = list(model.classes_)
    if 1 not in classes:
        raise ValueError("The saved classifier is missing target class 1.")
    risk_prob = float(probabilities[classes.index(1)]) * 100.0
    risk_level = (
        "High Risk" if risk_prob >= 70.0
        else "Moderate Risk" if risk_prob >= 35.0
        else "Low Risk"
    )
    diagnosis = "Defective Heart" if prediction == 1 else "Healthy Heart"
    risk_factors = get_risk_factors(values.tolist())

    return {
        "prediction": diagnosis,
        "risk_percentage": round(risk_prob, 1),
        "risk_score": round(risk_prob, 1),
        "risk_level": risk_level,
        "key_risk_factors": risk_factors,
        "risk_factors": risk_factors,
    }

if __name__ == "__main__":
    try:
        if len(sys.argv) != EXPECTED_FEATURES + 1:
            raise ValueError(f"Provide exactly {EXPECTED_FEATURES} numeric feature values.")
        values = [float(value) for value in sys.argv[1:]]
        print(json.dumps(predict(values)))
    except Exception as error:
        print(json.dumps({"error": str(error)}))
        sys.exit(1)