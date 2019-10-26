package com.passangers.junctionx.backend.model

import java.util.*
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class Atm(
    @Id
    val id: UUID,
    val city: String,
    val zipCD: String,
    val address: String,
    val geoX: Double,
    val geoY: Double,
    val canDeposit: Boolean
)