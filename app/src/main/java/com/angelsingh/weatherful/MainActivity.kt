package com.angelsingh.weatherful

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import com.angelsingh.weatherful.api.ApiRequests
import com.angelsingh.weatherful.model.WeatherData
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private var cts = CancellationTokenSource()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val apiReq by lazy {
        ApiRequests.create()
    }
    var locationReq: LocationRequest? = null

    var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            if (locationResult == null) {
                return
            }
            for (location in locationResult.locations) {
                Log.d("TAG-locations", location.toString())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContentView(R.layout.activity_main)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationReq = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        searchButton.setOnClickListener {
            if (searchField.query.toString().isNotEmpty()) {
                fetchWeather(searchField.query.toString())
                hideKeyboard()
            }
        }
        getLocation()

        locationButton.setOnClickListener {
            getLocation()
        }

        onTouchOutside(contentMain)
        searchPressed(searchField)
    }

    private fun fetchWeather(searchString: String) {
        launch(Dispatchers.Main) {
            val call = apiReq.getForecast(searchString, BuildConfig.OPEN_WEATHER_API_KEY, getString(R.string.metric))
            call.enqueue(object : Callback<WeatherData> {
                override fun onResponse(call: Call<WeatherData>, response: Response<WeatherData>) {
                    if (!response.isSuccessful) {
                        Log.d("TAG", response.message())
                    }
                    val weatherData = response.body()!!
                    Log.d("TAG", weatherData.cityName)
                    degreesNumLabel.text = String.format("%.1f", weatherData.main.temperature)
                    cityLabel.text = weatherData.cityName

                    searchField.setQuery("", false)
                }

                override fun onFailure(call: Call<WeatherData>, t: Throwable) {
                    Log.d("TAG", t.toString())
                }
            })
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    private fun onTouchOutside(view: View) {
        view.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                hideKeyboard()
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
                Log.i("TAG", "Llego al querytextchange")
                return true
            }
        })
    }

    private fun getLocation() {
        Log.d("TAG", "getLocation function reached")
        val location: Location? = null
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("TAG", "Location permission denied")
        } else {
            Log.d("TAG", ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION).toString())
            Log.d("TAG", "Permission granted")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 0)

        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        val locationTask: Task<Location> = fusedLocationClient.getLastLocation()
        locationTask.addOnSuccessListener { location ->
            if (location != null) {
                //We have a location
                Log.d("TAG", "onSuccess: $location")
                Log.d("TAG", "onSuccess lat: " + location.latitude)
                Log.d("TAG", "onSuccess lon: " + location.longitude)
                val lon = location.longitude
                val lat = location.latitude
                fetchLocalWeather(lat, lon)
            } else {
                Log.d("TAG", "onSuccess: Location was null...")
            }
        }
        locationTask.addOnFailureListener { e -> Log.e("TAG", "onFailure: " + e.localizedMessage) }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray)  {
        when (requestCode) {
            0 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Log.d("TAG", "About to access last location")
                    fusedLocationClient.requestLocationUpdates(locationReq, locationCallback, Looper.getMainLooper());
                    getLastLocation()

//                    fusedLocationClient.getCurrentLocation(PRIORITY_LOW_POWER, cts.token)


//                    val mLocationCallback: LocationCallback = object : LocationCallback() {
//                        override fun onLocationResult(locationResult: LocationResult) {
//                            Log.d("TAG", locationResult.lastLocation.longitude.toString())
//                            val lon = locationResult.lastLocation.longitude
//                            val lat = locationResult.lastLocation.latitude
//                            fetchLocalWeather(lat, lon)
//                        }
//                    }
//                    LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationReq, mLocationCallback, null)


//                    fusedLocationClient.lastLocation
//                            .addOnSuccessListener { location: Location? ->
//                                val lat = location?.latitude
//                                val lon = location?.longitude
//                                fetchLocalWeather(lat, lon)
//                            }
                } else {
                    Toast.makeText(this@MainActivity, "Permission denied to access your location", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    private fun fetchLocalWeather(lat: Double?, lon: Double?) {
        Log.d("TAG", "Fetching local weather")
//        launch(Dispatchers.Main) {
//            val call = apiReq.getLocalForecast (lat, lon, BuildConfig.OPEN_WEATHER_API_KEY, getString(R.string.metric))
//            call.enqueue(object : Callback<WeatherData> {
//                override fun onResponse(call: Call<WeatherData>, response: Response<WeatherData>) {
//                    if (!response.isSuccessful) {
//                        Log.d("TAG", response.message())
//                    }
//                    val weatherData = response.body()!!
//                    Log.d("TAG", weatherData.cityName)
//                    degreesNumLabel.text = String.format("%.1f", weatherData.main.temperature)
//                    cityLabel.text = weatherData.cityName
//
//                    searchField.setQuery("", false)
//                }
//
//                override fun onFailure(call: Call<WeatherData>, t: Throwable) {
//                    Log.d("TAG", t.toString())
//                }
//            })
//        }
    }

}