package com.angelsingh.weatherful

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.angelsingh.weatherful.api.ApiRequests
import com.angelsingh.weatherful.model.WeatherData
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

//    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val apiReq by lazy {
        ApiRequests.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        searchButton.setOnClickListener {
            if (searchField.query.toString().isNotEmpty()) {
                fetchWeather(searchField.query.toString())
            }
        }
    }

    private fun fetchWeather(searchString: String) {
        launch(Dispatchers.Main) {
            val call = apiReq.getForecast(searchString, BuildConfig.OPEN_WEATHER_API_KEY, "metric")
            call.enqueue(object : Callback<WeatherData> {
                override fun onResponse(call: Call<WeatherData>, response: Response<WeatherData>) {
                    if (!response.isSuccessful) {
                        Log.d("TAG", response.message())
                    }

                    val weatherData = response.body()!!
                    Log.d("TAG", weatherData.cityName)
                    degressNumLabel.text = weatherData.main.temperature.toString()
                    cityLabel.text = weatherData.cityName

                }

                override fun onFailure(call: Call<WeatherData>, t: Throwable) {
                    Log.d("TAG", t.toString())
                }
            })
        }
    }
}