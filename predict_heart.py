import sys
import warnings
import numpy as np
import pandas as pd
from sklearn.linear_model import LogisticRegression
from sklearn.preprocessing import StandardScaler

# Suppress convergence and feature name warnings for clean stdout
warnings.filterwarnings('ignore')

# Load the dataset
heart_data = pd.read_csv('heart.csv')

# Split features and target
X = heart_data.drop(columns='target', axis=1)
Y = heart_data['target']

# Scale features for proper convergence
scaler = StandardScaler()
X_scaled = scaler.fit_transform(X)

# Train the model with scaled features
model = LogisticRegression(max_iter=2000, random_state=42)
model.fit(X_scaled, Y)

# Prediction function
def predict_heart_disease(data):
    input_data_as_numpy_array = np.asarray(data).reshape(1, -1)
    scaled_input = scaler.transform(input_data_as_numpy_array)
    prediction = model.predict(scaled_input)
    return 'Defective Heart' if prediction[0] == 1 else 'Healthy Heart'

if __name__ == "__main__":
    if len(sys.argv) > 1:
        input_data = list(map(float, sys.argv[1:]))
        print(predict_heart_disease(input_data))
    else:
        print("Usage: python predict_heart.py <13 features>")