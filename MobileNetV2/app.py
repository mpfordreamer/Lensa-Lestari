from flask import Flask, request, jsonify
import onnxruntime as ort
import numpy as np
from PIL import Image

app = Flask(__name__)

# Load model ONNX
session = ort.InferenceSession("model_sampah.onnx")

# Label kelas (urutan sesuai training)
class_names = [
    "Anorganik-botol-plastik",
    "Anorganik-kaca",
    "Anorganik-masker",
    "Organik-daun-kering",
    "Organik-jeruk-busuk",
    "Organik-kulit-pisang"
]

# Preprocessing gambar
def preprocess_image(img):
    img = img.resize((224, 224))
    img = np.array(img).astype(np.float32) / 255.0  # Normalisasi
    img = np.expand_dims(img, axis=0)               # Tambah batch dimensi
    return img

@app.route("/predict", methods=["POST"])
def predict():
    if 'file' not in request.files:
        return jsonify({'error': 'No image uploaded'}), 400

    file = request.files['file']
    image = Image.open(file).convert('RGB')
    input_data = preprocess_image(image)

    inputs = {session.get_inputs()[0].name: input_data}
    outputs = session.run(None, inputs)
    prediction = np.argmax(outputs[0])
    confidence = float(np.max(outputs[0]))

    return jsonify({
        'predicted_class': class_names[prediction],
        'confidence': round(confidence, 4)
    })

if __name__ == "__main__":
    app.run(debug=True)