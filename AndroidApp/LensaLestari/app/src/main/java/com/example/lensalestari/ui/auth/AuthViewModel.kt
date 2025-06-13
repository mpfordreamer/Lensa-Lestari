package com.example.lensalestari.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lensalestari.data.model.LoginResponse
import com.example.lensalestari.data.model.RegisterResponse
import com.example.lensalestari.data.repository.AuthRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _loginResult = MutableLiveData<LoginResponse?>()
    val loginResult: LiveData<LoginResponse?> = _loginResult

    private val _registerResult = MutableLiveData<RegisterResponse?>()
    val registerResult: LiveData<RegisterResponse?> = _registerResult

    fun login(email: String, password: String) {
        repository.login(email, password).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                _loginResult.value = response.body()
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                _loginResult.value = null
            }
        })
    }

    fun register(name: String, email: String, password: String) {
        repository.register(name, email, password).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                _registerResult.value = response.body()
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                _registerResult.value = null
            }
        })
    }
}