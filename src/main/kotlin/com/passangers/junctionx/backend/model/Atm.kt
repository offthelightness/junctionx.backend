package com.passangers.junctionx.backend.model

import java.util.*
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class Atm(
    @Id
    val id: UUID = UUID.randomUUID(),
    val city: String = "",
    val zipCD: String = "",
    val address: String = "",
    val geoX: Double = 0.0,
    val geoY: Double = 0.0,
    val canDeposit: Boolean = false
)