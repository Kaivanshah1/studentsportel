package com.liquifysolutions.studentportel.domain
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import kotlinx.serialization.Serializable

@Document(collection = "student")
data class Student(
    @Id val id: String? = null,
    val name: String,
    val age: Int,
    val email: String,
    val gender: String,
    val assignedClass: String
)

data class ListStudentsInput(
    val nameSearch: String? = null,
    val age: Int? = null,
    val assignedClass: List<String>? = null,
    val email: String? = null,
    val gender: String? = null,
)

@Serializable
data class WeatherResponse(
    val request: Request,
    val location: Location,
    val current: CurrentWeather
)

@Serializable
data class Request(
    val type: String,
    val query: String,
    val language: String,
    val unit: String
)

@Serializable
data class Location(
    val name: String,
    val country: String,
    val region: String,
    val lat: String,
    val lon: String,
    val timezone_id: String,
    val localtime: String,
    val localtime_epoch: Long,
    val utc_offset: String
)

@Serializable
data class CurrentWeather(
    val observation_time: String,
    val temperature: Int,
    val weather_code: Int,
    val weather_icons: List<String>,
    val weather_descriptions: List<String>,
    val wind_speed: Int,
    val wind_degree: Int,
    val wind_dir: String,
    val pressure: Int,
    val precip: Int,
    val humidity: Int,
    val cloudcover: Int,
    val feelslike: Int,
    val uv_index: Int,
    val visibility: Int,
    val is_day: String
)
