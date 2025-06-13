from flask import Blueprint, request, jsonify, send_from_directory
from flask_jwt_extended import jwt_required, get_jwt_identity
from models import db, User, UserReward, ScanHistory
from datetime import datetime, timezone, timedelta

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

yolo_classes = ["Organik", "Anorganik"]

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
        "path": request.host_url.rstrip('/') + '/similarity-images/' + str(train_paths[I[0][rank]]),
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

# ====== Helper Function for YOLO ======
def preprocess_yolo(image):
    img = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)  # Convert ke RGB
    img = cv2.resize(img, (640, 640))
    img = img.astype(np.float32) / 255.0
    img = np.transpose(img, (2, 0, 1))  # CHW
    img = np.expand_dims(img, axis=0)  # Add batch dim
    return img

def apply_nms(boxes, iou_threshold=0.4):
    if not boxes:
        return []
    confidences = [float(box['confidence']) for box in boxes]
    bboxes = [box['bbox'] for box in boxes]
    bboxes_xywh = [[x1, y1, x2-x1, y2-y1] for x1, y1, x2, y2 in bboxes]
    indices = cv2.dnn.NMSBoxes(bboxes_xywh, confidences, score_threshold=0.1, nms_threshold=iou_threshold)
    if isinstance(indices, np.ndarray):
        indices = indices.flatten().tolist()
    elif hasattr(indices, "__iter__"):
        indices = [int(i[0]) if isinstance(i, (list, tuple, np.ndarray)) else int(i) for i in indices]
    else:
        indices = []
    return [boxes[i] for i in indices]

def postprocess_yolo(output, original_shape, conf_threshold=0.5, iou_threshold=0.4):
    preds = output[0]
    preds = np.transpose(preds[0], (1, 0))  # (8400, 6)
    boxes = []
    for det in preds:
        x1, y1, x2, y2 = det[:4]
        obj_conf = det[4]
        class_conf = det[5]
        score = obj_conf * class_conf
        if score < conf_threshold:
            continue
        x1, y1, x2, y2 = map(int, (x1, y1, x2, y2))
        class_id = int(class_conf > 0.5)
        boxes.append({
            "class": yolo_classes[class_id],
            "confidence": float(round(score, 4)),
            "bbox": [x1, y1, x2, y2]
        })
    boxes = apply_nms(boxes, iou_threshold=iou_threshold)
    return boxes

# ====== DETECT IMAGE ======
@model_bp.route('/detect-object', methods=['POST'])
@jwt_required()
def detect_object_mobile():
    user_id = get_jwt_identity()
    file = request.files.get('image')

    if not file or file.filename == '':
        return jsonify({'error': 'No image uploaded'}), 400
    if not file.content_type.startswith('image/'):
        return jsonify({'error': 'File must be an image'}), 400

    try:
        image_bytes = file.read()
        nparr = np.frombuffer(image_bytes, np.uint8)
        image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        if image is None:
            return jsonify({'error': 'Invalid image format'}), 400

        input_tensor = preprocess_yolo(image).astype(np.float32)
        input_name = yolo_session.get_inputs()[0].name
        output = yolo_session.run(None, {input_name: input_tensor})

        detections = postprocess_yolo(output, image.shape[:2])

        if not detections:
            return jsonify({
                "success": True,
                "detections": [],
                "message": "No object detected"
            })

        # Draw detection boxes
        image_with_boxes = image.copy()
        for det in detections:
            x1, y1, x2, y2 = det["bbox"]
            label = f"{det['class']} {det['confidence']:.2f}"
            text_y = y1 - 10 if y1 - 10 > 10 else y1 + 20
            (tw, th), _ = cv2.getTextSize(label, cv2.FONT_HERSHEY_SIMPLEX, 0.6, 2)
            cv2.rectangle(image_with_boxes, (x1, text_y - th), (x1 + tw, text_y), (0, 255, 0), -1)
            cv2.putText(image_with_boxes, label, (x1, text_y), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 0, 0), 2)
            cv2.rectangle(image_with_boxes, (x1, y1), (x2, y2), (0, 255, 0), 2)

        _, buffer = cv2.imencode('.jpg', image_with_boxes, [int(cv2.IMWRITE_JPEG_QUALITY), 60])
        image_with_box_base64 = base64.b64encode(buffer).decode('utf-8')

        top = max(detections, key=lambda x: x['confidence'])
        image_base64 = base64.b64encode(image_bytes).decode('utf-8')

        # Save to DB
        new_history = ScanHistory(
            user_id=user_id,
            image_base64=image_base64,
            predicted_class=top['class'],
            confidence=float(top['confidence'])
        )
        db.session.add(new_history)
        db.session.commit()

        # Cek scan terakhir (anti-spam)
        five_seconds_ago = datetime.now(timezone.utc) - timedelta(seconds=5)
        recent_scan = ScanHistory.query.filter_by(user_id=user_id)\
            .filter(ScanHistory.created_at >= five_seconds_ago)\
            .order_by(ScanHistory.created_at.desc()).first()
        if recent_scan:
            reward_message = "(⏳) Scan terlalu cepat, tunggu beberapa detik."
        else:
            from controllers.reward_controller import apply_reward
            reward_message = apply_reward(user_id, 1)

        user = User.query.get(user_id)
        return jsonify({
            "success": True,
            "detections": detections,
            "image_with_boxes_base64": image_with_box_base64,
            "top_class": top['class'],
            "top_confidence": float(round(top['confidence'], 4)),
            "reward_message": reward_message,
            "total_points": user.total_points if user else 0
        })
    except Exception as e:
        print(f"[ERROR] detect_object_mobile: {e}")
        return jsonify({'error': 'Server error occurred'}), 500

# ====== GET SCAN HISTORY ======
@model_bp.route('/scan-history', methods=['GET'])
@jwt_required()
def get_scan_history():
    user_id = get_jwt_identity()
    page = int(request.args.get('page', 1))
    per_page = int(request.args.get('per_page', 10))

    query = ScanHistory.query.filter_by(user_id=user_id).order_by(ScanHistory.created_at.desc())
    pagination = query.paginate(page=page, per_page=per_page, error_out=False)

    return jsonify({
        "total": pagination.total,
        "pages": pagination.pages,
        "current_page": pagination.page,
        "per_page": pagination.per_page,
        "data": [
            {
                "id": h.id,
                "predicted_class": h.predicted_class,
                "confidence": h.confidence,
                "image_base64": h.image_base64,
                "created_at": h.created_at.strftime('%Y-%m-%d %H:%M:%S')
            } for h in pagination.items
        ]
    })

# ====== GET SIMILARITY IMAGE ======
@model_bp.route('/similarity-images/<path:filename>')
def serve_similarity_image(filename):
    return send_from_directory('uploaded_images/similarity', filename)

# ====== GET REWARD HISTORY ======
@model_bp.route('/reward-history', methods=['GET'])
@jwt_required()
def get_reward_history():
    user_id = get_jwt_identity()

    page = int(request.args.get('page', 1))
    per_page = int(request.args.get('per_page', 10))

    query = UserReward.query.filter_by(user_id=user_id).order_by(UserReward.timestamp.desc())
    pagination = query.paginate(page=page, per_page=per_page, error_out=False)

    return jsonify({
        "total": pagination.total,
        "pages": pagination.pages,
        "current_page": pagination.page,
        "per_page": pagination.per_page,
        "data": [
            {
                "id": r.id,
                "action_name": r.reward_action.action_name,
                "points": r.reward_action.points,
                "description": r.reward_action.description,
                "timestamp": r.timestamp.strftime('%Y-%m-%d %H:%M:%S')
            } for r in pagination.items
        ]
    })

# ====== GET TRASH HISTORY ======
@model_bp.route('/trash-history', methods=['GET'])
@jwt_required()
def get_trash_history():
    user_id = get_jwt_identity()

    total = ScanHistory.query.filter_by(user_id=user_id).count()

    organik = ScanHistory.query.filter(
        ScanHistory.user_id == user_id,
        ScanHistory.predicted_class.ilike('Organik%')
    ).count()

    anorganik = ScanHistory.query.filter(
        ScanHistory.user_id == user_id,
        ScanHistory.predicted_class.ilike('Anorganik%')
    ).count()

    return jsonify({
        "total": total,
        "organik": organik,
        "anorganik": anorganik
    })