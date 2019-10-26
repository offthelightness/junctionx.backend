package com.passangers.junctionx.backend.repo

import com.passangers.junctionx.backend.model.AtmLoad
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.DayOfWeek
import java.util.*

interface AtmLoadRepository : JpaRepository<AtmLoad, UUID> {
    @Query(
        "SELECT a FROM AtmLoad a WHERE a.atmID = ?1 " +
                "and a.dayOfWeek = ?2 " +
                "and a.periodStart < ?3 " +
                "and a.periodEnd >= ?4"
    )
    fun findByAtmIDForConcreteDayAndPeriod(
        atmId: UUID,
        dayOfWeek: DayOfWeek,
        periodStart: Long,
        periodEnd: Long
    ): List<AtmLoad>
}