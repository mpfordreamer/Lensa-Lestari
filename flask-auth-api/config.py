import os

class Config:
    SECRET_KEY = os.environ.get("FLASK_SECRET_KEY")
    JWT_SECRET_KEY = os.environ.get("JWT_SECRET_KEY")
    SQLALCHEMY_DATABASE_URI = os.environ.get("DATABASE_URL") or "sqlite:///fallback.db"    
    SQLALCHEMY_TRACK_MODIFICATIONS = False