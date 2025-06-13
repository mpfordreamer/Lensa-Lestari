from app.models import RewardAction

def seed_reward_actions(db):
    """Seed tabel RewardAction hanya jika masih kosong."""
    if RewardAction.query.first():
        return False  # Sudah ada data, tidak perlu seeding

    actions = [
        RewardAction(action_name='Pemindaian sampah berhasil', points=5, description='Gambar dikenali sebagai jenis sampah tertentu'),
        RewardAction(action_name='Upload laporan lokasi sampah liar', points=10, description='Termasuk foto, lokasi, dan deskripsi'),
        RewardAction(action_name='Laporan diverifikasi oleh admin', points=15, description='Admin menyetujui laporan sebagai valid'),
        RewardAction(action_name='Menyelesaikan tantangan mingguan', points=20, description='Misalnya: "Scan 5 sampah plastik minggu ini"'),
        RewardAction(action_name='Share edukasi ke media sosial dari aplikasi', points=5, description='Membagikan konten kampanye dari fitur edukasi'),
        RewardAction(action_name='Ajak teman (referal berhasil registrasi)', points=10, description='Teman install dan login pakai kode referal'),
        RewardAction(action_name='Menonton konten edukasi video hingga selesai', points=3, description='Hanya dihitung jika ditonton sampai akhir'),
        RewardAction(action_name='Kuis edukasi: menjawab semua pertanyaan benar', points=7, description='Topik terkait daur ulang, jenis sampah, dsb')
    ]

    db.session.bulk_save_objects(actions)
    db.session.commit()
    return True  # Data berhasil di-seed
