package com.angelsingh.weatherful

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
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

        hideKeyboard(contentMain)
        searchPressed((searchField))
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
                    degressNumLabel.text = String.format("%.1f", weatherData.main.temperature)
                    cityLabel.text = weatherData.cityName

                    searchField.setQuery("", false)

                }

                override fun onFailure(call: Call<WeatherData>, t: Throwable) {
                    Log.d("TAG", t.toString())
                }
            })
        }
    }

    private fun hideKeyboard(view: View) {
        view.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                return true
            }
        })
    }

    private fun searchPressed(search: SearchView) {
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (searchField.query.toString().isNotEmpty()) {
                    fetchWeather(searchField.query.toString())
                }
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                Log.i("TAG","Llego al querytextchange")
                return true
            }
        })
    }

}