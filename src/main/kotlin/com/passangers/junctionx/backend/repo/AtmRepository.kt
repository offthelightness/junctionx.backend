package com.passangers.junctionx.backend.repo

import com.passangers.junctionx.backend.model.Atm
import com.passangers.junctionx.backend.model.AtmLoad
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.DayOfWeek
import java.util.*

interface AtmRepository : JpaRepository<Atm, UUID> {
}