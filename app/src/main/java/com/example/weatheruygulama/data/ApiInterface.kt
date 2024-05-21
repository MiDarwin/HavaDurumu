package com.example.weatheruygulama.data

import com.example.weatheruygulama.data.models.CurrentWeather
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    @GET("weather?")
    suspend fun getCurrentWeather(
        @Query("q") cityName: String,
        @Query("units") units: String,
        @Query("appid") apiKey: String
    ): Response<CurrentWeather>
}
