package com.passangers.junctionx.backend.model

import com.passangers.junctionx.backend.service.PredictionService
import com.passangers.junctionx.backend.service.PredictionService.Companion.AVERAGE_PEDESTRIAN_SPEED
import com.passangers.junctionx.backend.service.PredictionService.Companion.AVERAGE_SESSION_TIME_IN_MINUTES
import java.time.LocalDateTime
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class ATMIntent (
        @Id
        val userId: String = "",
        val atmId: String = "",
        val realDistanceToAtmInMeters: Double = 0.0,
        val averageHistoricalWaitingTime: Double = 0.0,
        val realtimeWaitingTime : Double = 0.0,
        val expiredTime: LocalDateTime? =
            LocalDateTime.now()
                .plusSeconds((60 * PredictionService.AVERAGE_SESSION_TIME_IN_MINUTES).toLong())
                .plusSeconds((60 * realDistanceToAtmInMeters  / AVERAGE_PEDESTRIAN_SPEED).toLong())
                .plusSeconds((60 * averageHistoricalWaitingTime).toLong())
                .plusSeconds((60 * realtimeWaitingTime).toLong())
)