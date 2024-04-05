package com.example.weather_denis

data class DailyForecast(
    val date: String,
    val weather: String,
    val temperature: Double,
    val humidity: Int,
    val windSpeed: Double
)
