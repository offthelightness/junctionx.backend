package com.passangers.junctionx.backend.model

import java.time.DayOfWeek
import java.time.LocalTime
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class AtmLoad(
    @Id
    val id: UUID = UUID.randomUUID(),
    val atmID: UUID = UUID.randomUUID(),
    val dayOfWeek: DayOfWeek = DayOfWeek.SUNDAY,
    val periodStart: Long = 0,
    val periodEnd: Long = 0,
    val transactionsCount: Int = 0
)