package com.passangers.junctionx.backend.repo

import com.passangers.junctionx.backend.model.Atm
import com.passangers.junctionx.backend.model.AtmLoad
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface AtmLoadRepository : JpaRepository<AtmLoad, UUID> {
    fun findByAtmID(id: UUID): List<AtmLoad>
}