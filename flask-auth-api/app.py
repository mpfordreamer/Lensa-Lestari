from flask import Flask, request, jsonify
from flask_cors import CORS
from flask_jwt_extended import JWTManager, create_access_token
from models import db, User
from config import Config
from dotenv import load_dotenv
from werkzeug.security import generate_password_hash

load_dotenv()

app = Flask(__name__)
app.config.from_object(Config)
CORS(app)
db.init_app(app)
jwt = JWTManager(app)

@app.route('/auth/register', methods=['POST'])
def register():
    data = request.form
    name = data.get('name')
    email = data.get('email')
    password = data.get('password')

    if User.query.filter_by(email=email).first():
        return jsonify({"message": "Email sudah digunakan"}), 400

    new_user = User(
        name=name,
        email=email,
        password=generate_password_hash(password)
    )
    db.session.add(new_user)
    db.session.commit()

    return jsonify({
        "token": create_access_token(identity=new_user.id),
        "user": new_user.to_dict()
    })

@app.route('/auth/login', methods=['POST'])
def login():
    data = request.form
    email = data.get('email')
    password = data.get('password')

    user = User.query.filter_by(email=email).first()
    if not user or not user.check_password(password):
        return jsonify({"message": "Email atau password salah"}), 401

    return jsonify({
        "token": create_access_token(identity=user.id),
        "user": user.to_dict()
    })

if __name__ == '__main__':
    with app.app_context():
        db.create_all()
    app.run(debug=True, host='0.0.0.0', port=5001)