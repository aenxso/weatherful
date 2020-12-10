package com.angelsingh.weatherful.model

import com.google.gson.annotations.SerializedName


class WeatherData(@SerializedName("name") var cityName : String,
                  @SerializedName("main") var main : Main,
                  @SerializedName("weather") var weather: List<Weather>)

class Weather(@SerializedName("id") var id : Int)

class Main(@SerializedName("temp") var temperature : Double)
