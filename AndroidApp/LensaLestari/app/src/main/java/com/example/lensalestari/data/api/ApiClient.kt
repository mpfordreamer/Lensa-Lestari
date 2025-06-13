package com.example.lensalestari.data.api

import android.content.Context
import com.example.lensalestari.utils.SessionManager
import com.example.lensalestari.utils.UrlManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // Cache instance agar tidak dibuat berulang kali
    private var apiService: ApiService? = null
    private var lastUsedUrl: String? = null

    fun getInstance(context: Context): ApiService? {
        val currentUrl = UrlManager.getBaseUrl(context) ?: return null

        // Jika URL tidak berubah dan instance sudah ada, gunakan yang lama
        if (currentUrl == lastUsedUrl && apiService != null) {
            return apiService
        }

        // Jika URL berubah atau instance belum ada, buat yang baru
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            // --- GANTI BAGIAN INI ---
            // Buat instance SessionManager
            val sessionManager = SessionManager(context)
            // Ambil token menggunakan fungsi yang sudah Anda buat
            val token = sessionManager.getAuthToken()
            // ------------------------

            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()

            if (!token.isNullOrEmpty()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }

            val request = requestBuilder.build()
            chain.proceed(request)
        }

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(currentUrl) // Gunakan URL dari SharedPreferences
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
        lastUsedUrl = currentUrl // Simpan URL yang terakhir digunakan

        return apiService
    }
}