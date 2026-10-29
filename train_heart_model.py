from pathlib import Path

import joblib
import numpy as np
import pandas as pd
from sklearn.ensemble import GradientBoostingClassifier, RandomForestClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score
from sklearn.model_selection import StratifiedKFold, cross_val_score, train_test_split
from sklearn.pipeline import make_pipeline
from sklearn.preprocessing import StandardScaler
from sklearn.svm import SVC


ROOT = Path(__file__).resolve().parent
DATA_PATH = ROOT / "heart.csv"
MODEL_DIR = ROOT / "models"
FEATURE_COLUMNS_PATH = MODEL_DIR / "heart_features.pkl"
EXPECTED_COLUMNS = [
    "age", "sex", "cp", "trestbps", "chol", "fbs", "restecg",
    "thalach", "exang", "oldpeak", "slope", "ca", "thal",
]


def train() -> None:
    if not DATA_PATH.is_file():
        raise FileNotFoundError(f"Training dataset not found: {DATA_PATH}")

    data = pd.read_csv(DATA_PATH)
    if "target" not in data.columns:
        raise ValueError("heart.csv must contain a 'target' column.")

    features = data.drop(columns="target")
    target = data["target"]
    if features.columns.tolist() != EXPECTED_COLUMNS:
        raise ValueError("heart.csv feature columns are missing or in an unexpected order.")
    if features.empty or target.isna().any():
        raise ValueError("Training data must contain features and no missing target values.")
    try:
        feature_values = features.to_numpy(dtype=float)
    except (TypeError, ValueError) as error:
        raise ValueError("All heart.csv features must be numeric.") from error
    if not np.isfinite(feature_values).all():
        raise ValueError("Training features must contain only finite numeric values.")
    if target.nunique() != 2 or set(target.unique()) != {0, 1}:
        raise ValueError("The heart dataset target must contain the binary labels 0 and 1.")
    if len(data) < 10 or target.value_counts().min() < 5:
        raise ValueError("At least five examples of each target class are required.")

    x_train, x_test, y_train, y_test = train_test_split(
        features, target, test_size=0.2, random_state=42, stratify=target
    )
    candidates = {
        "LogisticRegression": LogisticRegression(max_iter=2000, random_state=42),
        "RandomForest": RandomForestClassifier(
            n_estimators=150, max_depth=8, random_state=42
        ),
        "SVC": SVC(probability=True, random_state=42),
        "GradientBoosting": GradientBoostingClassifier(random_state=42),
    }

    folds = min(5, int(y_train.value_counts().min()))
    cross_validator = StratifiedKFold(n_splits=folds, shuffle=True, random_state=42)
    best_name = ""
    best_model = None
    best_score = -1.0
    for name, classifier in candidates.items():
        pipeline = make_pipeline(StandardScaler(), classifier)
        scores = cross_val_score(
            pipeline, x_train, y_train, cv=cross_validator, scoring="accuracy"
        )
        mean_score = float(scores.mean())
        print(f"{name} cross-validation accuracy: {mean_score * 100:.2f}%")
        if mean_score > best_score:
            best_name = name
            best_model = classifier
            best_score = mean_score

    if best_model is None:
        raise RuntimeError("No candidate model could be trained.")

    test_pipeline = make_pipeline(StandardScaler(), best_model)
    test_pipeline.fit(x_train, y_train)
    test_accuracy = accuracy_score(y_test, test_pipeline.predict(x_test))
    print(f"Selected model: {best_name}")
    print(f"Held-out test accuracy: {test_accuracy * 100:.2f}%")

    scaler = StandardScaler()
    full_features_scaled = scaler.fit_transform(features)
    best_model.fit(full_features_scaled, target)

    MODEL_DIR.mkdir(parents=True, exist_ok=True)
    joblib.dump(best_model, MODEL_DIR / "heart_model.pkl")
    joblib.dump(scaler, MODEL_DIR / "heart_scaler.pkl")
    joblib.dump(list(features.columns), FEATURE_COLUMNS_PATH)
    print(f"Model, scaler, and feature list saved to {MODEL_DIR}.")


if __name__ == "__main__":
    train()
