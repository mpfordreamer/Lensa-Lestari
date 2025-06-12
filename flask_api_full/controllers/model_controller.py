from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from models import db, User, RewardAction, UserReward, ScanHistory
from datetime import datetime

import numpy as np
import cv2
import io
import onnxruntime as ort
import faiss
import base64
from PIL import Image

model_bp = Blueprint('model', __name__)

# ====== Load Models ======
yolo_session = ort.InferenceSession("model_files/yolo_model/best_yolo.onnx", providers=["CPUExecutionProvider"])
mobilenet_session = ort.InferenceSession("model_files/MobileNetV2/model_sampah.onnx", providers=["CPUExecutionProvider"])
resnet_session = ort.InferenceSession("model_files/image_similarity/resnet50_feature_extractor.onnx")
faiss_index = faiss.read_index("model_files/image_similarity/faiss.index")
train_labels = np.load("model_files/image_similarity/train_labels.npy", allow_pickle=True)
train_paths = np.load("model_files/image_similarity/train_paths.npy", allow_pickle=True)

mobilenet_classes = [
    "Anorganik-botol-plastik", "Anorganik-kaca", "Anorganik-masker",
    "Organik-daun-kering", "Organik-jeruk-busuk", "Organik-kulit-pisang"
]

# ====== Helper Function for YOLO ======
def preprocess_yolo(image, input_shape=(640, 640)):
    image_resized = cv2.resize(image, input_shape)
    image_rgb = cv2.cvtColor(image_resized, cv2.COLOR_BGR2RGB)
    image_normalized = image_rgb.astype(np.float32) / 255.0
    image_transposed = np.transpose(image_normalized, (2, 0, 1))
    image_input = np.expand_dims(image_transposed, axis=0)
    return image_input

def postprocess_yolo(output, conf_thres=0.25):
    predictions = output[0]  # [1, N, 85]
    boxes = []
    for pred in predictions[0]:
        conf = pred[4]
        if conf > conf_thres:
            class_scores = pred[5:]
            cls = int(np.argmax(class_scores))
            score = class_scores[cls] * conf
            x_center, y_center, w, h = pred[0:4]
            x1 = int(x_center - w / 2)
            y1 = int(y_center - h / 2)
            x2 = int(x_center + w / 2)
            y2 = int(y_center + h / 2)
            boxes.append({
                "class_id": cls,
                "confidence": float(score),
                "bbox": [x1, y1, x2, y2]
            })
    return boxes

# ====== ANALYZE IMAGE ======
@model_bp.route('/analyze-image', methods=['POST'])
@jwt_required()
def analyze_image():
    user_id = get_jwt_identity()

    if 'image' not in request.files:
        return jsonify({'error': 'No image uploaded'}), 400

    file = request.files['image']
    img_bytes = file.read()

    # === Mobilenet Predict ===
    pil_image = Image.open(io.BytesIO(img_bytes)).convert("RGB")
    image_resized = pil_image.resize((224, 224))
    img_mobilenet = np.array(image_resized).astype(np.float32) / 255.0
    tensor_mobilenet = np.expand_dims(img_mobilenet, axis=0)

    outputs = mobilenet_session.run(None, {mobilenet_session.get_inputs()[0].name: tensor_mobilenet})
    prediction = np.argmax(outputs[0])
    confidence = float(np.max(outputs[0]))

    # === ResNet Feature Extraction ===
    img_resnet = np.transpose(img_mobilenet, (2, 0, 1))
    tensor_resnet = np.expand_dims(img_resnet, axis=0)
    feature_out = resnet_session.run(None, {"input": tensor_resnet})
    feature = feature_out[0].squeeze().flatten().reshape(1, -1).astype('float32')

    D, I = faiss_index.search(feature, 3)
    similar = [{
        "rank": rank + 1,
        "label": str(train_labels[I[0][rank]]),
        "path": str(train_paths[I[0][rank]]),
        "distance": float(D[0][rank])
    } for rank in range(3)]

    # === Simpan ke database sebagai riwayat ===
    encoded_image = base64.b64encode(img_bytes).decode('utf-8')
    new_history = ScanHistory(
        user_id=user_id,
        image_base64=encoded_image,
        predicted_class=mobilenet_classes[prediction],
        confidence=confidence
    )
    db.session.add(new_history)
    db.session.commit()

    # === Tambahkan reward ===
    from controllers.reward_controller import apply_reward
    reward_message = apply_reward(user_id, 1)
    user = User.query.get(user_id)

    return jsonify({
        "classification": {
            "predicted_class": mobilenet_classes[prediction],
            "confidence": round(confidence, 4)
        },
        "similar_images": similar,
        "reward_message": reward_message,
        "total_points": user.total_points
    })

# ====== DETECT OBJECT (UPLOAD GAMBAR) ======
@model_bp.route('/detect-object', methods=['POST'])
@jwt_required()
def detect_object():
    user_id = get_jwt_identity()
    if 'image' not in request.files:
        return jsonify({'error': 'No image uploaded'}), 400

    file = request.files['image']
    img_bytes = file.read()
    image = cv2.imdecode(np.frombuffer(img_bytes, np.uint8), cv2.IMREAD_COLOR)

    input_data = preprocess_yolo(image)
    input_name = yolo_session.get_inputs()[0].name
    outputs = yolo_session.run(None, {input_name: input_data})
    detections = postprocess_yolo(outputs)

    from controllers.reward_controller import apply_reward
    reward_message = apply_reward(user_id, 1)
    user = User.query.get(user_id)

    return jsonify({
        'detections': detections,
        "reward_message": reward_message,
        "total_points": user.total_points
    })

# ====== DETECT FRAME (REALTIME WEBCAM) ======
@model_bp.route('/detect-frame', methods=['POST'])
@jwt_required()
def detect_frame():
    user_id = get_jwt_identity()

    try:
        if not request.is_json or 'frame_base64' not in request.json:
            return jsonify({'error': 'No frame provided'}), 400

        base64_string = request.json['frame_base64']
        frame_bytes = base64.b64decode(base64_string)
        nparr = np.frombuffer(frame_bytes, np.uint8)
        frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

        if frame is None:
            return jsonify({'error': 'Invalid frame format'}), 400

        input_data = preprocess_yolo(frame)
        input_name = yolo_session.get_inputs()[0].name
        outputs = yolo_session.run(None, {input_name: input_data})
        detections = postprocess_yolo(outputs)

        # Simpan riwayat frame
        new_history = ScanHistory(
            user_id=user_id,
            image_base64=base64_string,
            predicted_class="Detected (YOLO)",
            confidence=1.0
        )
        db.session.add(new_history)
        db.session.commit()

        return jsonify({
            'success': True,
            'detections': detections,
            'message': 'Frame saved to history'
        })

    except Exception as e:
        return jsonify({'success': False, 'error': str(e)}), 500

# ====== GET SCAN HISTORY ======
@model_bp.route('/scan-history', methods=['GET'])
@jwt_required()
def get_scan_history():
    user_id = get_jwt_identity()
    history = ScanHistory.query.filter_by(user_id=user_id).order_by(ScanHistory.created_at.desc()).all()
    return jsonify([
        {
            "id": h.id,
            "predicted_class": h.predicted_class,
            "confidence": h.confidence,
            "image_base64": h.image_base64,
            "created_at": h.created_at.strftime('%Y-%m-%d %H:%M:%S')
        } for h in history
    ])
