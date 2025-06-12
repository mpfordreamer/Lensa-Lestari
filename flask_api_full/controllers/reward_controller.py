from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from models import db, RewardAction, UserReward, User

reward_bp = Blueprint('reward', __name__)

@reward_bp.route('/reward/actions', methods=['GET'])
def get_rewards():
    actions = RewardAction.query.all()
    return jsonify([{
        "id": a.id,
        "action": a.action_name,
        "points": a.points,
        "description": a.description
    } for a in actions])

@reward_bp.route('/reward/submit', methods=['POST'])
@jwt_required()
def submit_action():
    user_id = get_jwt_identity()
    action_id = request.json.get('reward_action_id')

    action = RewardAction.query.get(action_id)
    if not action:
        return jsonify({"message": "Action not found"}), 404

    db.session.add(UserReward(user_id=user_id, reward_action_id=action_id))
    user = User.query.get(user_id)
    user.total_points += action.points
    db.session.commit()

    return jsonify({"message": f"{action.points} poin ditambahkan", "total_points": user.total_points})

def apply_reward(user_id, action_id):
    reward_action = RewardAction.query.get(action_id)
    if not reward_action:
        return "Reward action tidak ditemukan."

    user = User.query.get(user_id)
    if not user:
        return "User tidak ditemukan."

    db.session.add(UserReward(user_id=user_id, reward_action_id=reward_action.id))
    user.total_points = (user.total_points or 0) + reward_action.points
    db.session.commit()

    return f"{reward_action.points} poin ditambahkan."

@reward_bp.route('/reward/scan_sampah', methods=['POST'])
@jwt_required()
def reward_scan_sampah():
    user_id = get_jwt_identity()
    msg = apply_reward(user_id, 1)
    return jsonify({
        "message": msg
    })


@reward_bp.route('/reward/upload_laporan', methods=['POST'])
@jwt_required()
def reward_upload_laporan():
    user_id = get_jwt_identity()
    msg = apply_reward(user_id, 2)
    return jsonify({
        "message": msg
    })


@reward_bp.route('/reward/laporan_diverifikasi', methods=['POST'])
@jwt_required()
def reward_laporan_diverifikasi():
    user_id = get_jwt_identity()
    msg = apply_reward(user_id, 3)
    return jsonify({
        "message": msg
    })


@reward_bp.route('/reward/tantangan_mingguan', methods=['POST'])
@jwt_required()
def reward_tantangan_mingguan():
    user_id = get_jwt_identity()
    msg = apply_reward(user_id, 4)
    return jsonify({
        "message": msg
    })


@reward_bp.route('/reward/share_edukasi', methods=['POST'])
@jwt_required()
def reward_share_edukasi():
    user_id = get_jwt_identity()
    msg = apply_reward(user_id, 5)
    return jsonify({
        "message": msg
    })


@reward_bp.route('/reward/referal_berhasil', methods=['POST'])
@jwt_required()
def reward_referal_berhasil():
    user_id = get_jwt_identity()
    msg = apply_reward(user_id, 6)
    return jsonify({
        "message": msg
    })


@reward_bp.route('/reward/tonton_video', methods=['POST'])
@jwt_required()
def reward_tonton_video():
    user_id = get_jwt_identity()
    msg = apply_reward(user_id, 7)
    return jsonify({
        "message": msg
    })


@reward_bp.route('/reward/kuis_selesai', methods=['POST'])
@jwt_required()
def reward_kuis_selesai():
    user_id = get_jwt_identity()
    msg = apply_reward(user_id, 8)
    return jsonify({
        "message": msg
    })

