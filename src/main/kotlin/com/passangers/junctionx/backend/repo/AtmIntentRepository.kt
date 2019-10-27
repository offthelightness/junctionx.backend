package com.passangers.junctionx.backend.repo

import com.passangers.junctionx.backend.model.ATMIntent
import com.passangers.junctionx.backend.model.Atm
import com.passangers.junctionx.backend.model.AtmLoad
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.DayOfWeek
import java.util.*
import java.time.LocalDateTime
import org.springframework.scheduling.annotation.Scheduled



interface AtmIntentRepository : JpaRepository<ATMIntent, UUID> {

    fun findByUserId(userId: String):List<ATMIntent>

    @Query("SELECT count(intent) FROM ATMIntent intent WHERE intent.atmId = ?1")
    fun findCountOfIntentForATM(id: String): Int

    @Query("DELETE FROM ATMIntent intent WHERE intent.expiredTime < ?1")
    fun clearExpiredIntents(localDateTime: LocalDateTime)
}