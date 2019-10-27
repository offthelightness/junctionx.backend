package com.passangers.junctionx.backend.service

import com.passangers.junctionx.backend.model.GeoPoint
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod


@Service
class GeoService {

    private val apiKey = "AIzaSyCsCnwTyXYy2bIkXWBcwuDT3tf7DtuBj94"

    @Autowired
    lateinit var restTemplate: RestTemplate

    fun getLineDistance(pointA: GeoPoint, pointB: GeoPoint): Double {

        var R = 6371e3; // metres
        var φ1 = Math.toRadians(pointA.latitude)
        var φ2 = Math.toRadians(pointB.latitude)
        var Δφ = Math.toRadians(pointB.latitude - pointA.latitude)
        var Δλ = Math.toRadians(pointB.longitude - pointA.longitude)

        var a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
                Math.cos(φ1) * Math.cos(φ2) *
                Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        var d = R * c;
        return d;
    }


    fun getRealDistance(pointA: GeoPoint, pointB: GeoPoint): Double? {
        val response = restTemplate.exchange(
            "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial" +
                    "&origins=${pointA.latitude},${pointA.longitude}" +
                    "&destinations=${pointB.latitude},${pointB.longitude}" +
                    "&key=$apiKey",
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<DataMatrixAPIResponse>() {

            })

        return response.body?.rows?.firstOrNull()?.elements?.firstOrNull()?.distance?.value?.toDouble()
    }
}

data class DataMatrixAPIResponse constructor(
    val rows: List<Row>? = null
)

data class Row (
    val elements: List<Element>? = null
)

data class Element(
    val distance: Distance? = null
)

data class Distance(
    val value : Int? = null
)

