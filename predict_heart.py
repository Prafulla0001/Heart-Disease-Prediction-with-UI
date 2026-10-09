import sys
import os
import json
import warnings
import numpy as np
import pandas as pd
import joblib

warnings.filterwarnings('ignore')

MODEL_PATH = 'models/heart_model.pkl'
SCALER_PATH = 'models/heart_scaler.pkl'

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
    try:
        # Load serialized model & scaler if available
        if os.path.exists(MODEL_PATH) and os.path.exists(SCALER_PATH):
            model = joblib.load(MODEL_PATH)
            scaler = joblib.load(SCALER_PATH)
        else:
            # Fallback training on the fly
            df = pd.read_csv('heart.csv')
            X = df.drop(columns='target', axis=1)
            Y = df['target']
            from sklearn.ensemble import RandomForestClassifier
            from sklearn.preprocessing import StandardScaler
            scaler = StandardScaler()
            X_scaled = scaler.fit_transform(X)
            model = RandomForestClassifier(n_estimators=100, random_state=42)
            model.fit(X_scaled, Y)

        input_arr = np.asarray(input_values, dtype=float).reshape(1, -1)
        scaled_input = scaler.transform(input_arr)
        
        prediction = model.predict(scaled_input)[0]
        
        # Calculate risk probability
        if hasattr(model, "predict_proba"):
            probs = model.predict_proba(scaled_input)[0]
            # Probability of target == 1 (defective heart)
            risk_prob = probs[1] * 100.0
        else:
            risk_prob = 95.0 if prediction == 1 else 10.0

        risk_level = "High Risk" if risk_prob >= 70.0 else ("Moderate Risk" if risk_prob >= 35.0 else "Low Risk")
        diagnosis = "Defective Heart" if prediction == 1 else "Healthy Heart"
        risk_factors = get_risk_factors(input_values)

        result_dict = {
            "prediction": diagnosis,
            "risk_score": round(risk_prob, 1),
            "risk_level": risk_level,
            "risk_factors": risk_factors
        }

        print(json.dumps(result_dict))

    except Exception as e:
        error_dict = {
            "error": str(e),
            "prediction": "Error",
            "risk_score": 0.0,
            "risk_level": "Unknown",
            "risk_factors": []
        }
        print(json.dumps(error_dict))

if __name__ == "__main__":
    if len(sys.argv) > 1:
        args = list(map(float, sys.argv[1:]))
        predict(args)
    else:
        print(json.dumps({"error": "No input provided"}))