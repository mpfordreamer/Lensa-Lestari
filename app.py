import os
from dotenv import load_dotenv
from flask import Flask, jsonify
from flask_cors import CORS
from flask_jwt_extended import JWTManager
from flask_migrate import Migrate


from config import Config
from models import db
from seed import seed_reward_actions
from controllers.auth_controller import auth_bp
from controllers.reward_controller import reward_bp
from controllers.model_controller import model_bp
from pyngrok import ngrok

# Load .env
load_dotenv()

# Inisialisasi Flask
app = Flask(__name__)
app.config.from_object(Config)
CORS(app)
JWTManager(app)
db.init_app(app)

# Migrate
migrate = Migrate(app,db)

# Register semua blueprint
app.register_blueprint(auth_bp)
app.register_blueprint(reward_bp)
app.register_blueprint(model_bp)

# Public URL ngrok (default = localhost)
public_url = None

@app.route('/')
def index():
    return jsonify({
        "message": "API gabungan aktif",
        "public_url": public_url or "http://localhost:5000"
    })

if __name__ == '__main__':
    with app.app_context():
        db.create_all()
        seed_reward_actions(db)

    ngrok.set_auth_token(os.getenv("NGROK_AUTH_TOKEN"))
    
    try:
        tunnel = ngrok.connect(5000, bind_tls=True)
        public_url = tunnel.public_url
        print(" * ngrok URL:", public_url)
    except Exception as e:
        print("[❌] Ngrok failed:", e)
        print("[🔁] Running on localhost only...")

    print("[🚀] Starting Flask server...")
    app.run(debug=False, port=5000)
