# Lensa Lestari Overview

## 💻 Tech Stack

| Step | Library / Tools |
| :--- | :--- |
| **Exploratory Data Analysis** | ![Pandas](https://img.shields.io/badge/pandas-%235E55D3.svg?style=for-the-badge&logo=pandas&logoColor=white) ![NumPy](https://img.shields.io/badge/numpy-%234D77CF.svg?style=for-the-badge&logo=numpy&logoColor=white) ![Matplotlib](https://img.shields.io/badge/Matplotlib-%23ffffff.svg?style=for-the-badge&logo=Matplotlib&logoColor=black) |
| **Modeling** | ![Python](https://img.shields.io/badge/python-3670A0?style=for-the-badge&logo=python&logoColor=ffdd54) ![TensorFlow](https://img.shields.io/badge/TensorFlow-%23FF6F00.svg?style=for-the-badge&logo=TensorFlow&logoColor=white) ![PyTorch](https://img.shields.io/badge/PyTorch-%23EE4C2C.svg?style=for-the-badge&logo=PyTorch&logoColor=white) ![Scikit-learn](https://img.shields.io/badge/scikit--learn-%23F7931E.svg?style=for-the-badge&logo=scikit-learn&logoColor=white) |
| **Backend** | ![Flask](https://img.shields.io/badge/flask-%23000.svg?style=for-the-badge&logo=flask&logoColor=white) |
| **Mobile App** | ![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white) ![Android Studio](https://img.shields.io/badge/Android%20Studio-3DDC84.svg?style=for-the-badge&logo=android-studio&logoColor=white) |
| **Database** | ![MySQL](https://img.shields.io/badge/mysql-%234479A1.svg?style=for-the-badge&logo=mysql&logoColor=white) |
| **Deployment** | Apk |

##  Mulai (Getting Started)

Ikuti langkah-langkah di bawah ini untuk menjalankan proyek Lensa Lestari di lingkungan lokal Anda.

### Prasyarat

Pastikan Anda telah menginstal perangkat lunak berikut:
- [Git](https://git-scm.com/)
- [Python 3.8+](https://www.python.org/)
- [Android Studio](https://developer.android.com/studio)

### Instalasi Backend

1.  **Clone repositori ini:**
    ```bash
    git clone https://github.com/mpfordreamer/Lensa-Lestari.git
    cd Lensa-Lestari/backend
    ```

2.  **Buat dan aktifkan virtual environment:**
    ```bash
    python -m venv venv
    source venv/bin/activate  # Untuk Linux/macOS
    # atau
    .\venv\Scripts\activate  # Untuk Windows
    ```

3.  **Instal semua dependensi:**
    ```bash
    pip install -r requirements.txt
    ```

4.  **Siapkan file environment:**
    Buat file `.env` di dalam folder `backend/` dan isi dengan konfigurasi yang diperlukan, seperti koneksi database.
    ```env
    DATABASE_URL="your_database_connection_string"
    ```

5.  **Jalankan server backend:**
    ```bash
    flask run
    # atau
    python app.py
    ```
    Server akan berjalan di `http://127.0.0.1:5000`.

### Instalasi Aplikasi Mobile (Android)

1.  **Buka proyek di Android Studio:**
    - Buka Android Studio.
    - Pilih `Open an Existing Project`.
    - Arahkan ke folder `Lensa-Lestari/mobile-app` (atau nama folder aplikasi Anda).

2.  **Konfigurasi Alamat IP Backend:**
    - Buka file konfigurasi jaringan (misalnya `NetworkConfig.kt` atau `Constants.kt`).
    - Ubah `BASE_URL` ke alamat IP lokal komputer Anda (misalnya `http://192.168.1.10:5000/`) agar aplikasi di ponsel/emulator dapat terhubung ke server backend Anda.
    
3.  **Sinkronkan Gradle & Jalankan Aplikasi:**
    - Biarkan Android Studio menyinkronkan semua dependensi (proses Gradle).
    - Jalankan aplikasi pada emulator atau perangkat Android fisik.

### Detail Model AI

Kami menggunakan pendekatan *multi-model* dalam format `.onnx` untuk mencapai fungsionalitas yang komprehensif, dari deteksi cepat hingga identifikasi detail.

1.  **Real-time Detection: YOLOv12n**
    - **Tujuan**: Deteksi objek sampah secara *real-time* di perangkat mobile.
    - **Fungsi**: Bertugas sebagai gerbang utama untuk mendeteksi keberadaan sampah dan memberikan klasifikasi dasar (Organik/Anorganik) dengan sangat cepat.

2.  **Klasifikasi Jenis Sampah: MobileNetV2 (CNN)**
    - **Tujuan**: Klasifikasi yang lebih spesifik setelah objek terdeteksi.
    - **Fungsi**: Setelah YOLO menandai lokasi sampah, model ini akan menganalisis gambar yang terpotong untuk mengidentifikasi jenisnya secara lebih detail (misalnya: botol plastik, kardus, daun kering, sisa makanan).

3.  **Identifikasi via Similaritas: ResNet50**
    - **Tujuan**: Identifikasi yang sangat spesifik hingga ke level unik atau merek.
    - **Fungsi**: Model ini mengekstrak *feature embedding* dari gambar sampah untuk dibandingkan dengan database referensi menggunakan *cosine similarity*, memungkinkan pengenalan produk spesifik (misalnya, botol merek X).

### Mengakses Model

Model-model pra-terlatih dalam format `.onnx` yang kami gunakan dapat diakses langsung dari repositori ini.

-   Kami menyediakan model-model siap pakai di dalam folder [`/models`](https://github.com/mpfordreamer/Lensa-Lestari) di repositori ini.

Pastikan path ke model di dalam kode backend sudah sesuai dengan lokasi Anda menyimpan model.

## Cara Menggunakan Aplikasi

Alur penggunaan aplikasi dirancang untuk memanfaatkan ketiga model secara berurutan:

1.  **Buka Aplikasi**: Jalankan aplikasi Lensa Lestari di perangkat Android Anda.
2.  **Arahkan Kamera & Lakukan Scan Awal**: Ketuk tombol "Scan Sampah". Model **YOLOv12n** akan secara *real-time* mendeteksi objek dan menampilkan kotak pembatas beserta klasifikasi awalnya (Organik/Anorganik).
3.  **Dapatkan Detail Klasifikasi**: Setelah objek terdeteksi, aplikasi akan secara otomatis menggunakan model **MobileNetV2** dan **ResNet50** untuk memberikan informasi yang lebih kaya, seperti jenis spesifik sampah.
4.  **Lihat Info Lengkap & Edukasi**: Hasil analisis detail, dampak lingkungan, dan cara pengelolaan yang benar akan ditampilkan.
5.  **Kumpulkan Poin**: Setiap pemindaian yang berhasil akan memberikan Anda poin sebagai bentuk apresiasi

## Tim Pengembang

Proyek "Lensa Lestari" dikembangkan oleh tim Laskar AI LAI25-SM059:

- **I Dewa Gede Mahesta Parawangsa** - (Lead ML Engineer)
- **I Kadek Ade Indra Swadinata** - (ML Support)
- **I Putu Wira Budhi Guna Ariyasa** - (Mobile App Developer)
- **Rizal Teddyansyah** - (Backend Developer & Data Engineer)
