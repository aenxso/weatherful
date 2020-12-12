package com.angelsingh.weatherful.api

import com.angelsingh.weatherful.model.WeatherData
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiRequests {

    @GET("data/2.5/weather?")
    fun getForecast(
        @Query("q") cityName: String,
        @Query("appid") appId: String,
        @Query("units") units: String
    ): Call<WeatherData>

    @GET("data/2.5/weather?")
    fun getLocalForecast(
            @Query("lat") latitude: Double?,
            @Query("lon") longitude: Double?,
            @Query("appid") appId: String,
            @Query("units") units: String
    ): Call<WeatherData>

    companion object {
        fun create(): ApiRequests {
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.openweathermap.org/")
                .build()
            return retrofit.create(ApiRequests::class.java)
        }
    }

}