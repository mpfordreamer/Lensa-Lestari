package com.example.lensalestari.data.repository

import com.example.lensalestari.data.api.ApiClient
import com.example.lensalestari.data.api.ApiService
import com.example.lensalestari.data.model.LoginResponse
import com.example.lensalestari.data.model.RegisterResponse
import retrofit2.Call

class AuthRepository(private val apiService: ApiService) {
    fun login(email: String, password: String): Call<LoginResponse> {
        return apiService.login(email, password)
    }

    fun register(name: String, email: String, password: String): Call<RegisterResponse> {
        // Pastikan API Flask-mu menerima field "name"
        return apiService.register(name, email, password)
    }
}