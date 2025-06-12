from flask_sqlalchemy import SQLAlchemy
from werkzeug.security import generate_password_hash, check_password_hash
from datetime import datetime

db = SQLAlchemy()

class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(100))
    email = db.Column(db.String(120), unique=True, nullable=False)
    password = db.Column(db.String(255), nullable=False)
    total_points = db.Column(db.Integer, default=0)

    def check_password(self, password_input):
        return check_password_hash(self.password, password_input)
    
    def get_badge(self):
        if self.total_points >= 2000:
            return "Gold"
        elif self.total_points >= 1000:
            return "Silver"
        elif self.total_points >= 500:
            return "Bronze"
        else:
            return "None"
    
    def to_dict(self):
        return {
            "id": self.id,
            "name": self.name,
            "email": self.email,
            "total_points": self.total_points,
            "badge": self.get_badge()
        }

class RewardAction(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    action_name = db.Column(db.String(255))
    points = db.Column(db.Integer)
    description = db.Column(db.Text)

class UserReward(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'))
    reward_action_id = db.Column(db.Integer, db.ForeignKey('reward_action.id'))
    timestamp = db.Column(db.DateTime, server_default=db.func.now())

class ScanHistory(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'))
    image_base64 = db.Column(db.Text)
    predicted_class = db.Column(db.String(100))
    confidence = db.Column(db.Float)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)