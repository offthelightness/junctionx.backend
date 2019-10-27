package com.passangers.junctionx.backend.model

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
        val expiredTime: LocalDateTime = LocalDateTime.now().plusMinutes(AVERAGE_SESSION_TIME_IN_MINUTES.toLong())
)