import os
import joblib
import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
from sklearn.ensemble import RandomForestClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score, classification_report

# Create models directory
os.makedirs('models', exist_ok=True)

# Load dataset
df = pd.read_csv('heart.csv')
X = df.drop(columns='target', axis=1)
Y = df['target']

# Split data
X_train, X_test, Y_train, Y_test = train_test_split(X, Y, test_size=0.2, random_state=42, stratify=Y)

# Scale features
scaler = StandardScaler()
X_train_scaled = scaler.fit_transform(X_train)
X_test_scaled = scaler.transform(X_test)

# Train Logistic Regression & Random Forest
lr_model = LogisticRegression(max_iter=2000, random_state=42)
lr_model.fit(X_train_scaled, Y_train)
lr_acc = accuracy_score(Y_test, lr_model.predict(X_test_scaled))

rf_model = RandomForestClassifier(n_estimators=150, max_depth=8, random_state=42)
rf_model.fit(X_train_scaled, Y_train)
rf_acc = accuracy_score(Y_test, rf_model.predict(X_test_scaled))

print(f"Logistic Regression Test Accuracy: {lr_acc * 100:.2f}%")
print(f"Random Forest Test Accuracy: {rf_acc * 100:.2f}%")

# Select best model (Random Forest typically achieves ~98-100% on Cleveland/Kaggle split)
best_model = rf_model if rf_acc >= lr_acc else lr_model
print(f"Selected Best Model: {type(best_model).__name__}")

# Train best model on full dataset for maximum generalization
X_full_scaled = scaler.fit_transform(X)
best_model.fit(X_full_scaled, Y)

# Save artifacts
joblib.dump(best_model, 'models/heart_model.pkl')
joblib.dump(scaler, 'models/heart_scaler.pkl')

feature_names = list(X.columns)
joblib.dump(feature_names, 'models/heart_features.pkl')

print("Models and Scalers successfully serialized to 'models/' directory!")