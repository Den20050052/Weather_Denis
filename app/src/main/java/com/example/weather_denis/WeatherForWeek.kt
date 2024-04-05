package com.example.weather_denis

import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.*

class WeatherForWeek : AppCompatActivity() {

    private lateinit var recyclerViewWeather: RecyclerView
    private lateinit var weatherAdapter: WeatherAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> {
                setTheme(R.style.Base_Theme_Weather_Denis)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                setTheme(R.style.Theme_Weather_Denis)
            }
        }

        setContentView(R.layout.activity_weather_for_week)
        recyclerViewWeather = findViewById(R.id.recyclerViewWeather)
        recyclerViewWeather.layoutManager = LinearLayoutManager(this)

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            val city = intent.getStringExtra("forWeek")
            if (city != null) {
                getWeeklyWeather(city)
            } else {
                Toast.makeText(this, "City is null", Toast.LENGTH_SHORT).show()
                swipeRefreshLayout.isRefreshing = false
            }
        }

        val city = intent.getStringExtra("forWeek")
        if (city != null) {
            getWeeklyWeather(city)
        } else {
            Toast.makeText(this, "City is null", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getWeeklyWeather(city: String) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val apiKey = "48954abbcc3b52020e5d86f115ea027f"
                val response = RetrofitClientWeek.api.getWeeklyWeather(city, apiKey)
                updateUI(response.list)
            } catch (e: Exception) {
                Toast.makeText(this@WeatherForWeek, "Failed to get weather data", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            } finally {
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun updateUI(weatherList: List<WeatherForDay>) {
        val dailyForecasts = weatherList.map { weatherForDay ->
            DailyForecast(
                date = formatDate(weatherForDay.dt),
                temperature = kelvinToCelsius(weatherForDay.main.temp),
                humidity = weatherForDay.main.humidity,
                weather = weatherForDay.weather[0].main,
                windSpeed = meterPerSecToKmPerHour(weatherForDay.wind.speed)
            )
        }
        weatherAdapter = WeatherAdapter(dailyForecasts)
        recyclerViewWeather.adapter = weatherAdapter
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEEE, MMMM dd, HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp * 1000))
    }


    private fun kelvinToCelsius(kelvin: Double): Double {
        return kelvin - 273.15
    }


    private fun meterPerSecToKmPerHour(meterPerSec: Double): Double {
        return meterPerSec * 3.6
    }
}



data class WeatherForWeekResponse(
    val city: City,
    val list: List<WeatherForDay>
)

data class City(
    val id: Int,
    val name: String,
    val country: String
)

data class WeatherForDay(
    val dt: Long,
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind,
    val clouds: Clouds
)

data class Clouds(
    val all: Int
)

interface WeatherApiWeek {
    @GET("forecast")
    suspend fun getWeeklyWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String
    ): WeatherForWeekResponse
}

object RetrofitClientWeek {
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    val api: WeatherApiWeek = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WeatherApiWeek::class.java)
}
