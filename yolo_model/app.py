# === UNTUK TEST NYA DI test.py ===
from flask import Flask, request, jsonify
from ultralytics import YOLO
import cv2
import numpy as np
import os
import base64

app = Flask(__name__)

# Load YOLO model
model_path = os.path.join(os.path.dirname(__file__), "model", "best_yolo.onnx")
try:
    model = YOLO(model_path)
    print("✅ Model ONNX loaded successfully")
except Exception as e:
    raise RuntimeError(f"Failed to load model: {e}")

def process_frame(frame): # Proses frame from mobile camera
    """Process single frame for object detection""" 
    results = model(frame)
    detections = []
    
    for result in results:
        boxes = result.boxes
        for box in boxes:
            x1, y1, x2, y2 = map(int, box.xyxy[0])
            cls = int(box.cls[0])
            conf = float(box.conf[0])
            
            detections.append({
                'class': model.names[cls],
                'confidence': float(conf),
                'bbox': [x1, y1, x2, y2]
            })
    
    return detections

@app.route('/api/detect_frame', methods=['POST']) # API endpoint for realtime frame detection
def detect_frame():
    """API endpoint for realtime frame detection"""
    try:
        # Expect base64 encoded frame from mobile camera
        if 'frame_base64' not in request.json:
            return jsonify({
                'success': False,
                'error': 'No frame provided'
            }), 400

        # Decode base64 frame
        base64_string = request.json['frame_base64']
        frame_bytes = base64.b64decode(base64_string)
        nparr = np.frombuffer(frame_bytes, np.uint8)
        frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        
        if frame is None:
            return jsonify({
                'success': False,
                'error': 'Invalid frame format'
            }), 400

        # Process detection
        detections = process_frame(frame)
        
        return jsonify({
            'success': True,
            'detections': detections
        })

    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

# Optional: Image upload endpoint
@app.route('/api/detect_image', methods=['POST'])
def detect_image():
    """Optional: API endpoint for image upload detection"""
    pass  # Implement if needed

if __name__ == '__main__':
    print("🚀 Starting Mobile Detection API - http://localhost:5000")
    app.run(host='0.0.0.0', port=5000)