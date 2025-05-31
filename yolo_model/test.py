from flask import Flask, request, jsonify, Response
from ultralytics import YOLO
import cv2
import numpy as np
import os

app = Flask(__name__)

# Load model
model_path = os.path.join(os.path.dirname(__file__), "model", "best_yolo.onnx")
try:
    model = YOLO(model_path)
    print("✅ Model ONNX loaded successfully")
except Exception as e:
    raise RuntimeError(f"Failed to load model: {e}")

def gen_frames():
    """Generate video frames with detection"""
    cap = cv2.VideoCapture(0)
    if not cap.isOpened():
        print("❌ Error: Could not open camera")
        return
    
    try:
        while True:
            success, frame = cap.read()
            if not success:
                break
            
            # Run YOLO detection
            results = model(frame)
            
            # Draw boxes
            for result in results:
                boxes = result.boxes
                for box in boxes:
                    # Get box coordinates
                    x1, y1, x2, y2 = map(int, box.xyxy[0])
                    # Get class and confidence
                    cls = int(box.cls[0])
                    conf = float(box.conf[0])
                    
                    # Draw rectangle and label
                    cv2.rectangle(frame, (x1, y1), (x2, y2), (0, 255, 0), 2)
                    cv2.putText(frame, f'{model.names[cls]} {conf:.2f}',
                              (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX,
                              0.5, (0, 255, 0), 2)
            
            # Convert frame to bytes
            _, buffer = cv2.imencode('.jpg', frame)
            frame_bytes = buffer.tobytes()
            
            yield (b'--frame\r\n'
                   b'Content-Type: image/jpeg\r\n\r\n' + frame_bytes + b'\r\n')
    
    finally:
        cap.release()

def process_image(image): #FoR 
    """Process single image for object detection"""
    results = model(image)
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

@app.route('/')
def index():
    """Main page with options for video and image detection"""
    return '''
    <html>
    <head>
        <title>Waste Detection System</title>
        <style>
            body { font-family: Arial; margin: 20px; text-align: center; }
            img { max-width: 100%; height: auto; margin-top: 20px; }
            .container { margin: 20px auto; max-width: 800px; }
            .section { margin: 40px 0; padding: 20px; border: 1px solid #ddd; }
        </style>
    </head>
    <body>
        <h1>Waste Detection System</h1>
        
        <div class="container">
            <div class="section">
                <h2>Real-time Video Detection</h2>
                <a href="/video_detection">Launch Video Detection</a>
            </div>
            
            <div class="section">
                <h2>Image Upload Detection</h2>
                <form action="/detect_image" method="post" enctype="multipart/form-data">
                    <input type="file" name="image" accept="image/*">
                    <input type="submit" value="Detect Objects">
                </form>
            </div>
        </div>
    </body>
    </html>
    '''

@app.route('/video_detection')
def video_detection():
    """Video detection page"""
    return '''
    <html>
    <head>
        <title>Real-time Waste Detection</title>
        <style>
            body { font-family: Arial; margin: 20px; text-align: center; }
            img { max-width: 100%; height: auto; margin-top: 20px; }
        </style>
    </head>
    <body>
        <h1>Real-time Waste Detection</h1>
        <img src="/video_feed">
        <p><a href="/">Back to Home</a></p>
    </body>
    </html>
    '''

@app.route('/video_feed')
def video_feed():
    """Video streaming route"""
    return Response(gen_frames(),
                   mimetype='multipart/x-mixed-replace; boundary=frame')

@app.route('/detect_image', methods=['POST'])
def detect_image():
    """Handle image upload and detection"""
    if 'image' not in request.files:
        return jsonify({'error': 'No image uploaded'}), 400
    
    file = request.files['image']
    if file.filename == '':
        return jsonify({'error': 'No selected file'}), 400
    
    # Read and process image
    image_bytes = file.read()
    nparr = np.frombuffer(image_bytes, np.uint8)
    image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
    
    if image is None:
        return jsonify({'error': 'Invalid image format'}), 400
    
    # Get detections
    detections = process_image(image)
    
    return jsonify({
        'success': True,
        'detections': detections
    })

if __name__ == '__main__':
    print("🚀 Starting Waste Detection Server - visit http://localhost:5000")
    app.run(host='0.0.0.0', port=5000, debug=True)

