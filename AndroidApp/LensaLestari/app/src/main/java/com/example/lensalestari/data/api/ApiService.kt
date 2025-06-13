package com.example.lensalestari.data.api

import com.example.lensalestari.data.model.ClassificationResponse
import com.example.lensalestari.data.model.HistoryResponse
import com.example.lensalestari.data.model.LoginResponse
import com.example.lensalestari.data.model.PointHistoryResponse
import com.example.lensalestari.data.model.RegisterResponse
import com.example.lensalestari.data.model.TrashHistoryResponse
import com.example.lensalestari.data.model.YoloResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @FormUrlEncoded
    @POST("/auth/login")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("/auth/register")
    fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<RegisterResponse>

    @Multipart
    @POST("/analyze-image")
    fun analyzeImage(
        @Part image: MultipartBody.Part
    ): Call<ClassificationResponse>

    @Multipart
    @POST("/detect-object")
    fun detectObject(
        @Part image: MultipartBody.Part
    ): Call<YoloResponse>

    @GET("/scan-history")
    fun getAnalysisHistory(
        @Header("Authorization") token: String,
        @Query("page") page: Int
    ): Call<HistoryResponse>

    @GET("/reward-history")
    fun getPointHistory(
        @Header("Authorization") token: String,
        @Query("page") page: Int
    ): Call<PointHistoryResponse>

    @GET("/trash-history")
    fun getTrashHistory(
        @Header("Authorization") token: String
    ): Call<TrashHistoryResponse>
}