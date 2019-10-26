package com.passangers.junctionx.backend.repo

import com.passangers.junctionx.backend.model.Atm
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface AtmRepository : JpaRepository<Atm, UUID>