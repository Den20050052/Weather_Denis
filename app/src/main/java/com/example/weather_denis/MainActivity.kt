package com.example.weather_denis

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.Locale
import kotlin.math.round

class MainActivity : AppCompatActivity() {

    private lateinit var editTextCity: EditText
    private lateinit var buttonGetWeather: Button
    private lateinit var imageViewWeather: ImageView
    private lateinit var textViewTemperature: TextView
    private lateinit var textViewHumidity: TextView
    private lateinit var textViewWindSpeed: TextView
    private lateinit var WeatherWeekBtn: TextView


    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Проверяем текущую тему устройства и применяем соответствующую тему для приложения
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> {
                // Устройство находится в темном режиме, применяем темную тему для приложения
                setTheme(R.style.Base_Theme_Weather_Denis)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                // Устройство находится в светлом режиме, применяем светлую тему для приложения
                setTheme(R.style.Theme_Weather_Denis)
            }
        }

        setContentView(R.layout.activity_main)

        editTextCity = findViewById(R.id.editTextCity)
        imageViewWeather = findViewById(R.id.imageViewWeather)
        textViewTemperature = findViewById(R.id.textViewTemperature)
        textViewHumidity = findViewById(R.id.textViewHumidity)
        textViewWindSpeed = findViewById(R.id.textViewWindSpeed)
        WeatherWeekBtn=findViewById(R.id.WeatherWeekBtn)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Получение провайдера местоположения
            val locationManager =
                getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val location =
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (location != null) {
                val geocoder = Geocoder(this, Locale.getDefault())
                val addresses = geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1
                )
                if (addresses != null && addresses.isNotEmpty()) {
                    val cityName = addresses[0]?.locality
                    if (cityName != null) {
                        editTextCity.setText(cityName)
                    } else {
                        editTextCity.setText(getString(R.string.unknown_location))
                    }
                } else {
                    editTextCity.setText(getString(R.string.unknown_location))
                }
            }

        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }

        val imageViewGetWeather = findViewById<ImageView>(R.id.imageViewGetWeather)
        imageViewGetWeather.setOnClickListener {
            val city = editTextCity.text.toString()
            if (city.isNotEmpty()) {
                getWeather(city)
            }
        }

        WeatherWeekBtn.setOnClickListener(){
                val intent = Intent(this, WeatherForWeek::class.java).apply {
                    putExtra("forWeek", editTextCity.text.toString())
                }
                startActivity(intent)
        }

    }

    private fun getWeather(city: String) {
        lifecycleScope.launch {
            try {
                val apiKey = "48954abbcc3b52020e5d86f115ea027f"
                val response = RetrofitClient.api.getWeather(city, apiKey)
                updateUI(response.weather[0].main, response.main.temp, response.main.humidity, response.wind.speed)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateUI(weather: String, temperatureInKelvin: Double, humidity: Int, windSpeed: Double) {
        val drawable: Drawable? = when (weather) {
            "Clear" -> ContextCompat.getDrawable(this, R.drawable.sun)
            "Clouds" -> ContextCompat.getDrawable(this, R.drawable.clouds)
            "Rain" -> ContextCompat.getDrawable(this, R.drawable.rain)
            else -> ContextCompat.getDrawable(this, R.drawable.weather_back)
        }

        imageViewWeather.setImageDrawable(drawable)

        val temperatureInCelsius = round(temperatureInKelvin - 273.15)

        textViewTemperature.text = getString(R.string.temperature, temperatureInCelsius)
        textViewHumidity.text = getString(R.string.humidity, humidity)
        textViewWindSpeed.text = getString(R.string.wind_speed, windSpeed)
    }
}
data class WeatherResponse(
    val main: Main,
    val wind: Wind,
    val weather: List<Weather>,
    val name: String
)

data class Main(
    val temp: Double,
    val humidity: Int
)

data class Wind(
    val speed: Double
)

data class Weather(
    val main: String
)

interface WeatherApi {
    @GET("weather")
    suspend fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String
    ): WeatherResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    val api        get() = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WeatherApi::class.java)
}
