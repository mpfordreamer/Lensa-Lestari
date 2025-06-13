package com.example.lensalestari.data.model

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @SerializedName("token")
    val token: String,

    @SerializedName("user")
    val user: User
)