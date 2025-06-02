import os
os.environ["KMP_DUPLICATE_LIB_OK"] = "TRUE"

from flask import Flask, request, jsonify
import numpy as np
import faiss
from PIL import Image
import onnxruntime as ort
import io
import torchvision.transforms as transforms
import os

# ========== CONFIG ==========
TOP_K = 3
FEATURE_DIM = 2048
ONNX_PATH = "model/resnet50_feature_extractor.onnx"
INDEX_PATH = "model/faiss.index"
LABELS_PATH = "model/train_labels.npy"
PATHS_PATH = "model/train_paths.npy"

# ========== LOAD COMPONENTS ==========
app = Flask(__name__)

# Load ONNX model
session = ort.InferenceSession(ONNX_PATH)

# Load FAISS index & metadata
index = faiss.read_index(INDEX_PATH)
train_labels = np.load(LABELS_PATH, allow_pickle=True)
train_paths = np.load(PATHS_PATH, allow_pickle=True)

# Transform image to tensor (same as training)
transform = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
])

# ========== INFERENCE ROUTE ==========
@app.route('/predict', methods=['POST'])
def predict():
    if 'image' not in request.files:
        return jsonify({"error": "No image uploaded"}), 400

    file = request.files['image']
    image = Image.open(io.BytesIO(file.read())).convert("RGB")
    tensor = transform(image).unsqueeze(0).numpy()

    # Run ONNX
    outputs = session.run(None, {"input": tensor})
    feature = outputs[0].squeeze().flatten().reshape(1, -1).astype('float32')

    # Query FAISS
    D, I = index.search(feature, TOP_K)
    results = []
    for rank in range(TOP_K):
        results.append({
            "rank": rank + 1,
            "label": str(train_labels[I[0][rank]]),
            "path": str(train_paths[I[0][rank]]),
            "distance": float(D[0][rank])
        })

    return jsonify({
        "top_k": TOP_K,
        "results": results
    })

# ========== RUN APP ==========
if __name__ == '__main__':
    app.run(debug=True, port=5000)
