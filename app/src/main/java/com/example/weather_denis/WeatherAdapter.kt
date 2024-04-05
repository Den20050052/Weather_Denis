package com.example.weather_denis

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WeatherAdapter(private val forecasts: List<DailyForecast>) : RecyclerView.Adapter<WeatherAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.weather_week_item, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val forecast = forecasts[position]
        holder.bind(forecast)
    }

    override fun getItemCount(): Int = forecasts.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewInfo: ImageView = itemView.findViewById(R.id.imageViewInfo)
        private val textViewDay: TextView = itemView.findViewById(R.id.textViewDay)
        private val textViewWeather: TextView = itemView.findViewById(R.id.textViewWeather)
        private val textViewTemperature: TextView = itemView.findViewById(R.id.textViewTemperature)
        private val textViewHumidity: TextView = itemView.findViewById(R.id.textViewHumidity)
        private val textViewWindSpeed: TextView = itemView.findViewById(R.id.textViewWindSpeed)

        fun bind(forecast: DailyForecast) {
            val drawableRes: Int = when (forecast.weather) {
                "Clear" -> R.drawable.sun
                "Clouds" -> R.drawable.clouds
                "Rain" -> R.drawable.rain
                else -> R.drawable.weather_back
            }
            imageViewInfo.setImageResource(drawableRes)

            textViewDay.text = forecast.date
            textViewWeather.text = forecast.weather
            textViewTemperature.text = itemView.context.getString(R.string.temperature, forecast.temperature)
            textViewHumidity.text = itemView.context.getString(R.string.humidity, forecast.humidity)
            textViewWindSpeed.text = itemView.context.getString(R.string.wind_speed, forecast.windSpeed)
        }
    }



}
