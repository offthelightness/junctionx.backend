package com.passangers.junctionx.backend.service

import com.passangers.junctionx.backend.model.GeoPoint
import org.springframework.stereotype.Service


@Service
class GeoService {

    fun getSimpleDistance(pointA: GeoPoint, pointB: GeoPoint): Double {

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
}