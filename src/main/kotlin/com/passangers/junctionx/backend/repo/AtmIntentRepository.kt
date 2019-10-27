package com.passangers.junctionx.backend.repo

import com.passangers.junctionx.backend.model.ATMIntent
import com.passangers.junctionx.backend.model.Atm
import com.passangers.junctionx.backend.model.AtmLoad
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.DayOfWeek
import java.util.*

interface AtmIntentRepository : JpaRepository<ATMIntent, UUID> {
    fun findByUserId(userId: String):List<ATMIntent>
    @Query("SELECT count(atm) FROM ATMIntent atm WHERE atm.atmId = id")
    fun findCountOfIntentForATM(id: UUID): Int


}