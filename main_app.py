import os
os.environ["KMP_DUPLICATE_LIB_OK"] = "TRUE"
os.environ["YOLO_AUTOINSTALL"] = "False"

from flask import Flask, request, jsonify
import onnxruntime as ort
import numpy as np
import cv2
from PIL import Image
import io
import faiss
import torchvision.transforms as transforms
from ultralytics import YOLO
from pyngrok import ngrok

# ========== INISIALISASI FLASK ==========
app = Flask(__name__)
public_url = None  # Disiapkan untuk endpoint index

@app.route('/')
def index():
    return f"Server is running! {'Public URL: ' + public_url if public_url else 'Local only'}"


# ========== LOAD MODEL ==========

print("[🔄] Loading YOLOv12n...")
yolo_model = YOLO("yolo_model/model/best_yolo.onnx", task="detect")
print("[✔] YOLOv8 loaded")

print("[🔄] Loading MobileNetV2...")
mobilenet_session = ort.InferenceSession("MobileNetV2/model_sampah.onnx")
mobilenet_classes = [
    "Anorganik-botol-plastik", "Anorganik-kaca", "Anorganik-masker",
    "Organik-daun-kering", "Organik-jeruk-busuk", "Organik-kulit-pisang"
]
print("[✔] MobileNetV2 loaded")

print("[🔄] Loading ResNet50 + FAISS index...")
resnet_session = ort.InferenceSession("image_similarity/model/resnet50_feature_extractor.onnx")
faiss_index = faiss.read_index("image_similarity/model/faiss.index")
train_labels = np.load("image_similarity/model/train_labels.npy", allow_pickle=True)
train_paths = np.load("image_similarity/model/train_paths.npy", allow_pickle=True)
print("[✔] ResNet50 + FAISS index loaded")

transform = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor()
])


# ========== ENDPOINT /analyze-image ==========
@app.route('/analyze-image', methods=['POST'])
def analyze_image():
    if 'image' not in request.files:
        return jsonify({'error': 'No image uploaded'}), 400

    file = request.files['image']
    img_bytes = file.read()
    pil_image = Image.open(io.BytesIO(img_bytes)).convert("RGB")
    image_resized = pil_image.resize((224, 224))

    # Preprocess MobileNetV2 (NHWC)
    img_mobilenet = np.array(image_resized).astype(np.float32) / 255.0
    tensor_mobilenet = np.expand_dims(img_mobilenet, axis=0)  # [1, 224, 224, 3]

    # Preprocess ResNet50 (NCHW)
    img_resnet = np.transpose(img_mobilenet, (2, 0, 1))
    tensor_resnet = np.expand_dims(img_resnet, axis=0)  # [1, 3, 224, 224]

    # Predict class
    outputs = mobilenet_session.run(None, {mobilenet_session.get_inputs()[0].name: tensor_mobilenet})
    prediction = np.argmax(outputs[0])
    confidence = float(np.max(outputs[0]))
    klasifikasi = {
        "predicted_class": mobilenet_classes[prediction],
        "confidence": round(confidence, 4)
    }

    # Feature extraction + similarity search
    feature_out = resnet_session.run(None, {"input": tensor_resnet})
    feature = feature_out[0].squeeze().flatten().reshape(1, -1).astype('float32')
    D, I = faiss_index.search(feature, 3)

    similar = []
    for rank in range(3):
        similar.append({
            "rank": rank + 1,
            "label": str(train_labels[I[0][rank]]),
            "path": str(train_paths[I[0][rank]]),
            "distance": float(D[0][rank])
        })

    return jsonify({
        "classification": klasifikasi,
        "similar_images": similar
    })


# ========== ENDPOINT /detect-object ==========
@app.route('/detect-object', methods=['POST'])
def detect_object():
    if 'image' not in request.files:
        return jsonify({'error': 'No image uploaded'}), 400

    file = request.files['image']
    img_bytes = file.read()
    opencv_image = cv2.imdecode(np.frombuffer(img_bytes, np.uint8), cv2.IMREAD_COLOR)

    results = yolo_model(opencv_image)
    detections = []

    for result in results:
        for box in result.boxes:
            x1, y1, x2, y2 = map(int, box.xyxy[0])
            cls = int(box.cls[0])
            conf = float(box.conf[0])
            detections.append({
                'class': yolo_model.names[cls],
                'confidence': conf,
                'bbox': [x1, y1, x2, y2]
            })

    return jsonify({'detections': detections})


# ========== RUNNING APP ==========
if __name__ == '__main__':
    ngrok.set_auth_token("2yJ1ohrbuhwbVQo329tIYHwBATN_3vnXY2VXEFSW1VzDULXtx")
    
    try:
        print("[🌐] Connecting to ngrok...")
        tunnel = ngrok.connect(5000, bind_tls=True)
        public_url = tunnel.public_url
        print(" * ngrok URL:", public_url)
    except Exception as e:
        print("[❌] Ngrok failed:", e)
        print("[🔁] Running on localhost only...")

    print("[🚀] Starting Flask server...")
    app.run(debug=False, port=5000)
