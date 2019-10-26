package com.passangers.junctionx.backend.model

import java.time.DayOfWeek
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class AtmLoad(
    @Id
    val id: UUID,
    val atmID: UUID,
    val dayOfWeek: DayOfWeek,
    val periodStart: Date,
    val periodEnd: Date,
    val transactionsCount: Int
)