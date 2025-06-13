package com.example.lensalestari.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private var prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_FILENAME = "app_prefs"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_POIN = "user_poin" // <-- 1. TAMBAHKAN KEY BARU UNTUK POIN
    }

    /**
     * Menyimpan data otentikasi pengguna setelah login/register berhasil.
     */
    // 2. PERBARUI FUNGSI INI DENGAN MENAMBAHKAN PARAMETER 'poin'
    fun saveAuthData(name: String, email: String, token: String, poin: Int) {
        val editor = prefs.edit()
        editor.putString(KEY_USER_NAME, name)
        editor.putString(KEY_USER_EMAIL, email)
        editor.putString(KEY_AUTH_TOKEN, token)
        editor.putInt(KEY_USER_POIN, poin) // <-- Simpan poin
        editor.apply()
    }

    /**
     * Mengambil nama pengguna yang tersimpan.
     */
    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    /**
     * Mengambil token otentikasi yang tersimpan.
     */
    fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    /**
     * Mengambil poin pengguna yang tersimpan.
     */
    // 3. TAMBAHKAN FUNGSI BARU INI
    fun getUserPoin(): Int {
        // Mengembalikan 0 jika data poin tidak ditemukan
        return prefs.getInt(KEY_USER_POIN, 0)
    }

    /**
     * Membersihkan semua data sesi (digunakan saat logout).
     */
    fun clearAuthData() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}